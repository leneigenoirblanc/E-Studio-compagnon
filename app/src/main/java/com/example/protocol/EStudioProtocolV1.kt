package com.example.protocol

import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Spécification formelle du protocole E-Studio Mobile Protocol v1
 * Définit la construction canonique des requêtes, le calcul des empreintes SHA-256,
 * les en-têtes obligatoires, et les structures de réponses normalisées.
 */
object EStudioProtocolV1 {

    const val VERSION = "1.0"

    // En-têtes HTTP requis
    const val HEADER_PROTOCOL_VERSION = "X-EStudio-Protocol-Version"
    const val HEADER_DEVICE_ID = "X-EStudio-Device-Id"
    const val HEADER_REQUEST_ID = "X-EStudio-Request-Id"
    const val HEADER_TIMESTAMP = "X-EStudio-Timestamp"
    const val HEADER_SIGNATURE = "X-EStudio-Signature"
    const val HEADER_IDEMPOTENCY_KEY = "Idempotency-Key"
    const val HEADER_AUTHORIZATION = "Authorization"

    // Fenêtre temporelle autorisée pour la protection contre le replay (±5 minutes)
    const val REPLAY_WINDOW_MILLIS = 5 * 60 * 1000L

    private val iso8601Format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    fun getCurrentTimestampIso(): String = iso8601Format.format(Date())

    /**
     * Construit la chaîne canonique exacte à signer par l'Android Keystore :
     * METHOD + "\n" + PATH + "\n" + TIMESTAMP + "\n" + REQUEST_ID + "\n" + BODY_SHA256
     */
    fun buildCanonicalRequest(
        method: String,
        path: String,
        timestamp: String,
        requestId: String,
        body: String
    ): String {
        val bodyHash = sha256Hex(body)
        return "${method.uppercase()}\n$path\n$timestamp\n$requestId\n$bodyHash"
    }

    /**
     * Calcul SHA-256 d'une chaîne
     */
    fun sha256Hex(input: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}

/**
 * Enveloppe standard pour toutes les réponses de l'API E-Studio
 */
data class EStudioApiResponse<T>(
    val success: Boolean,
    val protocolVersion: String = EStudioProtocolV1.VERSION,
    val requestId: String,
    val timestamp: String = EStudioProtocolV1.getCurrentTimestampIso(),
    val data: T?,
    val error: EStudioApiError? = null
)

data class EStudioApiError(
    val code: String,
    val message: String,
    val details: String? = null
)
