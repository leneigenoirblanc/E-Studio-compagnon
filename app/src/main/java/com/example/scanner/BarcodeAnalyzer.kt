package com.example.scanner

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.atomic.AtomicBoolean

/**
 * CameraX ImageAnalysis.Analyzer couplé à Google ML Kit Barcode Scanning
 * Supporte :
 * 1. Mode Auto-Scan avec délai paramétrable
 * 2. Mode Manuel avec déclencheur one-shot bouton SCAN
 * 3. Filtrage dynamique des symbologies GMS (EAN-13, UPC-A, EAN-8, Code 128, ITF-14, DataMatrix, QR Code)
 */
class BarcodeAnalyzer(
    var isAutoScanEnabled: Boolean = false,
    var autoScanDelayMs: Long = 800L,
    private val onBarcodeDetected: (String, Int) -> Unit
) : ImageAnalysis.Analyzer {

    private val manualScanRequested = AtomicBoolean(false)

    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            Barcode.FORMAT_EAN_13,
            Barcode.FORMAT_EAN_8,
            Barcode.FORMAT_CODE_128,
            Barcode.FORMAT_UPC_A,
            Barcode.FORMAT_UPC_E,
            Barcode.FORMAT_QR_CODE,
            Barcode.FORMAT_DATA_MATRIX,
            Barcode.FORMAT_ITF
        )
        .build()

    private val scanner = BarcodeScanning.getClient(options)

    private var lastScannedCode: String? = null
    private var lastScannedTimestamp: Long = 0L

    fun triggerManualScan() {
        manualScanRequested.set(true)
    }

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val isManual = manualScanRequested.getAndSet(false)
        val shouldAnalyze = isAutoScanEnabled || isManual

        if (!shouldAnalyze) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            scanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    val currentTime = System.currentTimeMillis()
                    for (barcode in barcodes) {
                        val rawValue = barcode.rawValue ?: continue
                        val format = barcode.format

                        // En mode manuel, on accepte directement le code. En mode auto-scan, on applique le délai de debounce
                        if (isManual || rawValue != lastScannedCode || (currentTime - lastScannedTimestamp) > autoScanDelayMs) {
                            lastScannedCode = rawValue
                            lastScannedTimestamp = currentTime
                            onBarcodeDetected(rawValue, format)
                            break
                        }
                    }
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}
