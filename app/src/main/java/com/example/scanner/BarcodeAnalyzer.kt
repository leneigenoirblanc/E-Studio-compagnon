package com.example.scanner

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

/**
 * CameraX ImageAnalysis.Analyzer couplé à Google ML Kit Barcode Scanning
 * Décode EAN-13, EAN-8, Code 128, Code 39, UPC-A, UPC-E et QR Codes à haute cadence
 */
class BarcodeAnalyzer(
    private val onBarcodeDetected: (String, Int) -> Unit
) : ImageAnalysis.Analyzer {

    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            Barcode.FORMAT_EAN_13,
            Barcode.FORMAT_EAN_8,
            Barcode.FORMAT_CODE_128,
            Barcode.FORMAT_CODE_39,
            Barcode.FORMAT_UPC_A,
            Barcode.FORMAT_UPC_E,
            Barcode.FORMAT_QR_CODE
        )
        .build()

    private val scanner = BarcodeScanning.getClient(options)

    private var lastScannedCode: String? = null
    private var lastScannedTimestamp: Long = 0L
    private val debounceDelayMs = 800L

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            scanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    val currentTime = System.currentTimeMillis()
                    for (barcode in barcodes) {
                        val rawValue = barcode.rawValue ?: continue
                        val format = barcode.format

                        // Debounce pour éviter la répétition excessive du même code en continue
                        if (rawValue != lastScannedCode || (currentTime - lastScannedTimestamp) > debounceDelayMs) {
                            lastScannedCode = rawValue
                            lastScannedTimestamp = currentTime
                            onBarcodeDetected(rawValue, format)
                            break
                        }
                    }
                }
                .addOnFailureListener {
                    // Analyse échouée pour cette frame, la boucle continue
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}
