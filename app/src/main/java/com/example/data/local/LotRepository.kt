package com.example.data.local

import com.example.domain.model.PrintInstruction
import com.example.model.MobileScanItem
import com.example.model.MobileScanLot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class LotRepository(private val lotDao: LotDao) {

    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)

    val activeLot: Flow<MobileScanLot?> = lotDao.getActiveLot().map { entity ->
        entity?.let { mapEntityToLot(it) }
    }

    val allLots: Flow<List<MobileScanLot>> = lotDao.getAllLots().map { list ->
        list.map { mapEntityToLot(it) }
    }

    val trashLots: Flow<List<MobileScanLot>> = lotDao.getTrashLots().map { list ->
        list.map { mapEntityToLot(it) }
    }

    suspend fun getLotById(id: String): MobileScanLot? {
        return lotDao.getLotById(id)?.let { mapEntityToLot(it) }
    }

    suspend fun saveActiveLot(lot: MobileScanLot) {
        val entity = mapLotToEntity(lot, isTransferred = false)
        lotDao.insertLot(entity)
    }

    suspend fun markLotAsTransferred(lotId: String, timestamp: String) {
        lotDao.markAsTransferred(lotId, timestamp)
    }

    suspend fun softDeleteLot(lotId: String) {
        lotDao.softDeleteLotById(lotId)
    }

    suspend fun restoreLot(lotId: String) {
        lotDao.restoreLotById(lotId)
    }

    suspend fun deletePermanently(lotId: String) {
        lotDao.deleteLotById(lotId)
    }

    suspend fun deleteLotPermanently(lotId: String) {
        deletePermanently(lotId)
    }

    suspend fun emptyTrash() {
        lotDao.emptyTrash()
    }

    suspend fun togglePinLot(lotId: String, currentPinned: Boolean) {
        lotDao.setPinned(lotId, !currentPinned)
    }

    suspend fun pinLot(lotId: String, pinned: Boolean) {
        lotDao.setPinned(lotId, pinned)
    }

    suspend fun setLocked(lotId: String, locked: Boolean) {
        lotDao.setLocked(lotId, locked)
    }

    suspend fun lockLot(lotId: String, isLocked: Boolean) {
        setLocked(lotId, isLocked)
    }

    suspend fun createNewLot(
        name: String,
        department: String,
        targetTemplateId: String,
        operatorName: String,
        profilePreset: String,
        isPromo: Boolean,
        requiresTemplate: Boolean,
        requiresQuantity: Boolean,
        catalogVersion: String
    ): MobileScanLot {
        val now = isoFormat.format(Date())
        val newId = "LOT-${SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())}-${UUID.randomUUID().toString().take(4).uppercase()}"
        val newLot = MobileScanLot(
            id = newId,
            name = name,
            createdAt = now,
            updatedAt = now,
            operatorName = operatorName,
            deviceName = "TC26-001",
            deviceType = "native_terminal",
            status = "DRAFT",
            targetTemplateId = targetTemplateId,
            syncMethod = "direct_lan",
            items = emptyList(),
            description = "Créé via Wizard (Profil $profilePreset)",
            version = 1,
            department = department,
            colorTag = if (isPromo) "PROMO" else profilePreset,
            catalogVersion = catalogVersion,
            isLocked = false,
            isPinned = false,
            isDeleted = false,
            deletedAt = null,
            duplicateRule = "INCREMENT_QTY",
            requiresTemplate = requiresTemplate,
            requiresQuantity = requiresQuantity
        )
        saveActiveLot(newLot)
        return newLot
    }

    /**
     * Point 28 : Cloner un lot existant
     */
    suspend fun cloneLot(sourceLot: MobileScanLot, newOperator: String? = null): MobileScanLot {
        val now = isoFormat.format(Date())
        val newId = "LOT-${SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())}-${UUID.randomUUID().toString().take(4).uppercase()}"
        val clonedItems = sourceLot.items.map { it.copy(id = "item_${UUID.randomUUID().toString().take(6)}") }
        val clonedLot = sourceLot.copy(
            id = newId,
            name = "Copie — ${sourceLot.name}",
            createdAt = now,
            updatedAt = now,
            operatorName = newOperator ?: sourceLot.operatorName,
            status = "DRAFT",
            version = 1,
            isLocked = false,
            isPinned = false,
            isDeleted = false,
            deletedAt = null,
            items = clonedItems
        )
        saveActiveLot(clonedLot)
        return clonedLot
    }

    /**
     * Point 29 : Fusionner deux lots (Lot A + Lot B) avec détection et agrégation des doublons
     */
    suspend fun mergeLots(lotA: MobileScanLot, lotB: MobileScanLot, newName: String): MobileScanLot {
        val now = isoFormat.format(Date())
        val newId = "LOT-${SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())}-${UUID.randomUUID().toString().take(4).uppercase()}"
        
        val aggregatedItems = mutableMapOf<String, MobileScanItem>()
        val allItems = lotA.items + lotB.items
        
        for (item in allItems) {
            val existing = aggregatedItems[item.code]
            if (existing != null) {
                aggregatedItems[item.code] = existing.copy(
                    quantity = existing.quantity + item.quantity,
                    facing = maxOf(existing.facing, item.facing)
                )
            } else {
                aggregatedItems[item.code] = item.copy(id = "item_${UUID.randomUUID().toString().take(6)}")
            }
        }

        val mergedLot = MobileScanLot(
            id = newId,
            name = newName,
            createdAt = now,
            updatedAt = now,
            operatorName = lotA.operatorName,
            deviceName = lotA.deviceName,
            deviceType = lotA.deviceType,
            status = "DRAFT",
            targetTemplateId = lotA.targetTemplateId ?: lotB.targetTemplateId,
            syncMethod = lotA.syncMethod,
            items = aggregatedItems.values.toList(),
            description = "Fusion de '${lotA.name}' et '${lotB.name}'",
            version = 1,
            department = lotA.department,
            colorTag = if (lotA.colorTag == "PROMO" || lotB.colorTag == "PROMO") "PROMO" else "NORMAL",
            catalogVersion = lotA.catalogVersion,
            isLocked = false,
            isPinned = false,
            isDeleted = false,
            deletedAt = null
        )

        saveActiveLot(mergedLot)
        return mergedLot
    }

    /**
     * Point 24 : Créer une version 2 / 3 d'un lot déjà exporté
     */
    suspend fun createNewVersion(sourceLot: MobileScanLot): MobileScanLot {
        val now = isoFormat.format(Date())
        val updated = sourceLot.copy(
            version = sourceLot.version + 1,
            updatedAt = now,
            status = "DRAFT",
            isLocked = false
        )
        saveActiveLot(updated)
        return updated
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
                if (item.department != null) put("department", item.department)
                put("isUnknown", item.isUnknown)
                if (item.templateId != null) put("templateId", item.templateId)
                if (item.instructions.isNotEmpty()) {
                    val instrArray = JSONArray()
                    item.instructions.forEach { instr ->
                        instrArray.put(JSONObject().apply {
                            put("templateId", instr.templateId)
                            put("templateName", instr.templateName)
                            put("quantity", instr.quantity)
                        })
                    }
                    put("instructions", instrArray)
                }
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
            isTransferred = isTransferred,
            description = lot.description,
            version = lot.version,
            department = lot.department,
            colorTag = lot.colorTag,
            catalogVersion = lot.catalogVersion,
            isLocked = lot.isLocked,
            isPinned = lot.isPinned,
            isDeleted = lot.isDeleted,
            deletedAt = lot.deletedAt,
            duplicateRule = lot.duplicateRule,
            requiresTemplate = lot.requiresTemplate,
            requiresQuantity = lot.requiresQuantity
        )
    }

    private fun mapEntityToLot(entity: LotEntity): MobileScanLot {
        val items = mutableListOf<MobileScanItem>()
        if (entity.itemsJson.isNotBlank()) {
            runCatching {
                val array = JSONArray(entity.itemsJson)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val instructions = mutableListOf<PrintInstruction>()
                    if (obj.has("instructions")) {
                        val instrArr = obj.getJSONArray("instructions")
                        for (j in 0 until instrArr.length()) {
                            val inObj = instrArr.getJSONObject(j)
                            instructions.add(
                                PrintInstruction(
                                    templateId = inObj.getString("templateId"),
                                    templateName = inObj.getString("templateName"),
                                    quantity = inObj.optInt("quantity", 1)
                                )
                            )
                        }
                    }

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
                            note = if (obj.has("note") && !obj.isNull("note")) obj.getString("note") else null,
                            department = if (obj.has("department") && !obj.isNull("department")) obj.getString("department") else null,
                            isUnknown = obj.optBoolean("isUnknown", false),
                            templateId = if (obj.has("templateId") && !obj.isNull("templateId")) obj.getString("templateId") else null,
                            instructions = instructions
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
            items = items,
            description = entity.description,
            version = entity.version,
            department = entity.department,
            colorTag = entity.colorTag,
            catalogVersion = entity.catalogVersion,
            isLocked = entity.isLocked,
            isPinned = entity.isPinned,
            isDeleted = entity.isDeleted,
            deletedAt = entity.deletedAt,
            duplicateRule = entity.duplicateRule,
            requiresTemplate = entity.requiresTemplate,
            requiresQuantity = entity.requiresQuantity
        )
    }
}
