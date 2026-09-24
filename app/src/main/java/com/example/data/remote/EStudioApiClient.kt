package com.example.data.remote

import com.example.model.MobileScanLot
import com.example.model.PairingConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class EStudioApiClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .writeTimeout(8, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    /**
     * Envoie un lot de scan au serveur E-Studio pour impression
     */
    suspend fun sendLot(
        config: PairingConfig,
        lot: MobileScanLot
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val scheme = if (config.serverPort == 443) "https" else "http"
            val url = "$scheme://${config.serverHost}:${config.serverPort}/api/v1/lots/spool"

            val lotJson = serializeLot(lot)
            val requestBody = lotJson.toString().toRequestBody(jsonMediaType)

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("X-EStudio-Instance", config.instanceId)
                .addHeader("X-EStudio-Token", config.token)
                .addHeader("X-EStudio-Signature", config.signature)
                .addHeader("Content-Type", "application/json")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val bodyStr = response.body?.string() ?: ""
                "Lot ${lot.id} envoyé avec succès au spooler d'impression E-Studio ($bodyStr)"
            } else {
                throw Exception("Erreur serveur E-Studio HTTP ${response.code}: ${response.message}")
            }
        }
    }

    /**
     * Teste la connectivité directe LAN vers le serveur E-Studio
     */
    suspend fun pingServer(config: PairingConfig): Result<Boolean> = withContext(Dispatchers.IO) {
        runCatching {
            val scheme = if (config.serverPort == 443) "https" else "http"
            val url = "$scheme://${config.serverHost}:${config.serverPort}/api/v1/health"

            val request = Request.Builder()
                .url(url)
                .get()
                .addHeader("X-EStudio-Instance", config.instanceId)
                .build()

            val response = client.newCall(request).execute()
            response.isSuccessful
        }
    }

    /**
     * Sérialise MobileScanLot selon le contrat JSON exact E-Studio
     */
    fun serializeLot(lot: MobileScanLot): JSONObject {
        val root = JSONObject()
        root.put("id", lot.id)
        root.put("name", lot.name)
        root.put("createdAt", lot.createdAt)
        root.put("updatedAt", lot.updatedAt)
        root.put("operatorName", lot.operatorName)
        root.put("deviceName", lot.deviceName)
        root.put("deviceType", lot.deviceType)
        root.put("status", lot.status)
        if (lot.targetTemplateId != null) {
            root.put("targetTemplateId", lot.targetTemplateId)
        } else {
            root.put("targetTemplateId", JSONObject.NULL)
        }
        root.put("syncMethod", lot.syncMethod)

        val itemsArray = JSONArray()
        lot.items.forEach { item ->
            val itemObj = JSONObject()
            itemObj.put("id", item.id)
            itemObj.put("code", item.code)
            if (item.designation != null) itemObj.put("designation", item.designation) else itemObj.put("designation", JSONObject.NULL)
            if (item.price != null) itemObj.put("price", item.price) else itemObj.put("price", JSONObject.NULL)
            if (item.promoPrice != null) itemObj.put("promoPrice", item.promoPrice) else itemObj.put("promoPrice", JSONObject.NULL)
            itemObj.put("quantity", item.quantity)
            itemObj.put("facing", item.facing)
            itemObj.put("scannedAt", item.scannedAt)
            if (item.note != null) itemObj.put("note", item.note) else itemObj.put("note", JSONObject.NULL)
            itemsArray.put(itemObj)
        }
        root.put("items", itemsArray)
        return root
    }
}
