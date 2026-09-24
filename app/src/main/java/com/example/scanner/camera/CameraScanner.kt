package com.example.scanner.camera

import com.example.domain.model.ScanSource
import com.example.scanner.api.BarcodeScan
import com.example.scanner.api.BarcodeScanner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class CameraScanner : BarcodeScanner {

    private val _scans = MutableSharedFlow<BarcodeScan>(extraBufferCapacity = 64)
    override val scans: Flow<BarcodeScan> = _scans.asSharedFlow()

    private var isStarted = false

    override suspend fun start() {
        isStarted = true
    }

    override suspend fun stop() {
        isStarted = false
    }

    fun onCodeScannedFromCamera(code: String, symbology: String = "MLKIT_EAN") {
        if (isStarted && code.isNotBlank()) {
            _scans.tryEmit(
                BarcodeScan(
                    barcode = code.trim(),
                    symbology = symbology,
                    source = ScanSource.CAMERA_MLKIT
                )
            )
        }
    }
}
