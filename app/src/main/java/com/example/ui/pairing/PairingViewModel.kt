package com.example.ui.pairing

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.SessionManager
import com.example.data.remote.EStudioApiClient
import com.example.model.PairingConfig
import com.example.security.HandshakeValidator
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
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isCameraPermissionGranted: Boolean = false
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
                        successMessage = "Poste E-Studio appairé avec succès !"
                    )
                }
                viewModelScope.launch {
                    _events.emit(PairingEvent.PairingCompleted)
                }
            },
            onFailure = { error ->
                _uiState.update {
                    it.copy(errorMessage = "Format QR Code invalide : ${error.localizedMessage}")
                }
            }
        )
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
