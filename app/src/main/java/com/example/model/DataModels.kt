package com.example.model

/**
 * Contrats de données stricts pour le système E-Studio (Retail & Logistique)
 */

data class MobileScanItem(
    val id: String, // ex: "m_item_1727123456"
    val code: String, // Code-barres scanné (EAN, UPC, Code 128, etc.)
    val designation: String? = null,
    val price: Double? = null,
    val promoPrice: Double? = null,
    val quantity: Int = 1, // Nombre d'exemplaires d'étiquettes à imprimer
    val facing: Int = 1,
    val scannedAt: String, // ISO-8601
    val note: String? = null
)

data class MobileScanLot(
    val id: String, // ex: "lot_mob_1727123"
    val name: String, // ex: "Rayon Épicerie - Réassort"
    val createdAt: String,
    val updatedAt: String,
    val operatorName: String,
    val deviceName: String, // ex: "Zebra TC26" ou "Samsung Galaxy A54"
    val deviceType: String = "android", // "android" ou "native_terminal"
    val status: String = "ready", // "ready", "received", "spooled"
    val targetTemplateId: String? = null,
    val syncMethod: String = "direct_lan",
    val items: List<MobileScanItem>
)

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
