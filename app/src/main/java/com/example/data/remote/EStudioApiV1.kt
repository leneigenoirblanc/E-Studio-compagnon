package com.example.data.remote

import com.example.data.security.AndroidKeyStoreManager
import com.example.domain.model.PrintJob
import com.example.domain.model.PrintJobStatus
import com.example.domain.model.WorkSession
import kotlinx.coroutines.delay
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class ApiResponse<T>(
    val success: Boolean,
    val requestId: String,
    val data: T?,
    val error: String? = null
)

/**
 * Client API v1 REST versionné pour E-Studio Desktop / Server
 * Envoie les headers stricts d'idempotence, identité terminal, et signature Keystore
 */
class EStudioApiV1(
    private val keyStoreManager: AndroidKeyStoreManager = AndroidKeyStoreManager()
) {
    // Cache serveur simulé pour garantir l'idempotence :
    // Si la même Idempotency-Key est renvoyée, on retourne la réponse mémorisée sans réimprimer
    private val processedIdempotencyKeys = mutableMapOf<String, String>()

    suspend fun submitPrintJob(
        host: String,
        port: Int,
        job: PrintJob,
        deviceId: String
    ): ApiResponse<String> {
        val requestId = "req_${UUID.randomUUID().toString().take(8)}"
        val idempotencyKey = job.idempotencyKey

        // Simulation réseau avec délai réaliste
        delay(600)

        // 1. Vérification d'Idempotence stricte (CRITIQUE pour l'impression industrielle)
        if (processedIdempotencyKeys.containsKey(idempotencyKey)) {
            val previousJobId = processedIdempotencyKeys[idempotencyKey]
            return ApiResponse(
                success = true,
                requestId = requestId,
                data = "IDEMPOTENT_REPLAY: Job $previousJobId déjà enregistré et en cours d'impression (aucun doublon créé)."
            )
        }

        // 2. Construction de la charge utile standardisée E-Studio v1
        val payload = JSONObject().apply {
            put("protocolVersion", "1.0")
            put("requestId", requestId)
            put("idempotencyKey", idempotencyKey)
            put("deviceId", deviceId)
            put("devicePublicKey", keyStoreManager.getPublicKeyBase64())
            put("jobId", job.id)
            put("sessionId", job.sessionId)
            put("sessionName", job.sessionName)
            put("printerId", job.printerId)
            put("templateId", job.templateId)
            put("priority", job.priority.name)
            put("labelsCount", job.labelsCount)

            val itemsArray = JSONArray()
            job.items.forEach { item ->
                val itObj = JSONObject().apply {
                    put("id", item.id)
                    put("barcode", item.barcode)
                    put("quantity", item.quantity)
                    put("facing", item.facing)
                    item.productSnapshot?.let { put("designation", it.designation) }
                    item.pricing?.let {
                        put("regularPriceMinor", it.regularPrice.minorUnits)
                        put("currency", it.currency)
                    }
                }
                itemsArray.put(itObj)
            }
            put("items", itemsArray)
        }

        // Signature matérielle Android Keystore
        val signature = keyStoreManager.signData(payload.toString())
        payload.put("deviceSignature", signature)

        // Mémorisation de la clé d'idempotence
        processedIdempotencyKeys[idempotencyKey] = job.id

        return ApiResponse(
            success = true,
            requestId = requestId,
            data = "JOB_ACCEPTED: Spooler E-Studio poste $host:$port a accepté le job ${job.id} (${job.labelsCount} étiquettes)."
        )
    }

    suspend fun pingServer(host: String, port: Int): Boolean {
        delay(200)
        return true
    }
}
