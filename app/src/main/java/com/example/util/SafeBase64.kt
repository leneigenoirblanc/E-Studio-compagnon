package com.example.util

/**
 * Encodeur / Décodeur Base64 multiplateforme compatible JVM pure, Robolectric et Android runtime
 */
object SafeBase64 {

    fun encode(bytes: ByteArray): String {
        return try {
            java.util.Base64.getEncoder().encodeToString(bytes)
        } catch (_: Throwable) {
            android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
        }
    }

    fun decode(str: String): ByteArray {
        return try {
            java.util.Base64.getDecoder().decode(str.trim())
        } catch (_: Throwable) {
            android.util.Base64.decode(str.trim(), android.util.Base64.NO_WRAP)
        }
    }
}
