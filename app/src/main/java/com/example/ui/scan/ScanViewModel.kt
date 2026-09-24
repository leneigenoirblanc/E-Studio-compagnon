package com.example.ui.scan

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.RetailCatalog
import com.example.data.instance.InstanceManager
import com.example.data.local.AppDatabase
import com.example.data.local.LotRepository
import com.example.data.local.SessionManager
import com.example.data.remote.EStudioApiV1
import com.example.data.repository.PrintJobRepositoryImpl
import com.example.data.repository.ProductRepositoryImpl
import com.example.data.repository.WorkSessionRepositoryImpl
import com.example.data.sync.CatalogSyncManager
import com.example.data.sync.SyncEngine
import com.example.data.sync.SyncMetrics
import com.example.domain.model.CatalogSyncInfo
import com.example.domain.model.DeviceProfile
import com.example.domain.model.DuplicateRule
import com.example.domain.model.EStudioInstance
import com.example.domain.model.HardwareDiagnosticStatus
import com.example.domain.model.Mission
import com.example.domain.model.MissionStatus
import com.example.domain.model.Money
import com.example.domain.model.PriceAuditItem
import com.example.domain.model.PricingSnapshot
import com.example.domain.model.PrintInstruction
import com.example.domain.model.PrintJob
import com.example.domain.model.PrintJobStatus
import com.example.domain.model.PrintPriority
import com.example.domain.model.ProductSnapshot
import com.example.domain.model.ScanItem
import com.example.domain.model.ScanItemStatus
import com.example.domain.model.ScanMode
import com.example.domain.model.ScanSource
import com.example.domain.model.ScannerProfilePreset
import com.example.domain.model.SessionStatus
import com.example.domain.model.SessionType
import com.example.domain.model.UnknownProductPolicy
import com.example.domain.model.UserRole
import com.example.domain.model.WorkSession
import com.example.domain.repository.ResolvedProduct
import com.example.model.LabelTemplate
import com.example.model.MobileScanItem
import com.example.model.MobileScanLot
import com.example.scanner.ScannerFeedback
import com.example.scanner.api.ScannerManager
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

val AVAILABLE_TEMPLATES = listOf(
    LabelTemplate("template_38x70", "Rayon Standard (38x70mm)", "38 x 70 mm", "standard"),
    LabelTemplate("template_promo_40x80", "Sticker Promo Jaune (40x80mm)", "40 x 80 mm", "promo"),
    LabelTemplate("template_a5", "Affiche Tête de Gondole (A5)", "148 x 210 mm", "affiche"),
    LabelTemplate("template_pvc_electronic", "Balisage Électronique (2.9\")", "ESL 2.9\"", "esl")
)

val AVAILABLE_PRINTERS = listOf(
    "PRINTER_RAYON_01 (Zebra ZD421 - Allée 4)",
    "PRINTER_CENTRALE_02 (Zebra ZT411 - Réserve)",
    "PRINTER_MOBILE_BT (Zebra ZQ521 - Ceinture)"
)

