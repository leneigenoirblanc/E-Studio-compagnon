package com.example.domain.model

import java.util.Locale

/**
 * Gestion monétaire sans virgule flottante pour éviter les imprécisions de calcul
 */
data class Money(
    val minorUnits: Long, // ex: 1299 pour 12.99 € ou 1299 XAF
    val currency: String = "EUR"
) {
    fun toDouble(): Double = minorUnits / 100.0

    fun formatted(): String {
        return when (currency) {
            "EUR" -> String.format(Locale.US, "%.2f €", toDouble())
            "XAF" -> "$minorUnits XAF"
            "USD" -> String.format(Locale.US, "$%.2f", toDouble())
            else -> "${toDouble()} $currency"
        }
    }

    companion object {
        fun fromDouble(amount: Double, currency: String = "EUR"): Money {
            val units = (Math.round(amount * 100)).toLong()
            return Money(units, currency)
        }
    }
}

enum class ScanSource {
    CAMERA_MLKIT,
    ZEBRA_DATAWEDGE,
    HONEYWELL_INTENT,
    MANUAL_KEYPAD
}

enum class ScanItemStatus {
    SCANNED,
    VALIDATED,
    PRINT_QUEUED,
    PRINTED,
    ERROR
}

data class ProductSnapshot(
    val sku: String,
    val barcode: String,
    val designation: String,
    val category: String,
    val department: String,
    val facing: Int = 1
)

data class PricingSnapshot(
    val regularPrice: Money,
    val promoPrice: Money? = null,
    val currency: String = "EUR",
    val promotionId: String? = null
) {
    val isPromoActive: Boolean get() = promoPrice != null
}

data class ScanItem(
    val id: String, // ex: "scan_1727123456"
    val barcode: String,
    val quantity: Int = 1,
    val facing: Int = 1,
    val instructions: List<PrintInstruction> = emptyList(),
    val capturedAt: Long = System.currentTimeMillis(),
    val source: ScanSource = ScanSource.CAMERA_MLKIT,
    val productSnapshot: ProductSnapshot? = null,
    val pricing: PricingSnapshot? = null,
    val status: ScanItemStatus = ScanItemStatus.SCANNED,
    val idempotencyKey: String // Clé unique pour chaque scan
)

enum class SessionStatus {
    DRAFT,
    ACTIVE,
    PAUSED,
    READY_TO_SUBMIT,
    SUBMITTING,
    SUBMITTED,
    PRINTING,
    COMPLETED,
    FAILED,
    CANCELLED
}

enum class SessionType {
    REASSORT_RAYON,
    BALISAGE_PROMO,
    AUDIT_PRIX,
    MISSION_INVENTAIRE
}

data class WorkSession(
    val id: String, // ex: "sess_1727123"
    val name: String,
    val type: SessionType = SessionType.REASSORT_RAYON,
    val operatorId: String,
    val operatorName: String,
    val deviceId: String,
    val deviceName: String,
    val status: SessionStatus = SessionStatus.ACTIVE,
    val items: List<ScanItem> = emptyList(),
    val targetTemplateId: String? = "template_38x70",
    val targetPrinterId: String = "PRINTER_RAYON_01",
    val priority: PrintPriority = PrintPriority.NORMAL,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val idempotencyKey: String
) {
    val totalReferences: Int get() = items.size
    val totalLabels: Int get() = items.sumOf { it.quantity }
}

enum class PrintPriority {
    LOW,
    NORMAL,
    HIGH,
    URGENT
}

/**
 * Cycle de vie étendu et résilient d'un travail d'impression
 * Supporte UNKNOWN en cas de coupure réseau pendant le spooler thermique
 */
enum class PrintJobStatus {
    CREATED,
    VALIDATING,
    ACCEPTED,
    QUEUED,
    SUBMITTING,
    SERVER_RECEIVED,
    DISPATCHING,
    SENT_TO_PRINTER,
    SPOOLING,
    PRINTING,
    COMPLETED,
    PRINTED,
    FAILED,
    RETRYING,
    CANCEL_REQUESTED,
    CANCELLED,
    UNKNOWN // État indéterminé : réseau coupé après envoi à la tête d'impression
}

