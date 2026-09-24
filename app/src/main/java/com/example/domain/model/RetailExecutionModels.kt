package com.example.domain.model

/**
 * Modèles industriels Retail Execution étendus pour E-Studio Mobile
 */

/**
 * Statut d'une instance E-Studio
 */
enum class InstanceStatus {
    ONLINE,
    OFFLINE
}

/**
 * Représentation d'une instance E-Studio Desktop/Server détectée ou mémorisée (AnyDesk-style)
 */
data class EStudioInstance(
    val id: String,
    val name: String, // ex: "E-Studio Caisse", "Magasin Central", "Entrepôt"
    val address: String, // ex: "192.168.1.25"
    val port: Int = 8080,
    val isOnline: Boolean = true,
    val isFavorite: Boolean = false,
    val lastConnectedAt: Long = System.currentTimeMillis(),
    val catalogArticleCount: Int = 18742,
    val catalogVersion: String = "2026.09.24.01",
    val storeName: String = "Magasin Central"
) {
    val host: String get() = address
    val catalogArticlesCount: Int get() = catalogArticleCount
    val lastConnected: Long get() = lastConnectedAt
    val status: InstanceStatus get() = if (isOnline) InstanceStatus.ONLINE else InstanceStatus.OFFLINE
}

/**
 * Multi-gabarits par article : un article peut avoir plusieurs instructions d'impression
 * ex: 10 étiquettes rayon 50x30 ET 2 stop-rayons promotion
 */
data class PrintInstruction(
    val templateId: String,
    val templateName: String,
    val quantity: Int = 1
)

/**
 * Règle de traitement lorsqu'un article déjà scanné est à nouveau présenté
 */
enum class DuplicateRule {
    INCREMENT_QTY,   // Augmenter la quantité (par défaut)
    NEW_LINE,        // Créer une nouvelle ligne
    ASK              // Demander quoi faire
}

/**
 * Politique appliquée aux articles non référencés dans le catalogue local
 */
enum class UnknownProductPolicy {
    BLOCK,           // Bloquer l'ajout
    WARN_AND_ALLOW,  // Autoriser mais signaler ⚠ NON RÉFÉRENCÉ (par défaut)
    ALLOW_AUTO       // Autoriser sans avertissement
}

/**
 * Modes de scan clairement séparés
 */
enum class ScanMode {
    MANUAL,                    // Auto Scan OFF, Auto Validate OFF
    AUTO_SCAN_WITH_VALIDATION, // Auto Scan ON, Auto Validate OFF
    FULL_AUTOMATIC             // Auto Scan ON, Auto Validate ON (Zebra / Cadence élevée)
}

/**
 * Profils pré-paramétrés de scanner
 */
enum class ScannerProfilePreset {
    GENERAL_PUBLIC, // Grand public (caméra standard, délais souples)
    SUPERMARKET,    // Supermarché (EAN-13, EAN-8, promo rapide, bip sonore)
    LOGISTICS,      // Logistique (Code 128, GS1-128, ITF-14)
    WAREHOUSE,      // Entrepôt (SSCC, DataMatrix, validation ultra-rapide)
    CUSTOM          // Personnalisé
}

/**
 * Profils métier de lots pour création accélérée
 */
data class LotProfile(
    val id: String,
    val name: String,
    val description: String,
    val defaultDepartment: String,
    val defaultTemplateId: String,
    val isPromoMode: Boolean = false,
    val duplicateRule: DuplicateRule = DuplicateRule.INCREMENT_QTY,
    val colorTag: String = "NORMAL"
)

/**
 * État et métriques de synchronisation différentielle du catalogue
 */
data class CatalogSyncInfo(
    val localVersion: String = "2026.09.23.04",
    val serverVersion: String = "2026.09.24.01",
    val totalArticlesCount: Int = 18742,
    val addedArticlesCount: Int = 142,
    val modifiedArticlesCount: Int = 381,
    val deletedArticlesCount: Int = 17,
    val isUpToDate: Boolean = false,
    val lastSyncTimestamp: Long = System.currentTimeMillis() - (8 * 24 * 3600 * 1000L), // 8 jours par défaut
    val isSyncing: Boolean = false,
    val syncProgress: Float = 0f
) {
    val isObsolete: Boolean get() = (System.currentTimeMillis() - lastSyncTimestamp) > (7 * 24 * 3600 * 1000L)
    val daysSinceLastSync: Int get() = ((System.currentTimeMillis() - lastSyncTimestamp) / (24 * 3600 * 1000L)).toInt().coerceAtLeast(0)
    val isUpdateAvailable: Boolean get() = !isUpToDate
    val articlesCount: Int get() = totalArticlesCount
    val newArticlesCount: Int get() = addedArticlesCount
    val updatedArticlesCount: Int get() = modifiedArticlesCount
}

/**
 * Diagnostic matériel & logiciel du terminal
 */
data class HardwareDiagnosticStatus(
    val cameraOk: Boolean = true,
    val scannerOk: Boolean = true,
    val microphoneOk: Boolean = true,
    val soundOk: Boolean = true,
    val vibrationOk: Boolean = true,
    val storageOk: Boolean = true,
    val localDatabaseOk: Boolean = true,
    val catalogOk: Boolean = true,
    val eStudioServerOk: Boolean = true,
    val wifiOk: Boolean = true,
    val dataWedgeAvailable: Boolean = true,
    val physicalTriggerOk: Boolean = true
)

/**
 * Résumé avant export pour contrôle opérateur
 */
data class LotValidationSummary(
    val lotId: String,
    val lotName: String,
    val totalArticles: Int,
    val totalLabels: Int,
    val mainTemplateName: String,
    val promoArticlesCount: Int,
    val unknownArticlesCount: Int,
    val incompleteArticlesCount: Int,
    val catalogVersion: String,
    val isValidForExport: Boolean
)
