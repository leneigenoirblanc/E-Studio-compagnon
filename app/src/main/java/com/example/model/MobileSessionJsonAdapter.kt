package com.example.model

import org.json.JSONArray
import org.json.JSONObject

/**
 * Sérialiseur JSON pour l'export et l'import de lots mobiles E-Studio
 */
object MobileSessionJsonAdapter {

    fun exportLotToJson(lot: MobileScanLot): String {
        val root = JSONObject()
        root.put("id", lot.id)
        root.put("name", lot.name)
        root.put("createdAt", lot.createdAt)
        root.put("updatedAt", lot.updatedAt)
        root.put("operatorName", lot.operatorName)
        root.put("deviceName", lot.deviceName)
        root.put("deviceType", lot.deviceType)
        root.put("status", lot.status)
        root.put("targetTemplateId", lot.targetTemplateId ?: JSONObject.NULL)
        root.put("syncMethod", lot.syncMethod)
        root.put("department", lot.department)
        root.put("colorTag", lot.colorTag)
        root.put("catalogVersion", lot.catalogVersion)
        root.put("isLocked", lot.isLocked)
        root.put("isPinned", lot.isPinned)
        root.put("version", lot.version)

        val itemsArray = JSONArray()
        for (item in lot.items) {
            val itemObj = JSONObject()
            itemObj.put("id", item.id)
            itemObj.put("code", item.code)
            itemObj.put("designation", item.designation ?: JSONObject.NULL)
            itemObj.put("price", item.price ?: JSONObject.NULL)
            itemObj.put("promoPrice", item.promoPrice ?: JSONObject.NULL)
            itemObj.put("quantity", item.quantity)
            itemObj.put("facing", item.facing)
            itemObj.put("scannedAt", item.scannedAt)
            itemObj.put("department", item.department ?: JSONObject.NULL)
            itemObj.put("templateId", item.templateId ?: JSONObject.NULL)
            itemObj.put("isUnknown", item.isUnknown)

            val instructionsArray = JSONArray()
            for (instr in item.instructions) {
                val instrObj = JSONObject()
                instrObj.put("templateId", instr.templateId)
                instrObj.put("templateName", instr.templateName)
                instrObj.put("quantity", instr.quantity)
                instructionsArray.put(instrObj)
            }
            itemObj.put("instructions", instructionsArray)
            itemsArray.put(itemObj)
        }
        root.put("items", itemsArray)
        return root.toString(2)
    }
}
