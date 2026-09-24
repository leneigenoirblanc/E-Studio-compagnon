package com.example.ui.scan

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.RetailCatalog
import com.example.data.local.AppDatabase
import com.example.data.local.LotRepository
import com.example.data.local.SessionManager
import com.example.data.remote.EStudioApiClient
import com.example.model.LabelTemplate
import com.example.model.MobileScanItem
import com.example.model.MobileScanLot
import com.example.scanner.ScannerFeedback
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class ScanUiState(
    val lotId: String = "",
    val lotName: String = "Rayon Épicerie - Réassort",
    val operatorName: String = "Agent Rayon - 04",
    val deviceName: String = "Android Terminal",
    val deviceType: String = "android",
    val targetTemplateId: String = "template_38x70",
    val selectedTemplate: LabelTemplate = AVAILABLE_TEMPLATES[0],
    val items: List<MobileScanItem> = emptyList(),
    val quickQuantity: Int = 1,
    val isPromoMode: Boolean = false,
    val promoInputPrice: String = "",
    val isTorchOn: Boolean = false,
    val isTransferring: Boolean = false,
    val lastScannedCode: String? = null,
    val lastScannedDesignation: String? = null,
    val statusMessage: String? = null,
    val isLanConnected: Boolean = true,
    val offlinePendingLotsCount: Int = 0
) {
    val totalReferencesCount: Int get() = items.size
    val totalLabelsCount: Int get() = items.sumOf { it.quantity }
}

val AVAILABLE_TEMPLATES = listOf(
    LabelTemplate("template_38x70", "Rayon Standard (38x70mm)", "38 x 70 mm", "standard"),
    LabelTemplate("template_promo_40x80", "Sticker Promo Jaune (40x80mm)", "40 x 80 mm", "promo"),
    LabelTemplate("template_a5", "Affiche Tête de Gondole (A5)", "148 x 210 mm", "affiche"),
    LabelTemplate("template_pvc_electronic", "Balisage Électronique (2.9\")", "ESL 2.9\"", "esl")
)

sealed interface ScanEvent {
    data class ShowToast(val message: String) : ScanEvent
    data class TransferSuccess(val message: String) : ScanEvent
    data class TransferError(val error: String) : ScanEvent
}

class ScanViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val lotRepository = LotRepository(database.lotDao())
    val sessionManager = SessionManager(application)
    private val apiClient = EStudioApiClient()
    private val feedback = ScannerFeedback(application)

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ScanEvent>()
    val events: SharedFlow<ScanEvent> = _events.asSharedFlow()

    private val isoDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)

    init {
        val operator = sessionManager.getOperatorName()
        val device = sessionManager.getDeviceName()
        val devType = sessionManager.getDeviceType()
        val savedTemplateId = sessionManager.getSelectedTemplateId()
        val template = AVAILABLE_TEMPLATES.find { it.id == savedTemplateId } ?: AVAILABLE_TEMPLATES[0]

        _uiState.update {
            it.copy(
                lotId = generateLotId(),
                operatorName = operator,
                deviceName = device,
                deviceType = devType,
                targetTemplateId = template.id,
                selectedTemplate = template
            )
        }

        // Observer le lot actif depuis la base Room
        viewModelScope.launch {
            lotRepository.activeLot.collect { activeLot ->
                if (activeLot != null && activeLot.items.isNotEmpty()) {
                    _uiState.update { state ->
                        state.copy(
                            lotId = activeLot.id,
                            lotName = activeLot.name,
                            items = activeLot.items,
                            operatorName = activeLot.operatorName,
                            deviceName = activeLot.deviceName,
                            deviceType = activeLot.deviceType,
                            targetTemplateId = activeLot.targetTemplateId ?: state.targetTemplateId
                        )
                    }
                }
            }
        }
    }

    private fun generateLotId(): String {
        return "lot_mob_${System.currentTimeMillis() % 10000000}"
    }

    private fun nowIso(): String {
        return isoDateFormat.format(Date())
    }

    /**
     * Traitement du scan d'un code-barres (CameraX ou Zebra DataWedge)
     */
    fun onBarcodeScanned(rawCode: String, symbology: String? = null) {
        val cleanCode = rawCode.trim()
        if (cleanCode.isBlank()) return

        feedback.notifyScanSuccess()

        val product = RetailCatalog.findProduct(cleanCode)
        val defaultDesignation = product?.designation ?: "Article EAN $cleanCode"
        val regularPrice = product?.price ?: 2.99
        val promoPrice = if (_uiState.value.isPromoMode) {
            _uiState.value.promoInputPrice.toDoubleOrNull() ?: product?.promoPrice ?: (regularPrice * 0.8)
        } else {
            product?.promoPrice
        }

        val addQuantity = _uiState.value.quickQuantity

        _uiState.update { currentState ->
            val existingItemIndex = currentState.items.indexOfFirst { it.code == cleanCode }
            val updatedItems = currentState.items.toMutableList()

            if (existingItemIndex >= 0) {
                // Incrémenter la quantité de l'article déjà présent
                val existing = updatedItems[existingItemIndex]
                updatedItems[existingItemIndex] = existing.copy(
                    quantity = existing.quantity + addQuantity,
                    scannedAt = nowIso(),
                    promoPrice = if (currentState.isPromoMode) promoPrice else existing.promoPrice
                )
            } else {
                // Ajouter un nouvel article au début de la liste
                val newItem = MobileScanItem(
                    id = "m_item_${System.currentTimeMillis()}",
                    code = cleanCode,
                    designation = defaultDesignation,
                    price = regularPrice,
                    promoPrice = promoPrice,
                    quantity = addQuantity,
                    facing = product?.facing ?: 1,
                    scannedAt = nowIso()
                )
                updatedItems.add(0, newItem)
            }

            currentState.copy(
                items = updatedItems,
                lastScannedCode = cleanCode,
                lastScannedDesignation = defaultDesignation,
                statusMessage = "+$addQuantity ex : $defaultDesignation"
            )
        }

        // Sauvegarder automatiquement dans la base Room locale
        persistCurrentLot()
    }

    fun setQuickQuantity(qty: Int) {
        _uiState.update { it.copy(quickQuantity = qty) }
    }

    fun togglePromoMode(enabled: Boolean) {
        _uiState.update { it.copy(isPromoMode = enabled) }
    }

    fun setPromoPriceInput(price: String) {
        _uiState.update { it.copy(promoInputPrice = price) }
    }

    fun toggleTorch(): Boolean {
        val next = !_uiState.value.isTorchOn
        _uiState.update { it.copy(isTorchOn = next) }
        return next
    }

    fun incrementItemQuantity(itemId: String, delta: Int) {
        _uiState.update { currentState ->
            val updatedItems = currentState.items.mapNotNull { item ->
                if (item.id == itemId) {
                    val newQty = item.quantity + delta
                    if (newQty > 0) item.copy(quantity = newQty) else null
                } else {
                    item
                }
            }
            currentState.copy(items = updatedItems)
        }
        persistCurrentLot()
    }

    fun removeItem(itemId: String) {
        _uiState.update { currentState ->
            val updatedItems = currentState.items.filterNot { it.id == itemId }
            currentState.copy(items = updatedItems)
        }
        persistCurrentLot()
    }

    fun clearCurrentLot() {
        val newId = generateLotId()
        _uiState.update {
            it.copy(
                lotId = newId,
                items = emptyList(),
                statusMessage = "Nouveau lot initialisé"
            )
        }
        persistCurrentLot()
    }

    fun updateLotMetadata(name: String, operator: String, templateId: String) {
        sessionManager.setOperatorName(operator)
        sessionManager.setSelectedTemplateId(templateId)
        val template = AVAILABLE_TEMPLATES.find { it.id == templateId } ?: AVAILABLE_TEMPLATES[0]

        _uiState.update {
            it.copy(
                lotName = name,
                operatorName = operator,
                targetTemplateId = templateId,
                selectedTemplate = template
            )
        }
        persistCurrentLot()
    }

    private fun persistCurrentLot() {
        viewModelScope.launch {
            val state = _uiState.value
            val lot = MobileScanLot(
                id = state.lotId.ifBlank { generateLotId() },
                name = state.lotName,
                createdAt = nowIso(),
                updatedAt = nowIso(),
                operatorName = state.operatorName,
                deviceName = state.deviceName,
                deviceType = state.deviceType,
                status = "ready",
                targetTemplateId = state.targetTemplateId,
                syncMethod = "direct_lan",
                items = state.items
            )
            lotRepository.saveActiveLot(lot)
        }
    }

    /**
     * Transfert immédiat du lot au serveur d'impression E-Studio
     */
    fun transferLotToEStudio() {
        val state = _uiState.value
        if (state.items.isEmpty()) {
            viewModelScope.launch {
                _events.emit(ScanEvent.TransferError("Veuillez scanner au moins un article avant de transférer."))
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isTransferring = true, statusMessage = "Connexion au spooler E-Studio...") }

            val config = sessionManager.loadConfig()
            val lotToSend = MobileScanLot(
                id = state.lotId,
                name = state.lotName,
                createdAt = nowIso(),
                updatedAt = nowIso(),
                operatorName = state.operatorName,
                deviceName = state.deviceName,
                deviceType = state.deviceType,
                status = "ready",
                targetTemplateId = state.targetTemplateId,
                syncMethod = "direct_lan",
                items = state.items
            )

            val result = apiClient.sendLot(config, lotToSend)

            result.fold(
                onSuccess = { successMsg ->
                    lotRepository.markLotAsTransferred(lotToSend.id, nowIso())
                    _uiState.update {
                        it.copy(
                            isTransferring = false,
                            statusMessage = "Lot transmis avec succès au spooler !"
                        )
                    }
                    _events.emit(ScanEvent.TransferSuccess("Lot '${lotToSend.name}' transmis au serveur E-Studio (${state.totalLabelsCount} étiquettes) !"))
                    clearCurrentLot()
                },
                onFailure = { error ->
                    // Mode hors-ligne : sauvegardé localement en attente
                    _uiState.update {
                        it.copy(
                            isTransferring = false,
                            isLanConnected = false,
                            statusMessage = "Sauvegardé hors-ligne (${error.localizedMessage})"
                        )
                    }
                    _events.emit(ScanEvent.TransferError("Serveur E-Studio inaccessible : lot sauvegardé localement dans la base Room."))
                }
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        feedback.release()
    }
}
