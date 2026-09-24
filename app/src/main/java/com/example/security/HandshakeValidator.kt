package com.example.security

import android.net.Uri
import com.example.model.PairingConfig
import java.security.MessageDigest

/**
 * Validateur de sécurité et protocole d'appairage E-Studio
 * Valide les signatures SHA-256 et gère l'extraction des QR Codes de configuration
 */
object HandshakeValidator {

    private const val SESSION_VALIDITY_MS = 30L * 24L * 60L * 60L * 1000L // 30 jours
    private const val DEFAULT_SECRET_SALT = "ESTUDIO_RETAIL_SECURE_SALT_2026"

    /**
     * Calcule le hash SHA-256 en chaîne hexadécimale
     */
    fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Génère une signature SHA-256 attendue pour un tuple d'appairage
     */
    fun generateExpectedSignature(
        instanceId: String,
        token: String,
        pin: String,
        salt: String = DEFAULT_SECRET_SALT
    ): String {
        return sha256("$instanceId:$token:$pin:$salt")
    }

    /**
     * Parse une URL scannée depuis le QR Code officiel E-Studio
     * Format : https://<IP_OU_DOMAINE>[:PORT]/?mode=pwa&inst=<INSTANCE_ID>&tok=<SHA256_TOKEN>&pin=<PIN>&sig=<SIGNATURE>
     */
    fun parsePairingUrl(rawUrl: String): Result<PairingConfig> {
        return runCatching {
            val uri = Uri.parse(rawUrl)
            val host = uri.host ?: throw IllegalArgumentException("Host manquant dans l'URL")
            val port = if (uri.port != -1) uri.port else if (uri.scheme == "https") 443 else 8080
            
            val inst = uri.getQueryParameter("inst") 
                ?: throw IllegalArgumentException("Paramètre 'inst' absent du QR code")
            val tok = uri.getQueryParameter("tok") 
                ?: throw IllegalArgumentException("Paramètre 'tok' absent du QR code")
            val pin = uri.getQueryParameter("pin") 
                ?: throw IllegalArgumentException("Paramètre 'pin' absent du QR code")
            val sig = uri.getQueryParameter("sig") 
                ?: throw IllegalArgumentException("Paramètre 'sig' absent du QR code")

            val now = System.currentTimeMillis()

            PairingConfig(
                serverHost = host,
                serverPort = port,
                instanceId = inst,
                token = tok,
                pin = pin,
                signature = sig,
                pairedAtTimestamp = now,
                isPaired = true,
                isLocked = false
            )
        }
    }

    /**
     * Vérifie la validité de la session (30 jours max)
     */
    fun isSessionValid(config: PairingConfig): Boolean {
        if (!config.isPaired) return false
        val now = System.currentTimeMillis()
        val age = now - config.pairedAtTimestamp
        return age in 0..SESSION_VALIDITY_MS
    }

    /**
     * Vérifie si le code PIN 4 chiffres correspond
     */
    fun verifyPin(enteredPin: String, expectedPin: String): Boolean {
        return enteredPin.trim() == expectedPin.trim()
    }
}
