package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import com.example.model.PairingConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("estudio_prefs", Context.MODE_PRIVATE)

    private val _pairingState = MutableStateFlow(loadConfig())
    val pairingState: StateFlow<PairingConfig> = _pairingState.asStateFlow()

    private val _isLocked = MutableStateFlow(prefs.getBoolean(KEY_IS_LOCKED, false))
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    fun loadConfig(): PairingConfig {
        val host = prefs.getString(KEY_HOST, "192.168.1.100") ?: "192.168.1.100"
        val port = prefs.getInt(KEY_PORT, 8080)
        val inst = prefs.getString(KEY_INST, "ESTUDIO-CENTRAL-01") ?: "ESTUDIO-CENTRAL-01"
        val tok = prefs.getString(KEY_TOK, "") ?: ""
        val pin = prefs.getString(KEY_PIN, "1234") ?: "1234"
        val sig = prefs.getString(KEY_SIG, "") ?: ""
        val pairedAt = prefs.getLong(KEY_PAIRED_AT, 0L)
        val isPaired = prefs.getBoolean(KEY_IS_PAIRED, false)

        return PairingConfig(
            serverHost = host,
            serverPort = port,
            instanceId = inst,
            token = tok,
            pin = pin,
            signature = sig,
            pairedAtTimestamp = pairedAt,
            isPaired = isPaired,
            isLocked = false
        )
    }

    fun savePairing(config: PairingConfig) {
        prefs.edit()
            .putString(KEY_HOST, config.serverHost)
            .putInt(KEY_PORT, config.serverPort)
            .putString(KEY_INST, config.instanceId)
            .putString(KEY_TOK, config.token)
            .putString(KEY_PIN, config.pin)
            .putString(KEY_SIG, config.signature)
            .putLong(KEY_PAIRED_AT, config.pairedAtTimestamp)
            .putBoolean(KEY_IS_PAIRED, config.isPaired)
            .putBoolean(KEY_IS_LOCKED, false)
            .apply()

        _pairingState.value = config
        _isLocked.value = false
    }

    fun setLocked(locked: Boolean) {
        prefs.edit().putBoolean(KEY_IS_LOCKED, locked).apply()
        _isLocked.value = locked
    }

    fun resetPairing() {
        prefs.edit().clear().apply()
        _pairingState.value = PairingConfig()
        _isLocked.value = false
    }

    fun getOperatorName(): String {
        return prefs.getString(KEY_OPERATOR, "Agent Rayon - 04") ?: "Agent Rayon - 04"
    }

    fun setOperatorName(name: String) {
        prefs.edit().putString(KEY_OPERATOR, name).apply()
    }

    fun getDeviceName(): String {
        val defaultModel = if (Build.MANUFACTURER.contains("Zebra", ignoreCase = true) || Build.MODEL.contains("TC", ignoreCase = true)) {
            "Zebra ${Build.MODEL}"
        } else {
            "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}"
        }
        return prefs.getString(KEY_DEVICE_NAME, defaultModel) ?: defaultModel
    }

    fun setDeviceName(name: String) {
        prefs.edit().putString(KEY_DEVICE_NAME, name).apply()
    }

    fun getDeviceType(): String {
        val isZebra = Build.MANUFACTURER.contains("Zebra", ignoreCase = true) || Build.MODEL.contains("TC", ignoreCase = true)
        val defaultType = if (isZebra) "native_terminal" else "android"
        return prefs.getString(KEY_DEVICE_TYPE, defaultType) ?: defaultType
    }

    fun getSelectedTemplateId(): String {
        return prefs.getString(KEY_TEMPLATE_ID, "template_38x70") ?: "template_38x70"
    }

    fun setSelectedTemplateId(templateId: String) {
        prefs.edit().putString(KEY_TEMPLATE_ID, templateId).apply()
    }

    companion object {
        private const val KEY_HOST = "key_host"
        private const val KEY_PORT = "key_port"
        private const val KEY_INST = "key_inst"
        private const val KEY_TOK = "key_tok"
        private const val KEY_PIN = "key_pin"
        private const val KEY_SIG = "key_sig"
        private const val KEY_PAIRED_AT = "key_paired_at"
        private const val KEY_IS_PAIRED = "key_is_paired"
        private const val KEY_IS_LOCKED = "key_is_locked"
        private const val KEY_OPERATOR = "key_operator"
        private const val KEY_DEVICE_NAME = "key_device_name"
        private const val KEY_DEVICE_TYPE = "key_device_type"
        private const val KEY_TEMPLATE_ID = "key_template_id"
    }
}