data class PrintJob(
    val id: String, // ex: "job_01HXYZ..."
    val sessionId: String,
    val sessionName: String,
    val templateId: String,
    val templateName: String,
    val printerId: String,
    val storeId: String,
    val operatorName: String,
    val priority: PrintPriority = PrintPriority.NORMAL,
    val status: PrintJobStatus = PrintJobStatus.QUEUED,
    val items: List<ScanItem>,
    val labelsCount: Int,
    val idempotencyKey: String,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val errorMessage: String? = null
)

enum class PrinterProtocol {
    ZPL_II,
    TSPL,
    ESC_POS,
    PDF_DIRECT
}

enum class PrinterState {
    ONLINE,
    OFFLINE,
    PAPER_OUT,
    COVER_OPEN,
    BUSY,
    ERROR
}

data class PrinterCapability(
    val id: String,
    val name: String,
    val model: String,
    val protocol: PrinterProtocol,
    val dpi: Int = 203,
    val maxLabelWidthMm: Int = 104,
    val maxLabelHeightMm: Int = 300,
    val supportedTemplateIds: List<String>,
    val state: PrinterState = PrinterState.ONLINE,
    val location: String
) {
    fun canPrintTemplate(templateId: String, widthMm: Int, heightMm: Int): Boolean {
        if (!supportedTemplateIds.contains(templateId)) return false
        return widthMm <= maxLabelWidthMm && heightMm <= maxLabelHeightMm
    }
}

enum class MissionStatus {
    PENDING,
    IN_PROGRESS,
    VALIDATED
}

data class Mission(
    val id: String,
    val title: String,
    val department: String,
    val targetItemsCount: Int,
    val completedItemsCount: Int = 0,
    val status: MissionStatus = MissionStatus.IN_PROGRESS,
    val requiredBarcodes: List<String> = emptyList()
)

data class PriceAuditItem(
    val barcode: String,
    val designation: String,
    val shelfPrice: Money,
    val systemPrice: Money,
    val isMismatch: Boolean,
    val auditedAt: Long = System.currentTimeMillis()
)

enum class UserRole {
    STORE_OPERATOR,
    STORE_MANAGER,
    AUDITOR,
    ADMIN
}

enum class Permission {
    SCAN_ITEM,
    CREATE_WORK_SESSION,
    SUBMIT_PRINT_JOB,
    AUDIT_PRICE,
    FORCE_PRICE_OVERRIDE,
    MANAGE_DEVICE,
    VIEW_AUDIT_LOGS
}

data class OperatorSession(
    val sessionId: String,
    val operatorId: String,
    val operatorName: String,
    val role: UserRole,
    val permissions: Set<Permission>,
    val startedAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + 8 * 3600 * 1000L
) {
    val isExpired: Boolean get() = System.currentTimeMillis() > expiresAt
    fun hasPermission(permission: Permission): Boolean = permissions.contains(permission)
}

enum class ConflictResolutionStrategy {
    SERVER_WINS, // Le serveur central reste l'autorité absolue sur les prix et le stock
    CLIENT_WINS,
    MANUAL_REVIEW,
    LAST_WRITE_WINS
}

data class CatalogDelta(
    val fromVersion: Long,
    val toVersion: Long,
    val deltaToken: String,
    val added: List<ProductSnapshot>,
    val updated: List<ProductSnapshot>,
    val deletedBarcodes: List<String>,
    val checksum: String
)

enum class AuditAction {
    PRICE_CHECK,
    PRICE_OVERRIDE,
    PRINT_JOB_SUBMITTED,
    OPERATOR_LOGIN,
    OPERATOR_LOGOUT,
    DEVICE_PAIRING,
    CATALOG_SYNC
}

data class DetailedAuditEvent(
    val id: String,
    val timestamp: Long = System.currentTimeMillis(),
    val operatorId: String,
    val operatorName: String,
    val deviceId: String,
    val storeId: String,
    val action: AuditAction,
    val targetType: String,
    val targetId: String,
    val beforeJson: String? = null,
    val afterJson: String? = null,
    val requestId: String
)

data class DeviceProfile(
    val deviceId: String,
    val model: String,
    val storeId: String,
    val operatorName: String,
    val userRole: UserRole = UserRole.STORE_OPERATOR,
    val batteryPercent: Int = 78,
    val networkStatus: String = "LAN_ONLINE", // LAN_ONLINE, CLOUD_ONLINE, OFFLINE
    val scannerType: String = "CAMERA_MLKIT",
    val appVersion: String = "2.1.0-industrial",
    val lastSyncTimestamp: Long = System.currentTimeMillis()
)
