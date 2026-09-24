package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import com.example.domain.model.DeviceProfile
import com.example.domain.model.UserRole
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

    private val _failedAttempts = MutableStateFlow(prefs.getInt(KEY_FAILED_ATTEMPTS, 0))
    val failedAttempts: StateFlow<Int> = _failedAttempts.asStateFlow()

    fun loadConfig(): PairingConfig {
        val host = prefs.getString(KEY_HOST, "192.168.1.100") ?: "192.168.1.100"
        val port = prefs.getInt(KEY_PORT, 8080)
        val inst = prefs.getString(KEY_INST, "ESTUDIO-CENTRAL-01") ?: "ESTUDIO-CENTRAL-01"
        val tok = prefs.getString(KEY_TOK, "") ?: ""
        val pin = prefs.getString(KEY_PIN, "123456") ?: "123456"
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
            .putInt(KEY_FAILED_ATTEMPTS, 0)
            .apply()

        _pairingState.value = config
        _isLocked.value = false
        _failedAttempts.value = 0
    }

    fun setLocked(locked: Boolean) {
        prefs.edit().putBoolean(KEY_IS_LOCKED, locked).apply()
        _isLocked.value = locked
    }

    fun recordFailedAttempt() {
        val count = _failedAttempts.value + 1
        prefs.edit().putInt(KEY_FAILED_ATTEMPTS, count).apply()
        _failedAttempts.value = count
    }

    fun resetFailedAttempts() {
        prefs.edit().putInt(KEY_FAILED_ATTEMPTS, 0).apply()
        _failedAttempts.value = 0
    }

    fun resetPairing() {
        prefs.edit().clear().apply()
        _pairingState.value = PairingConfig()
        _isLocked.value = false
        _failedAttempts.value = 0
    }

    fun getOperatorName(): String {
        return prefs.getString(KEY_OPERATOR, "Alice Dupont") ?: "Alice Dupont"
    }

    fun setOperatorName(name: String) {
        prefs.edit().putString(KEY_OPERATOR, name).apply()
    }

    fun saveOperator(name: String, role: String) {
        setOperatorName(name)
        val r = runCatching { UserRole.valueOf(role) }.getOrDefault(UserRole.STORE_OPERATOR)
        setUserRole(r)
    }

    fun getUserRole(): UserRole {
        val roleStr = prefs.getString(KEY_ROLE, UserRole.STORE_OPERATOR.name) ?: UserRole.STORE_OPERATOR.name
        return runCatching { UserRole.valueOf(roleStr) }.getOrDefault(UserRole.STORE_OPERATOR)
    }

    fun setUserRole(role: UserRole) {
        prefs.edit().putString(KEY_ROLE, role.name).apply()
    }

    fun getDeviceId(): String {
        return prefs.getString(KEY_DEVICE_ID, "TC26-001") ?: "TC26-001"
    }

    fun setDeviceId(id: String) {
        prefs.edit().putString(KEY_DEVICE_ID, id).apply()
    }

    fun getStoreId(): String {
        return prefs.getString(KEY_STORE_ID, "Store #04 - Yaoundé Central") ?: "Store #04 - Yaoundé Central"
    }

    fun setStoreId(store: String) {
        prefs.edit().putString(KEY_STORE_ID, store).apply()
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

    fun getDeviceProfile(): DeviceProfile {
        return DeviceProfile(
            deviceId = getDeviceId(),
            model = getDeviceName(),
            storeId = getStoreId(),
            operatorName = getOperatorName(),
            userRole = getUserRole(),
            batteryPercent = 78,
            networkStatus = "LAN_ONLINE",
            scannerType = if (getDeviceType() == "native_terminal") "ZEBRA_DATAWEDGE" else "CAMERA_MLKIT",
            appVersion = "2.1.0-industrial",
            lastSyncTimestamp = System.currentTimeMillis()
        )
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
        private const val KEY_FAILED_ATTEMPTS = "key_failed_attempts"
        private const val KEY_OPERATOR = "key_operator"
        private const val KEY_ROLE = "key_role"
        private const val KEY_DEVICE_ID = "key_device_id"
        private const val KEY_STORE_ID = "key_store_id"
        private const val KEY_DEVICE_NAME = "key_device_name"
        private const val KEY_DEVICE_TYPE = "key_device_type"
        private const val KEY_TEMPLATE_ID = "key_template_id"
    }
}