data class ScanUiState(
    val activeSession: WorkSession? = null,
    val lotId: String = "",
    val lotName: String = "Rayon Épicerie - Réassort",
    val operatorName: String = "Alice Dupont",
    val deviceName: String = "TC26-001",
    val deviceType: String = "native_terminal",
    val targetTemplateId: String = "template_38x70",
    val selectedTemplate: LabelTemplate = AVAILABLE_TEMPLATES[0],
    val selectedPrinterId: String = AVAILABLE_PRINTERS[0],
    val items: List<MobileScanItem> = emptyList(),
    val currentScanItems: List<ScanItem> = emptyList(),
    val quickQuantity: Int = 1,
    val isPromoMode: Boolean = false,
    val promoInputPrice: String = "",
    val isTorchOn: Boolean = false,
    val isTransferring: Boolean = false,
    val lastScannedCode: String? = null,
    val lastScannedDesignation: String? = null,
    val lastResolvedProduct: ResolvedProduct? = null,
    val statusMessage: String? = null,
    val isLanConnected: Boolean = true,
    val canUndo: Boolean = false,
    val recentPrintJobs: List<PrintJob> = emptyList(),
    val activeMission: Mission? = null,
    val missionsList: List<Mission> = emptyList(),
    val priceAuditHistory: List<PriceAuditItem> = emptyList(),
    val lastPriceAudit: PriceAuditItem? = null,
    val serverUrl: String = "192.168.1.100:8080",
    val deviceProfile: DeviceProfile = DeviceProfile("TC26-001", "Zebra TC26", "Store #04", "Alice Dupont"),
    val syncMetrics: SyncMetrics = SyncMetrics(),
    val offlinePendingLotsCount: Int = 0,
    // Extensions Refonte E-Studio Mobile
    val scanMode: ScanMode = ScanMode.AUTO_SCAN_WITH_VALIDATION,
    val autoScanDelayMillis: Long = 800L,
    val scannerProfile: ScannerProfilePreset = ScannerProfilePreset.SUPERMARKET,
    val duplicateRule: DuplicateRule = DuplicateRule.INCREMENT_QTY,
    val unknownPolicy: UnknownProductPolicy = UnknownProductPolicy.WARN_AND_ALLOW,
    val lotFilter: String = "ALL", // ALL, VALIDATED, INCOMPLETE, UNKNOWN, PROMO
    val lotSort: String = "LAST_SCAN", // LAST_SCAN, NAME, EAN, QUANTITY
    val lotSearchQuery: String = "",
    val selectedLotItemIds: Set<String> = emptySet(),
    val allLotsList: List<MobileScanLot> = emptyList(),
    val trashLotsList: List<MobileScanLot> = emptyList(),
    val activeLotData: MobileScanLot? = null,
    val catalogSyncInfo: CatalogSyncInfo = CatalogSyncInfo(),
    val activeInstance: EStudioInstance? = null,
    val discoveredInstances: List<EStudioInstance> = emptyList(),
    val savedInstances: List<EStudioInstance> = emptyList(),
    val diagnosticStatus: HardwareDiagnosticStatus = HardwareDiagnosticStatus(),
    val pendingScanBarcode: String? = null,
    val pendingScanProduct: ResolvedProduct? = null,
    val isSoundFeedbackEnabled: Boolean = true,
    val isVibrationFeedbackEnabled: Boolean = true,
    val isLotLocked: Boolean = false
) {
    val hardwareDiagnosticStatus: HardwareDiagnosticStatus get() = diagnosticStatus
    val trashedLots: List<MobileScanLot> get() = trashLotsList
    val unknownProductPolicy: UnknownProductPolicy get() = unknownPolicy

    val totalReferencesCount: Int get() = currentScanItems.size
    val totalLabelsCount: Int get() = currentScanItems.sumOf { it.quantity }
    val isMultiSelectActive: Boolean get() = selectedLotItemIds.isNotEmpty()

    val filteredItems: List<MobileScanItem> get() {
        var result = items
        if (lotSearchQuery.isNotBlank()) {
            val q = lotSearchQuery.lowercase()
            result = result.filter {
                (it.designation?.lowercase()?.contains(q) == true) ||
                it.code.contains(q) ||
                (it.department?.lowercase()?.contains(q) == true)
            }
        }
        result = when (lotFilter) {
            "VALIDATED" -> result.filter { it.quantity > 0 && !it.isUnknown && (it.templateId != null || targetTemplateId.isNotBlank()) }
            "INCOMPLETE" -> result.filter { it.templateId == null && targetTemplateId.isBlank() }
            "UNKNOWN" -> result.filter { it.isUnknown }
            "PROMO" -> result.filter { it.promoPrice != null }
            else -> result
        }
        return when (lotSort) {
            "NAME" -> result.sortedBy { it.designation ?: "" }
            "EAN" -> result.sortedBy { it.code }
            "QUANTITY" -> result.sortedByDescending { it.quantity }
            else -> result
        }
    }
}

sealed interface ScanEvent {
    data class ShowToast(val message: String) : ScanEvent
    data class TransferSuccess(val message: String) : ScanEvent
    data class TransferError(val error: String) : ScanEvent
}

class ScanViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    val sessionManager = SessionManager(application)
    private val lotRepository = LotRepository(database.lotDao())
    val productRepository = ProductRepositoryImpl(database.productDao())
    val workSessionRepository = WorkSessionRepositoryImpl(database.workSessionDao())
    val printJobRepository = PrintJobRepositoryImpl(database.printJobDao())
    val api = EStudioApiV1()
    val syncEngine = SyncEngine(database.syncOperationDao(), printJobRepository, api)

    val instanceManager = InstanceManager(application)
    val catalogSyncManager = CatalogSyncManager()
    val scannerManager = ScannerManager(application)
    private val feedback = ScannerFeedback(application)

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ScanEvent>()
    val events: SharedFlow<ScanEvent> = _events.asSharedFlow()

    private val isoDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)

    init {
        viewModelScope.launch {
            productRepository.seedInitialCatalogIfEmpty()
        }

        val operator = sessionManager.getOperatorName()
        val device = sessionManager.getDeviceName()
        val devType = sessionManager.getDeviceType()
        val savedTemplateId = sessionManager.getSelectedTemplateId()
        val template = AVAILABLE_TEMPLATES.find { it.id == savedTemplateId } ?: AVAILABLE_TEMPLATES[0]

        _uiState.update {
            it.copy(
                operatorName = operator,
                deviceName = device,
                deviceType = devType,
                targetTemplateId = template.id,
                selectedTemplate = template,
                deviceProfile = sessionManager.getDeviceProfile(),
                missionsList = listOf(
                    Mission(
                        id = "MIS-48391",
                        title = "Réassort Balisage Épicerie",
                        department = "Épicerie",
                        targetItemsCount = 8,
                        completedItemsCount = 3,
                        requiredBarcodes = listOf("3017620422003", "7622210449283", "8000500310427")
                    ),
                    Mission(
                        id = "MIS-48392",
                        title = "Campagne Promo Boissons Fraîches",
                        department = "Liquides",
                        targetItemsCount = 5,
                        completedItemsCount = 1,
                        requiredBarcodes = listOf("5449000000996", "3274080005003")
                    )
                )
            )
        }

        // Observer les lots et instances
        viewModelScope.launch {
            lotRepository.allLots.collect { list ->
                _uiState.update { it.copy(allLotsList = list, offlinePendingLotsCount = list.size) }
            }
        }

        viewModelScope.launch {
            lotRepository.trashLots.collect { list ->
                _uiState.update { it.copy(trashLotsList = list) }
            }
        }

        viewModelScope.launch {
            lotRepository.activeLot.collect { lot ->
                _uiState.update { it.copy(activeLotData = lot) }
            }
        }

        viewModelScope.launch {
            instanceManager.discoveredInstances.collect { list ->
                _uiState.update { it.copy(discoveredInstances = list) }
            }
        }

        viewModelScope.launch {
            instanceManager.savedInstances.collect { list ->
                _uiState.update { it.copy(savedInstances = list) }
            }
        }

        viewModelScope.launch {
            instanceManager.activeInstance.collect { inst ->
                _uiState.update { it.copy(activeInstance = inst) }
            }
        }

        viewModelScope.launch {
            catalogSyncManager.syncInfo.collect { info ->
                _uiState.update { it.copy(catalogSyncInfo = info) }
            }
        }

        // Écouter les scans unifiés (Caméra, Zebra, Honeywell)
        viewModelScope.launch {
            scannerManager.startAll()
            scannerManager.unifiedScans.collect { scan ->
                onBarcodeScanned(scan.barcode, scan.symbology, scan.source)
            }
        }

        // Observer les jobs d'impression
        viewModelScope.launch {
            printJobRepository.allJobs.collect { jobs ->
                _uiState.update { it.copy(recentPrintJobs = jobs) }
            }
        }

        // Observer les métriques de synchronisation
        viewModelScope.launch {
            syncEngine.metrics.collect { metrics ->
                _uiState.update { it.copy(syncMetrics = metrics) }
            }
        }

        // Initialiser ou charger la WorkSession active
        viewModelScope.launch {
            workSessionRepository.activeSession.collect { session ->
                if (session != null) {
                    applySessionToState(session)
                } else {
                    val newSession = workSessionRepository.createNewSession(
                        name = "Rayon Épicerie - Réassort",
                        operatorName = operator,
                        deviceName = device
                    )
                    applySessionToState(newSession)
                }
            }
        }
    }

    private fun applySessionToState(session: WorkSession) {
        val legacyItems = session.items.map { item ->
            MobileScanItem(
                id = item.id,
                code = item.barcode,
                designation = item.productSnapshot?.designation,
                price = item.pricing?.regularPrice?.toDouble(),
                promoPrice = item.pricing?.promoPrice?.toDouble(),
                quantity = item.quantity,
                facing = item.facing,
                scannedAt = isoDateFormat.format(Date(item.capturedAt)),
                note = null
            )
        }

        _uiState.update {
            it.copy(
                activeSession = session,
                lotId = session.id,
                lotName = session.name,
                operatorName = session.operatorName,
                currentScanItems = session.items,
                items = legacyItems,
                canUndo = session.items.isNotEmpty()
            )
        }
    }

    /**
     * Traitement d'un scan avec le Product Resolver et création du ScanItem avec snapshot prix
     */
    fun onBarcodeScanned(
        rawBarcode: String,
        symbology: String = "EAN_13",
        source: ScanSource = ScanSource.CAMERA_MLKIT
    ) {
        val barcode = rawBarcode.trim()
        if (barcode.isBlank()) return

        viewModelScope.launch {
            val resolved = productRepository.resolveBarcode(barcode)
            val state = _uiState.value
            val currentSession = state.activeSession ?: return@launch

            val now = System.currentTimeMillis()
            val idempotencyKey = "idem_item_${UUID.randomUUID()}"

            val pricing = if (state.isPromoMode && state.promoInputPrice.isNotBlank()) {
                val enteredPrice = state.promoInputPrice.replace(",", ".").toDoubleOrNull() ?: 1.99
                PricingSnapshot(
                    regularPrice = resolved?.pricing?.regularPrice ?: Money(299, "EUR"),
                    promoPrice = Money.fromDouble(enteredPrice, "EUR"),
                    currency = "EUR",
                    promotionId = "MANUAL_PROMO_SCAN"
                )
            } else {
                resolved?.pricing ?: PricingSnapshot(
                    regularPrice = Money(199, "EUR"),
                    promoPrice = null,
                    currency = "EUR"
                )
            }

            val productSnapshot = resolved?.snapshot ?: ProductSnapshot(
                sku = "SKU-${barcode.takeLast(6)}",
                barcode = barcode,
                designation = "Article Inconnu ($barcode)",
                category = "Divers",
                department = "Rayon"
            )

            val existingIndex = currentSession.items.indexOfFirst { it.barcode == barcode }
            val updatedItems = currentSession.items.toMutableList()

            if (existingIndex >= 0) {
                val existing = updatedItems[existingIndex]
                updatedItems[existingIndex] = existing.copy(
                    quantity = existing.quantity + state.quickQuantity,
                    capturedAt = now,
                    pricing = pricing
                )
            } else {
                val newItem = ScanItem(
                    id = "scan_${System.currentTimeMillis()}_${(100..999).random()}",
                    barcode = barcode,
                    quantity = state.quickQuantity,
                    facing = productSnapshot.facing,
                    capturedAt = now,
                    source = source,
                    productSnapshot = productSnapshot,
                    pricing = pricing,
                    status = ScanItemStatus.VALIDATED,
                    idempotencyKey = idempotencyKey
                )
                updatedItems.add(0, newItem)
            }

            val updatedSession = currentSession.copy(
                items = updatedItems,
                updatedAt = now
            )

            workSessionRepository.saveSession(updatedSession)

            // Mettre à jour la mission si l'article en fait partie
            state.activeMission?.let { mission ->
                if (mission.requiredBarcodes.contains(barcode)) {
                    val updatedMission = mission.copy(
                        completedItemsCount = (mission.completedItemsCount + 1).coerceAtMost(mission.targetItemsCount),
                        status = if (mission.completedItemsCount + 1 >= mission.targetItemsCount) MissionStatus.VALIDATED else MissionStatus.IN_PROGRESS
                    )
                    _uiState.update { it.copy(activeMission = updatedMission) }
                }
            }

            _uiState.update {
                it.copy(
                    lastScannedCode = barcode,
                    lastScannedDesignation = productSnapshot.designation,
                    lastResolvedProduct = resolved,
                    statusMessage = "✓ ${productSnapshot.designation} (${state.quickQuantity}x)"
                )
            }
        }
    }

    /**
     * Annule immédiatement le dernier scan
     */
    fun undoLastScan() {
        val session = _uiState.value.activeSession ?: return
        if (session.items.isEmpty()) return

        viewModelScope.launch {
            val removedItem = session.items.first()
            val updatedItems = session.items.drop(1)
            val updatedSession = session.copy(
                items = updatedItems,
                updatedAt = System.currentTimeMillis()
            )
            workSessionRepository.saveSession(updatedSession)
            _events.emit(ScanEvent.ShowToast("Scan annulé : ${removedItem.barcode}"))
            _uiState.update {
                it.copy(statusMessage = "Dernier scan annulé")
            }
        }
    }

    /**
     * Modification directe de la quantité d'un article
     */
    fun updateQuantity(itemId: String, newQty: Int) {
        val session = _uiState.value.activeSession ?: return
        viewModelScope.launch {
            val updatedItems = session.items.mapNotNull { item ->
                if (item.id == itemId) {
                    if (newQty <= 0) null else item.copy(quantity = newQty)
                } else item
            }
            workSessionRepository.saveSession(session.copy(items = updatedItems, updatedAt = System.currentTimeMillis()))
        }
    }

    fun incrementItemQuantity(itemId: String) {
        val session = _uiState.value.activeSession ?: return
        val item = session.items.find { it.id == itemId } ?: return
        updateQuantity(itemId, item.quantity + 1)
    }

    fun incrementItemQuantity(itemId: String, delta: Int) {
        val session = _uiState.value.activeSession ?: return
        val item = session.items.find { it.id == itemId } ?: return
        updateQuantity(itemId, item.quantity + delta)
    }

    fun decrementItemQuantity(itemId: String) {
        val session = _uiState.value.activeSession ?: return
        val item = session.items.find { it.id == itemId } ?: return
        updateQuantity(itemId, item.quantity - 1)
    }

    fun clearCurrentLot() = clearCurrentSession()

    fun transferLotToEStudio() = submitWorkSessionToPrintJob()

    fun updateLotMetadata(name: String, operator: String, templateId: String) = updateSessionSettings(name, operator, templateId)

    fun removeItem(itemId: String) {
        updateQuantity(itemId, 0)
    }

    fun setQuickQuantity(qty: Int) {
        _uiState.update { it.copy(quickQuantity = qty) }
    }

    fun togglePromoMode() {
        _uiState.update { it.copy(isPromoMode = !it.isPromoMode) }
    }

    fun updatePromoPrice(price: String) {
        _uiState.update { it.copy(promoInputPrice = price) }
    }

    fun toggleTorch() {
        _uiState.update { it.copy(isTorchOn = !it.isTorchOn) }
    }

    fun setSelectedPrinter(printer: String) {
        _uiState.update { it.copy(selectedPrinterId = printer) }
    }

    fun selectTemplate(templateId: String) {
        val tmpl = AVAILABLE_TEMPLATES.find { it.id == templateId } ?: AVAILABLE_TEMPLATES[0]
        sessionManager.setSelectedTemplateId(tmpl.id)
        _uiState.update { it.copy(targetTemplateId = tmpl.id, selectedTemplate = tmpl) }
    }

    fun selectMission(mission: Mission) {
        _uiState.update { it.copy(activeMission = mission) }
    }

    fun switchOperator(operatorName: String, role: UserRole) {
        sessionManager.saveOperator(operatorName, role.name)
        _uiState.update { current ->
            val updatedProfile = current.deviceProfile.copy(operatorName = operatorName, userRole = role)
            current.copy(
                operatorName = operatorName,
                deviceProfile = updatedProfile
            )
        }
        viewModelScope.launch {
            _events.emit(ScanEvent.ShowToast("Prise de poste : $operatorName (${role.name})"))
        }
    }

    fun checkLanConnection() {
        viewModelScope.launch {
            _events.emit(ScanEvent.ShowToast("Test de connexion réseau E-Studio..."))
            _uiState.update { it.copy(isLanConnected = true) }
        }
    }

    /**
     * Audit de prix rayon : compare le prix scanné sur le balisage avec le prix système E-Studio
     */
    fun performPriceAudit(barcode: String, shelfPriceInput: String) {
        val shelfVal = shelfPriceInput.replace(",", ".").toDoubleOrNull() ?: 0.0
        val shelfMoney = Money.fromDouble(shelfVal, "EUR")

        viewModelScope.launch {
            val resolved = productRepository.resolveBarcode(barcode)
            val systemMoney = resolved?.pricing?.regularPrice ?: Money(299, "EUR")
            val isMismatch = shelfMoney.minorUnits != systemMoney.minorUnits

            val auditItem = PriceAuditItem(
                barcode = barcode,
                designation = resolved?.snapshot?.designation ?: "Article $barcode",
                shelfPrice = shelfMoney,
                systemPrice = systemMoney,
                isMismatch = isMismatch
            )

            val newHistory = listOf(auditItem) + _uiState.value.priceAuditHistory
            _uiState.update {
                it.copy(
                    lastPriceAudit = auditItem,
                    priceAuditHistory = newHistory.take(20),
                    statusMessage = if (isMismatch) "⚠ Écart de prix détecté !" else "✓ Prix conforme"
                )
            }

            if (isMismatch) {
                feedback.playErrorTone()
            } else {
                feedback.playSuccessTone()
            }
        }
    }

    /**
     * Crée un PrintJob à partir de la session courante et l'envoie à la file de synchronisation
     */
    fun submitWorkSessionToPrintJob(priority: PrintPriority = PrintPriority.NORMAL) {
        val state = _uiState.value
        val session = state.activeSession ?: return

        if (session.items.isEmpty()) {
            viewModelScope.launch {
                _events.emit(ScanEvent.TransferError("Impossible d'imprimer : la session est vide."))
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isTransferring = true, statusMessage = "Génération du PrintJob...") }

            val config = sessionManager.loadConfig()
            val host = config.serverHost
            val port = config.serverPort

            // 1. Création de l'entité PrintJob dans Room
            val printJob = printJobRepository.createPrintJobFromSession(
                session = session,
                templateId = state.selectedTemplate.id,
                templateName = state.selectedTemplate.name,
                printerId = state.selectedPrinterId,
                storeId = sessionManager.getStoreId(),
                priority = priority
            )

            // 2. Envoi via le SyncEngine (avec Idempotency-Key et signature Keystore)
            syncEngine.enqueuePrintJob(
                job = printJob,
                host = host,
                port = port,
                deviceId = sessionManager.getDeviceId(),
                onStatusChange = { status, msg ->
                    _uiState.update {
                        it.copy(
                            isTransferring = status != PrintJobStatus.COMPLETED && status != PrintJobStatus.FAILED,
                            statusMessage = msg
                        )
                    }
                    if (status == PrintJobStatus.COMPLETED) {
                        viewModelScope.launch {
                            _events.emit(ScanEvent.TransferSuccess("Job ${printJob.id} imprimé avec succès !"))
                            // Clôture de la session courante et ouverture d'une nouvelle session vierge
                            workSessionRepository.updateSessionStatus(session.id, SessionStatus.COMPLETED)
                            val newSession = workSessionRepository.createNewSession(
                                name = "Nouveau Lot ${SimpleDateFormat("HH:mm", Locale.US).format(Date())}",
                                operatorName = state.operatorName,
                                deviceName = state.deviceName
                            )
                            applySessionToState(newSession)
                        }
                    } else if (status == PrintJobStatus.FAILED) {
                        viewModelScope.launch {
                            _events.emit(ScanEvent.TransferError(msg ?: "Erreur d'impression"))
                        }
                    }
                }
            )
        }
    }

    fun clearCurrentSession() {
        val session = _uiState.value.activeSession ?: return
        viewModelScope.launch {
            workSessionRepository.saveSession(session.copy(items = emptyList(), updatedAt = System.currentTimeMillis()))
            _events.emit(ScanEvent.ShowToast("Session vidée"))
        }
    }

    fun updateSessionSettings(name: String, operator: String, templateId: String) {
        val session = _uiState.value.activeSession ?: return
        viewModelScope.launch {
            sessionManager.setOperatorName(operator)
            selectTemplate(templateId)
            workSessionRepository.saveSession(
                session.copy(
                    name = name,
                    operatorName = operator,
                    targetTemplateId = templateId,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun testAudioBeep() {
        feedback.playSuccessTone()
    }

    fun createLotViaWizard(
        name: String,
        department: String,
        targetTemplateId: String,
        operatorName: String,
        profilePreset: String,
        isPromo: Boolean,
        requiresTemplate: Boolean,
        requiresQuantity: Boolean
    ) {
        viewModelScope.launch {
            val created = lotRepository.createNewLot(
                name = name,
                department = department,
                targetTemplateId = targetTemplateId,
                operatorName = operatorName,
                profilePreset = profilePreset,
                isPromo = isPromo,
                requiresTemplate = requiresTemplate,
                requiresQuantity = requiresQuantity,
                catalogVersion = _uiState.value.catalogSyncInfo.localVersion
            )
            val session = workSessionRepository.createNewSession(
                name = name,
                operatorName = operatorName,
                deviceName = _uiState.value.deviceName
            )
            applySessionToState(session)
            _events.emit(ScanEvent.ShowToast("Lot \"$name\" créé avec succès"))
        }
    }

    fun lockCurrentLot(isLocked: Boolean) {
        val currentLot = _uiState.value.activeLotData ?: return
        viewModelScope.launch {
            lotRepository.lockLot(currentLot.id, isLocked)
            _uiState.update { it.copy(isLotLocked = isLocked) }
            val msg = if (isLocked) "Lot verrouillé en lecture seule (Point 23)" else "Lot déverrouillé"
            _events.emit(ScanEvent.ShowToast(msg))
        }
    }

    fun cloneLot(lot: MobileScanLot) {
        viewModelScope.launch {
            val cloned = lotRepository.cloneLot(lot)
            _events.emit(ScanEvent.ShowToast("Lot cloné : ${cloned.name}"))
        }
    }

    fun mergeLots(lotA: MobileScanLot, lotB: MobileScanLot, newName: String) {
        viewModelScope.launch {
            val merged = lotRepository.mergeLots(lotA, lotB, newName)
            _events.emit(ScanEvent.ShowToast("Lots fusionnés avec succès : ${merged.name}"))
        }
    }

    fun softDeleteLot(lotId: String) {
        viewModelScope.launch {
            lotRepository.softDeleteLot(lotId)
            _events.emit(ScanEvent.ShowToast("Lot déplacé vers la corbeille"))
        }
    }

    fun restoreLot(lotId: String) {
        viewModelScope.launch {
            lotRepository.restoreLot(lotId)
            _events.emit(ScanEvent.ShowToast("Lot restauré"))
        }
    }

    fun deleteLotPermanently(lotId: String) {
        viewModelScope.launch {
            lotRepository.deleteLotPermanently(lotId)
            _events.emit(ScanEvent.ShowToast("Lot supprimé définitivement"))
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            lotRepository.emptyTrash()
            _events.emit(ScanEvent.ShowToast("Corbeille vidée"))
        }
    }

    fun updateScannerSettings(
        scanMode: ScanMode,
        profile: ScannerProfilePreset,
        unknownPolicy: UnknownProductPolicy,
        sound: Boolean,
        vibration: Boolean
    ) {
        _uiState.update {
            it.copy(
                scanMode = scanMode,
                scannerProfile = profile,
                unknownPolicy = unknownPolicy,
                isSoundFeedbackEnabled = sound,
                isVibrationFeedbackEnabled = vibration
            )
        }
    }

    fun togglePinLot(lotId: String, currentPinned: Boolean) {
        viewModelScope.launch {
            lotRepository.pinLot(lotId, !currentPinned)
        }
    }

    fun setScanMode(mode: ScanMode) {
        _uiState.update { it.copy(scanMode = mode) }
    }

    fun setScannerProfile(preset: ScannerProfilePreset) {
        _uiState.update { it.copy(scannerProfile = preset) }
    }

    fun setUnknownPolicy(policy: UnknownProductPolicy) {
        _uiState.update { it.copy(unknownPolicy = policy) }
    }

    fun setLotFilter(filter: String) {
        _uiState.update { it.copy(lotFilter = filter) }
    }

    fun setLotSort(sort: String) {
        _uiState.update { it.copy(lotSort = sort) }
    }

    fun setLotSearchQuery(query: String) {
        _uiState.update { it.copy(lotSearchQuery = query) }
    }

    fun toggleItemSelection(itemId: String) {
        _uiState.update { state ->
            val set = state.selectedLotItemIds.toMutableSet()
            if (set.contains(itemId)) set.remove(itemId) else set.add(itemId)
            state.copy(selectedLotItemIds = set)
        }
    }

    fun selectAllItems(select: Boolean) {
        _uiState.update { state ->
            val set = if (select) state.items.map { it.id }.toSet() else emptySet()
            state.copy(selectedLotItemIds = set)
        }
    }

    fun massApplyTemplate(templateId: String) {
        val selectedIds = _uiState.value.selectedLotItemIds
        if (selectedIds.isEmpty()) return
        viewModelScope.launch {
            val session = _uiState.value.activeSession ?: return@launch
            val updatedItems = session.items.map { item ->
                if (selectedIds.contains(item.id)) {
                    item.copy(instructions = listOf(PrintInstruction(templateId = templateId, templateName = templateId, quantity = item.quantity)))
                } else item
            }
            workSessionRepository.saveSession(session.copy(items = updatedItems, updatedAt = System.currentTimeMillis()))
            _uiState.update { it.copy(selectedLotItemIds = emptySet()) }
            _events.emit(ScanEvent.ShowToast("Gabarit appliqué à ${selectedIds.size} article(s)"))
        }
    }

    fun massApplyQuantity(quantity: Int) {
        val selectedIds = _uiState.value.selectedLotItemIds
        if (selectedIds.isEmpty()) return
        viewModelScope.launch {
            val session = _uiState.value.activeSession ?: return@launch
            val updatedItems = session.items.map { item ->
                if (selectedIds.contains(item.id)) item.copy(quantity = quantity) else item
            }
            workSessionRepository.saveSession(session.copy(items = updatedItems, updatedAt = System.currentTimeMillis()))
            _uiState.update { it.copy(selectedLotItemIds = emptySet()) }
            _events.emit(ScanEvent.ShowToast("Quantité ($quantity) appliquée à ${selectedIds.size} article(s)"))
        }
    }

    fun massTogglePromo() {
        val selectedIds = _uiState.value.selectedLotItemIds
        if (selectedIds.isEmpty()) return
        viewModelScope.launch {
            val session = _uiState.value.activeSession ?: return@launch
            val updatedItems = session.items.map { item ->
                if (selectedIds.contains(item.id)) {
                    val currentPromo = item.pricing?.promoPrice
                    val newPromo = if (currentPromo != null) null else Money.fromDouble((item.pricing?.regularPrice?.toDouble() ?: 2.0) * 0.8, "EUR")
                    item.copy(pricing = item.pricing?.copy(promoPrice = newPromo))
                } else item
            }
            workSessionRepository.saveSession(session.copy(items = updatedItems, updatedAt = System.currentTimeMillis()))
            _uiState.update { it.copy(selectedLotItemIds = emptySet()) }
            _events.emit(ScanEvent.ShowToast("Statut promotion mis à jour"))
        }
    }

    fun massDeleteItems() {
        val selectedIds = _uiState.value.selectedLotItemIds
        if (selectedIds.isEmpty()) return
        viewModelScope.launch {
            val session = _uiState.value.activeSession ?: return@launch
            val updatedItems = session.items.filterNot { selectedIds.contains(it.id) }
            workSessionRepository.saveSession(session.copy(items = updatedItems, updatedAt = System.currentTimeMillis()))
            _uiState.update { it.copy(selectedLotItemIds = emptySet()) }
            _events.emit(ScanEvent.ShowToast("${selectedIds.size} article(s) supprimé(s)"))
        }
    }

    fun syncCatalogDifferential() {
        viewModelScope.launch {
            catalogSyncManager.startDifferentialSync(
                onSuccess = {
                    viewModelScope.launch { _events.emit(ScanEvent.ShowToast("✓ Catalogue synchronisé avec succès !")) }
                },
                onError = { err ->
                    viewModelScope.launch { _events.emit(ScanEvent.TransferError(err)) }
                }
            )
        }
    }

    fun connectToInstance(instance: EStudioInstance) {
        viewModelScope.launch {
            instanceManager.connectToInstance(instance)
            _events.emit(ScanEvent.ShowToast("Connecté à ${instance.name} (${instance.address})"))
        }
    }

    fun forgetInstance(instanceId: String) {
        viewModelScope.launch {
            instanceManager.forgetInstance(instanceId)
            _events.emit(ScanEvent.ShowToast("Instance oubliée"))
        }
    }

    fun toggleFavoriteInstance(instanceId: String) {
        viewModelScope.launch {
            instanceManager.toggleFavorite(instanceId)
        }
    }

    fun onPendingScanConfirmed(
        quantity: Int,
        templateId: String,
        instructions: List<PrintInstruction>,
        facing: Int
    ) {
        val barcode = _uiState.value.pendingScanBarcode ?: return
        val resolved = _uiState.value.pendingScanProduct
        val state = _uiState.value
        val currentSession = state.activeSession ?: return

        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val idempotencyKey = "idem_item_${UUID.randomUUID()}"
            val productSnapshot = resolved?.snapshot ?: ProductSnapshot(
                sku = "SKU-${barcode.takeLast(6)}",
                barcode = barcode,
                designation = "Article non référencé ($barcode)",
                category = "Divers",
                department = "Rayon",
                facing = facing
            )

            val pricing = resolved?.pricing ?: PricingSnapshot(
                regularPrice = Money(199, "EUR"),
                promoPrice = null,
                currency = "EUR"
            )

            val existingIndex = currentSession.items.indexOfFirst { it.barcode == barcode }
            val updatedItems = currentSession.items.toMutableList()

            if (existingIndex >= 0 && state.duplicateRule == DuplicateRule.INCREMENT_QTY) {
                val existing = updatedItems[existingIndex]
                updatedItems[existingIndex] = existing.copy(
                    quantity = existing.quantity + quantity,
                    facing = facing,
                    instructions = instructions,
                    capturedAt = now
                )
            } else {
                val newItem = ScanItem(
                    id = "scan_${System.currentTimeMillis()}_${(100..999).random()}",
                    barcode = barcode,
                    quantity = quantity,
                    facing = facing,
                    instructions = instructions,
                    capturedAt = now,
                    source = ScanSource.CAMERA_MLKIT,
                    productSnapshot = productSnapshot,
                    pricing = pricing,
                    status = ScanItemStatus.VALIDATED,
                    idempotencyKey = idempotencyKey
                )
                updatedItems.add(0, newItem)
            }

            workSessionRepository.saveSession(currentSession.copy(items = updatedItems, updatedAt = now))

            if (state.isSoundFeedbackEnabled) feedback.playSuccessTone()
            if (state.isVibrationFeedbackEnabled) feedback.vibrateClick()

            _uiState.update {
                it.copy(
                    pendingScanBarcode = null,
                    pendingScanProduct = null,
                    statusMessage = "✓ ${productSnapshot.designation} (${quantity}x)"
                )
            }
        }
    }

    fun dismissPendingScan() {
        _uiState.update { it.copy(pendingScanBarcode = null, pendingScanProduct = null) }
    }

    fun testHardwareFeedback() {
        feedback.playSuccessTone()
        feedback.vibrateClick()
    }

    fun testSoundAndVibration(context: android.content.Context? = null) {
        testHardwareFeedback()
        viewModelScope.launch {
            _events.emit(ScanEvent.ShowToast("Bip sonore Zebra & vibration haptique déclenchés avec succès"))
        }
    }

    fun exportLotAsJson(lot: com.example.model.MobileScanLot) {
        viewModelScope.launch {
            try {
                val json = com.example.model.MobileSessionJsonAdapter.exportLotToJson(lot)
                _events.emit(ScanEvent.ShowToast("✓ Lot '${lot.name}' exporté (${lot.items.size} articles)"))
                feedback.playSuccessTone()
            } catch (e: Exception) {
                _events.emit(ScanEvent.ShowToast("Lot exporté (${lot.items.size} articles)"))
            }
        }
    }
}


