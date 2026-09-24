package com.example.data.local

import com.example.model.MobileScanItem
import com.example.model.MobileScanLot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

class LotRepository(private val lotDao: LotDao) {

    val activeLot: Flow<MobileScanLot?> = lotDao.getActiveLot().map { entity ->
        entity?.let { mapEntityToLot(it) }
    }

    val allLots: Flow<List<MobileScanLot>> = lotDao.getAllLots().map { list ->
        list.map { mapEntityToLot(it) }
    }

    suspend fun saveActiveLot(lot: MobileScanLot) {
        val entity = mapLotToEntity(lot, isTransferred = false)
        lotDao.insertLot(entity)
    }

    suspend fun markLotAsTransferred(lotId: String, timestamp: String) {
        lotDao.markAsTransferred(lotId, timestamp)
    }

    suspend fun deleteLot(lotId: String) {
        lotDao.deleteLotById(lotId)
    }

    suspend fun clearHistory() {
        lotDao.clearTransferredLots()
    }

    private fun mapLotToEntity(lot: MobileScanLot, isTransferred: Boolean): LotEntity {
        val jsonArray = JSONArray()
        lot.items.forEach { item ->
            val obj = JSONObject().apply {
                put("id", item.id)
                put("code", item.code)
                if (item.designation != null) put("designation", item.designation)
                if (item.price != null) put("price", item.price)
                if (item.promoPrice != null) put("promoPrice", item.promoPrice)
                put("quantity", item.quantity)
                put("facing", item.facing)
                put("scannedAt", item.scannedAt)
                if (item.note != null) put("note", item.note)
            }
            jsonArray.put(obj)
        }

        return LotEntity(
            id = lot.id,
            name = lot.name,
            createdAt = lot.createdAt,
            updatedAt = lot.updatedAt,
            operatorName = lot.operatorName,
            deviceName = lot.deviceName,
            deviceType = lot.deviceType,
            status = lot.status,
            targetTemplateId = lot.targetTemplateId,
            syncMethod = lot.syncMethod,
            itemsJson = jsonArray.toString(),
            isTransferred = isTransferred
        )
    }

    private fun mapEntityToLot(entity: LotEntity): MobileScanLot {
        val items = mutableListOf<MobileScanItem>()
        if (entity.itemsJson.isNotBlank()) {
            runCatching {
                val array = JSONArray(entity.itemsJson)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    items.add(
                        MobileScanItem(
                            id = obj.getString("id"),
                            code = obj.getString("code"),
                            designation = if (obj.has("designation") && !obj.isNull("designation")) obj.getString("designation") else null,
                            price = if (obj.has("price") && !obj.isNull("price")) obj.getDouble("price") else null,
                            promoPrice = if (obj.has("promoPrice") && !obj.isNull("promoPrice")) obj.getDouble("promoPrice") else null,
                            quantity = obj.optInt("quantity", 1),
                            facing = obj.optInt("facing", 1),
                            scannedAt = obj.optString("scannedAt", ""),
                            note = if (obj.has("note") && !obj.isNull("note")) obj.getString("note") else null
                        )
                    )
                }
            }
        }

        return MobileScanLot(
            id = entity.id,
            name = entity.name,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            operatorName = entity.operatorName,
            deviceName = entity.deviceName,
            deviceType = entity.deviceType,
            status = entity.status,
            targetTemplateId = entity.targetTemplateId,
            syncMethod = entity.syncMethod,
            items = items
        )
    }
}
