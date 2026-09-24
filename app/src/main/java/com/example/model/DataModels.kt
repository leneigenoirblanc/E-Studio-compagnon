package com.example.model

/**
 * Contrats de données stricts pour le système E-Studio (Retail & Logistique)
 */

import com.example.domain.model.PrintInstruction

data class MobileScanItem(
    val id: String, // ex: "m_item_1727123456"
    val code: String, // Code-barres scanné (EAN, UPC, Code 128, etc.)
    val designation: String? = null,
    val price: Double? = null,
    val promoPrice: Double? = null,
    val quantity: Int = 1, // Nombre d'exemplaires d'étiquettes à imprimer
    val facing: Int = 1,
    val scannedAt: String = "", // ISO-8601
    val note: String? = null,
    val department: String? = null,
    val isUnknown: Boolean = false, // ⚠ NON RÉFÉRENCÉ
    val templateId: String? = null,
    val instructions: List<PrintInstruction> = emptyList() // Multi-gabarits par article
) {
    val barcode: String get() = code
}

data class MobileScanLot(
    val id: String, // ex: "lot_mob_1727123" ou "LOT-2026-09-24-001"
    val name: String, // ex: "Rayon Épicerie - Réassort"
    val createdAt: String = "",
    val updatedAt: String = "",
    val operatorName: String = "Alice Dupont",
    val deviceName: String = "TC26-001", // ex: "Zebra TC26" ou "Samsung Galaxy A54"
    val deviceType: String = "android", // "android" ou "native_terminal"
    val status: String = "ready", // "ready", "received", "spooled", "draft", "locked"
    val targetTemplateId: String? = null,
    val syncMethod: String = "direct_lan",
    val items: List<MobileScanItem> = emptyList(),
    val description: String = "",
    val version: Int = 1,
    val department: String = "Épicerie",
    val colorTag: String = "NORMAL", // PROMO, URGENT, NORMAL, INVENTAIRE
    val catalogVersion: String = "2026.09.24.01", // Catalogue "figé" pour chaque lot
    val isLocked: Boolean = false,
    val isPinned: Boolean = false,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val duplicateRule: String = "INCREMENT_QTY",
    val requiresTemplate: Boolean = true,
    val requiresQuantity: Boolean = true
) {
    val operator: String get() = operatorName
    val isTransferred: Boolean get() = status == "spooled" || status == "received"
}

data class PairingConfig(
    val serverHost: String = "192.168.1.100",
    val serverPort: Int = 8080,
    val instanceId: String = "ESTUDIO-STORE-01",
    val token: String = "",
    val pin: String = "1234",
    val signature: String = "",
    val pairedAtTimestamp: Long = 0L,
    val isPaired: Boolean = false,
    val isLocked: Boolean = false
)

data class LabelTemplate(
    val id: String,
    val name: String,
    val dimensions: String,
    val type: String
)
