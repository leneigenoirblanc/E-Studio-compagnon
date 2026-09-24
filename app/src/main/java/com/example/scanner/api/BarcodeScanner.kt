package com.example.scanner.api

import com.example.domain.model.ScanSource
import kotlinx.coroutines.flow.Flow

data class BarcodeScan(
    val barcode: String,
    val symbology: String = "EAN_13",
    val source: ScanSource = ScanSource.CAMERA_MLKIT,
    val timestamp: Long = System.currentTimeMillis()
)

interface BarcodeScanner {
    val scans: Flow<BarcodeScan>
    suspend fun start()
    suspend fun stop()
}

/**
 * Filtre Anti-Double-Scan intelligent :
 * Évite de rescanner le même article en continu avec la caméra dans un délai de 800 ms.
 * En revanche, un scan laser physique Zebra ou un code différent est immédiatement accepté.
 */
class AntiDoubleScanFilter(private val debounceThresholdMs: Long = 800L) {
    private var lastBarcode: String? = null
    private var lastScanTimestamp: Long = 0L

    fun shouldAcceptScan(barcode: String, source: ScanSource): Boolean {
        val now = System.currentTimeMillis()
        // Si c'est un bouton matériel physique (Zebra / Honeywell), l'opérateur a expressément appuyé sur la gâchette
        if (source == ScanSource.ZEBRA_DATAWEDGE || source == ScanSource.HONEYWELL_INTENT) {
            lastBarcode = barcode
            lastScanTimestamp = now
            return true
        }

        // Pour la caméra : filtre de rebond
        if (barcode == lastBarcode && (now - lastScanTimestamp) < debounceThresholdMs) {
            return false // Ignorer le double scan involontaire
        }

        lastBarcode = barcode
        lastScanTimestamp = now
        return true
    }

    fun reset() {
        lastBarcode = null
        lastScanTimestamp = 0L
    }
}
