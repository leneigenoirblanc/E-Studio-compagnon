package com.example.data.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.example.protocol.EStudioProtocolV1
import com.example.util.SafeBase64
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.security.spec.ECGenParameterSpec

/**
 * Gestionnaire d'identité cryptographique matériel Android Keystore
 * Génère et protège les clés asymétriques du terminal physique (Zebra, Honeywell, Smartphone).
 * Intègre un fallback standard EC secp256r1 pour les environnements de test JVM/Robolectric.
 */
class AndroidKeyStoreManager {

    private var keyStore: KeyStore? = null
    private var jvmFallbackKeyPair: KeyPair? = null

    companion object {
        private const val KEY_ALIAS = "ESTUDIO_DEVICE_IDENTITY_KEY"
    }

    init {
        try {
            val ks = KeyStore.getInstance("AndroidKeyStore")
            ks.load(null)
            keyStore = ks
            ensureDeviceKeyPair()
        } catch (_: Exception) {
            // Environnement JVM de test unitaire : génération d'une paire EC standard en mémoire
            val kpg = KeyPairGenerator.getInstance("EC")
            kpg.initialize(ECGenParameterSpec("secp256r1"))
            jvmFallbackKeyPair = kpg.generateKeyPair()
        }
    }

    private fun ensureDeviceKeyPair() {
        val ks = keyStore ?: return
        if (!ks.containsAlias(KEY_ALIAS)) {
            val kpg = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_EC,
                "AndroidKeyStore"
            )
            val parameterSpec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
            )
                .setDigests(KeyProperties.DIGEST_SHA256)
                .build()

            kpg.initialize(parameterSpec)
            kpg.generateKeyPair()
        }
    }

    fun getPublicKey(): PublicKey {
        val ks = keyStore
        return if (ks != null && ks.containsAlias(KEY_ALIAS)) {
            ks.getCertificate(KEY_ALIAS).publicKey
        } else {
            jvmFallbackKeyPair!!.public
        }
    }

    fun getPublicKeyBase64(): String {
        val pubKey = getPublicKey()
        return SafeBase64.encode(pubKey.encoded)
    }

    /**
     * Signe une chaîne brute avec la clé privée protégée dans le Keystore
     */
    fun signData(data: String): String {
        val privateKey: PrivateKey = keyStore?.let {
            it.getKey(KEY_ALIAS, null) as? PrivateKey
        } ?: jvmFallbackKeyPair?.private ?: return "ERR_NO_KEY"

        val signature = Signature.getInstance("SHA256withECDSA")
        signature.initSign(privateKey)
        signature.update(data.toByteArray(Charsets.UTF_8))
        val signedBytes = signature.sign()
        return SafeBase64.encode(signedBytes)
    }

    /**
     * Construit et signe la requête canonique standard E-Studio Mobile Protocol v1
     */
    fun signCanonicalRequest(
        method: String,
        path: String,
        timestamp: String,
        requestId: String,
        body: String
    ): String {
        val canonical = EStudioProtocolV1.buildCanonicalRequest(method, path, timestamp, requestId, body)
        return signData(canonical)
    }

    /**
     * Vérification d'une signature ECDSA-SHA256
     */
    fun verifySignature(
        canonicalString: String,
        signatureBase64: String,
        publicKey: PublicKey
    ): Boolean {
        return runCatching {
            val signature = Signature.getInstance("SHA256withECDSA")
            signature.initVerify(publicKey)
            signature.update(canonicalString.toByteArray(Charsets.UTF_8))
            val sigBytes = SafeBase64.decode(signatureBase64)
            signature.verify(sigBytes)
        }.getOrDefault(false)
    }
}
