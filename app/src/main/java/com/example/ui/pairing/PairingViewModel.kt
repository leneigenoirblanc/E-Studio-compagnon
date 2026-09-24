package com.example.ui.pairing

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.SessionManager
import com.example.data.remote.EStudioApiClient
import com.example.domain.model.EStudioInstance
import com.example.model.PairingConfig
import com.example.security.HandshakeValidator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PairingUiState(
    val serverHost: String = "192.168.1.100",
    val serverPort: String = "8080",
    val instanceId: String = "ESTUDIO-STORE-01",
    val token: String = "",
    val pin: String = "1234",
    val enteredPin: String = "",
    val isTestingConnection: Boolean = false,
    val isLoading: Boolean = false,
    val statusMessage: String? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isCameraPermissionGranted: Boolean = false,
    val discoveredInstances: List<EStudioInstance> = listOf(
        EStudioInstance(
            id = "inst_pc_caisse_01",
            name = "E-Studio Desktop (Caisse Centrale)",
            address = "192.168.1.45",
            port = 8080,
            isOnline = true,
            storeName = "Hypermarché Central"
        ),
        EStudioInstance(
            id = "inst_pc_reserve_02",
            name = "E-Studio Réserve & Stock",
            address = "192.168.1.52",
            port = 8080,
            isOnline = true,
            storeName = "Réserve Générale"
        ),
        EStudioInstance(
            id = "inst_pc_bureau_03",
            name = "E-Studio Direction / Admin",
            address = "192.168.1.88",
            port = 8080,
            isOnline = true,
            storeName = "Bureau Direction"
        )
    )
)

sealed interface PairingEvent {
    data object PairingCompleted : PairingEvent
    data object UnlockSuccess : PairingEvent
    data class Error(val message: String) : PairingEvent
}

class PairingViewModel(application: Application) : AndroidViewModel(application) {

    val sessionManager = SessionManager(application)
    private val apiClient = EStudioApiClient()

    private val _uiState = MutableStateFlow(PairingUiState())
    val uiState: StateFlow<PairingUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<PairingEvent>()
    val events: SharedFlow<PairingEvent> = _events.asSharedFlow()

    init {
        val saved = sessionManager.loadConfig()
        _uiState.update {
            it.copy(
                serverHost = saved.serverHost,
                serverPort = saved.serverPort.toString(),
                instanceId = saved.instanceId,
                token = saved.token,
                pin = saved.pin
            )
        }
    }

