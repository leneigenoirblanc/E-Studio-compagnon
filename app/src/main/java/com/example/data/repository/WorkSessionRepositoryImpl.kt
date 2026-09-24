package com.example.data.repository

import com.example.data.local.dao.WorkSessionDao
import com.example.data.local.entities.WorkSessionEntity
import com.example.domain.model.Money
import com.example.domain.model.PricingSnapshot
import com.example.domain.model.PrintPriority
import com.example.domain.model.ProductSnapshot
import com.example.domain.model.ScanItem
import com.example.domain.model.ScanItemStatus
import com.example.domain.model.ScanSource
import com.example.domain.model.SessionStatus
import com.example.domain.model.SessionType
import com.example.domain.model.WorkSession
import com.example.domain.repository.WorkSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class WorkSessionRepositoryImpl(private val dao: WorkSessionDao) : WorkSessionRepository {

    override val activeSession: Flow<WorkSession?> = dao.getActiveSessionFlow().map { entity ->
        entity?.let { mapEntityToSession(it) }
    }

    override val allSessions: Flow<List<WorkSession>> = dao.getAllSessions().map { list ->
        list.map { mapEntityToSession(it) }
    }

    override suspend fun getSessionById(id: String): WorkSession? {
        return dao.getSessionById(id)?.let { mapEntityToSession(it) }
    }

    override suspend fun saveSession(session: WorkSession) {
        dao.insertSession(mapSessionToEntity(session))
    }

    override suspend fun updateSessionStatus(sessionId: String, status: SessionStatus) {
        dao.updateSessionStatus(sessionId, status.name, System.currentTimeMillis())
    }

    override suspend fun deleteSession(sessionId: String) {
        dao.deleteSession(sessionId)
    }

    override suspend fun createNewSession(
        name: String,
        operatorName: String,
        deviceName: String
    ): WorkSession {
        val newSession = WorkSession(
            id = "sess_${System.currentTimeMillis()}_${(1000..9999).random()}",
            name = name,
            type = SessionType.REASSORT_RAYON,
            operatorId = "OP_${operatorName.uppercase().take(6)}",
            operatorName = operatorName,
            deviceId = "DEV_${deviceName.take(6)}",
            deviceName = deviceName,
            status = SessionStatus.ACTIVE,
            items = emptyList(),
            targetTemplateId = "template_38x70",
            targetPrinterId = "PRINTER_RAYON_01",
            priority = PrintPriority.NORMAL,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            idempotencyKey = "idem_sess_${UUID.randomUUID()}"
        )
        saveSession(newSession)
        return newSession
    }

    private fun mapSessionToEntity(session: WorkSession): WorkSessionEntity {
        val array = JSONArray()
        session.items.forEach { item ->
            val obj = JSONObject().apply {
                put("id", item.id)
                put("barcode", item.barcode)
                put("quantity", item.quantity)
                put("facing", item.facing)
                put("capturedAt", item.capturedAt)
                put("source", item.source.name)
                put("status", item.status.name)
                put("idempotencyKey", item.idempotencyKey)

                item.productSnapshot?.let { prod ->
                    val prodObj = JSONObject().apply {
                        put("sku", prod.sku)
                        put("barcode", prod.barcode)
                        put("designation", prod.designation)
                        put("category", prod.category)
                        put("department", prod.department)
                        put("facing", prod.facing)
                    }
                    put("productSnapshot", prodObj)
                }

                item.pricing?.let { pr ->
                    val prObj = JSONObject().apply {
                        put("regularPriceMinor", pr.regularPrice.minorUnits)
                        put("currency", pr.currency)
                        if (pr.promoPrice != null) put("promoPriceMinor", pr.promoPrice.minorUnits)
                        if (pr.promotionId != null) put("promotionId", pr.promotionId)
                    }
                    put("pricing", prObj)
                }
            }
            array.put(obj)
        }

        return WorkSessionEntity(
            id = session.id,
            name = session.name,
            sessionType = session.type.name,
            operatorId = session.operatorId,
            operatorName = session.operatorName,
            deviceId = session.deviceId,
            deviceName = session.deviceName,
            status = session.status.name,
            targetTemplateId = session.targetTemplateId,
            targetPrinterId = session.targetPrinterId,
            priority = session.priority.name,
            itemsJson = array.toString(),
            idempotencyKey = session.idempotencyKey,
            createdAt = session.createdAt,
            updatedAt = session.updatedAt
        )
    }

    private fun mapEntityToSession(entity: WorkSessionEntity): WorkSession {
        val items = mutableListOf<ScanItem>()
        runCatching {
            val array = JSONArray(entity.itemsJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val prodObj = obj.optJSONObject("productSnapshot")
                val prObj = obj.optJSONObject("pricing")

                val prod = prodObj?.let {
                    ProductSnapshot(
                        sku = it.optString("sku"),
                        barcode = it.optString("barcode"),
                        designation = it.optString("designation"),
                        category = it.optString("category"),
                        department = it.optString("department"),
                        facing = it.optInt("facing", 1)
                    )
                }

                val pricing = prObj?.let {
                    val reg = it.optLong("regularPriceMinor", 0L)
                    val curr = it.optString("currency", "EUR")
                    val promo = if (it.has("promoPriceMinor")) it.optLong("promoPriceMinor") else null
                    val promoId = if (it.has("promotionId")) it.optString("promotionId") else null
                    PricingSnapshot(
                        regularPrice = Money(reg, curr),
                        promoPrice = promo?.let { p -> Money(p, curr) },
                        currency = curr,
                        promotionId = promoId
                    )
                }

                items.add(
                    ScanItem(
                        id = obj.getString("id"),
                        barcode = obj.getString("barcode"),
                        quantity = obj.optInt("quantity", 1),
                        facing = obj.optInt("facing", 1),
                        capturedAt = obj.optLong("capturedAt", System.currentTimeMillis()),
                        source = runCatching { ScanSource.valueOf(obj.getString("source")) }.getOrDefault(ScanSource.CAMERA_MLKIT),
                        productSnapshot = prod,
                        pricing = pricing,
                        status = runCatching { ScanItemStatus.valueOf(obj.getString("status")) }.getOrDefault(ScanItemStatus.SCANNED),
                        idempotencyKey = obj.optString("idempotencyKey", "idem_${UUID.randomUUID()}")
                    )
                )
            }
        }

        return WorkSession(
            id = entity.id,
            name = entity.name,
            type = runCatching { SessionType.valueOf(entity.sessionType) }.getOrDefault(SessionType.REASSORT_RAYON),
            operatorId = entity.operatorId,
            operatorName = entity.operatorName,
            deviceId = entity.deviceId,
            deviceName = entity.deviceName,
            status = runCatching { SessionStatus.valueOf(entity.status) }.getOrDefault(SessionStatus.ACTIVE),
            items = items,
            targetTemplateId = entity.targetTemplateId,
            targetPrinterId = entity.targetPrinterId,
            priority = runCatching { PrintPriority.valueOf(entity.priority) }.getOrDefault(PrintPriority.NORMAL),
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            idempotencyKey = entity.idempotencyKey
        )
    }
}