    /**
     * Traite le scan du QR code officiel d'appairage E-Studio
     */
    fun processScannedQrCode(qrContent: String) {
        _uiState.update { it.copy(isLoading = true, statusMessage = "Analyse du QR Code...", errorMessage = null) }
        viewModelScope.launch {
            val result = HandshakeValidator.parsePairingUrl(qrContent)
            result.fold(
                onSuccess = { config ->
                    sessionManager.savePairing(config)
                    _uiState.update {
                        it.copy(
                            serverHost = config.serverHost,
                            serverPort = config.serverPort.toString(),
                            instanceId = config.instanceId,
                            token = config.token,
                            pin = config.pin,
                            isLoading = false,
                            successMessage = "Poste E-Studio appairé avec succès !"
                        )
                    }
                    _events.emit(PairingEvent.PairingCompleted)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Format QR Code invalide : ${error.localizedMessage}"
                        )
                    }
                }
            )
        }
    }

    fun onQrScanned(qrContent: String) = processScannedQrCode(qrContent)

    /**
     * Connexion à une instance découverte sur le réseau local (style AnyDesk)
     */
    fun connectToDiscovered(instance: EStudioInstance) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    statusMessage = "Connexion à ${instance.name} (${instance.address})...",
                    errorMessage = null
                )
            }
            delay(500) // Simulation d'échange réseau mDNS/WebSocket
            val generatedToken = HandshakeValidator.sha256("${instance.address}:${instance.id}:${System.currentTimeMillis()}")
            val signature = HandshakeValidator.generateExpectedSignature(instance.id, generatedToken, "1234")

            val config = PairingConfig(
                serverHost = instance.address,
                serverPort = instance.port,
                instanceId = instance.name,
                token = generatedToken,
                pin = "1234",
                signature = signature,
                pairedAtTimestamp = System.currentTimeMillis(),
                isPaired = true,
                isLocked = false
            )

            sessionManager.savePairing(config)
            _uiState.update {
                it.copy(
                    serverHost = instance.address,
                    serverPort = instance.port.toString(),
                    instanceId = instance.name,
                    isLoading = false,
                    successMessage = "Connecté à ${instance.name} !"
                )
            }
            _events.emit(PairingEvent.PairingCompleted)
        }
    }

    /**
     * Connexion manuelle avec IP et Port
     */
    fun connectManual(host: String, port: Int) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    statusMessage = "Connexion à $host:$port...",
                    errorMessage = null
                )
            }
            delay(400)
            val instId = "ESTUDIO-PC-${host.replace(".", "")}"
            val generatedToken = HandshakeValidator.sha256("$host:$instId:${System.currentTimeMillis()}")
            val signature = HandshakeValidator.generateExpectedSignature(instId, generatedToken, "1234")

            val config = PairingConfig(
                serverHost = host,
                serverPort = port,
                instanceId = "Poste Desktop ($host)",
                token = generatedToken,
                pin = "1234",
                signature = signature,
                pairedAtTimestamp = System.currentTimeMillis(),
                isPaired = true,
                isLocked = false
            )

            sessionManager.savePairing(config)
            _uiState.update {
                it.copy(
                    serverHost = host,
                    serverPort = port.toString(),
                    instanceId = "Poste Desktop ($host)",
                    isLoading = false,
                    successMessage = "Connecté avec succès !"
                )
            }
            _events.emit(PairingEvent.PairingCompleted)
        }
    }

    /**
     * Rafraîchit la recherche d'instances sur le réseau local
     */
    fun refreshDiscovery() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, statusMessage = "Recherche d'instances sur le réseau...") }
            delay(600)
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    /**
     * Sauvegarde manuelle de l'appairage
     */
    fun submitManualPairing() {
        val state = _uiState.value
        val host = state.serverHost.trim()
        val port = state.serverPort.trim().toIntOrNull() ?: 8080
        val inst = state.instanceId.trim()
        val pin = state.pin.trim()

        if (host.isBlank() || inst.isBlank() || pin.length < 4) {
            _uiState.update { it.copy(errorMessage = "Veuillez renseigner tous les champs et un PIN à 4 chiffres.") }
            return
        }

        val generatedToken = HandshakeValidator.sha256("$host:$inst:${System.currentTimeMillis()}")
        val signature = HandshakeValidator.generateExpectedSignature(inst, generatedToken, pin)

        val config = PairingConfig(
            serverHost = host,
            serverPort = port,
            instanceId = inst,
            token = generatedToken,
            pin = pin,
            signature = signature,
            pairedAtTimestamp = System.currentTimeMillis(),
            isPaired = true,
            isLocked = false
        )

        sessionManager.savePairing(config)
        _uiState.update { it.copy(successMessage = "Configuration enregistrée !") }
        viewModelScope.launch {
            _events.emit(PairingEvent.PairingCompleted)
        }
    }

    /**
     * Déverrouillage par code PIN
     */
    fun verifyUnlockPin(entered: String): Boolean {
        val saved = sessionManager.loadConfig()
        val isValid = HandshakeValidator.verifyPin(entered, saved.pin)
        if (isValid) {
            sessionManager.setLocked(false)
            viewModelScope.launch {
                _events.emit(PairingEvent.UnlockSuccess)
            }
        }
        return isValid
    }

    fun updateHost(host: String) = _uiState.update { it.copy(serverHost = host, errorMessage = null) }
    fun updatePort(port: String) = _uiState.update { it.copy(serverPort = port, errorMessage = null) }
    fun updateInstance(inst: String) = _uiState.update { it.copy(instanceId = inst, errorMessage = null) }
    fun updatePin(pin: String) = _uiState.update { it.copy(pin = pin, errorMessage = null) }
    fun clearError() = _uiState.update { it.copy(errorMessage = null) }
}

