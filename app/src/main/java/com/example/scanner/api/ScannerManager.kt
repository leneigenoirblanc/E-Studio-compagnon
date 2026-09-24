package com.example.scanner.api

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.domain.model.ScanSource
import com.example.scanner.camera.CameraScanner
import com.example.scanner.honeywell.HoneywellScanner
import com.example.scanner.zebra.ZebraScanner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class ScannerManager(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val antiDoubleScan = AntiDoubleScanFilter(debounceThresholdMs = 800L)

    val cameraScanner = CameraScanner()
    val zebraScanner = ZebraScanner(context)
    val honeywellScanner = HoneywellScanner(context)

    private val _unifiedScans = MutableSharedFlow<BarcodeScan>(extraBufferCapacity = 100)
    val unifiedScans: Flow<BarcodeScan> = _unifiedScans.asSharedFlow()

    private var toneGenerator: ToneGenerator? = runCatching {
        ToneGenerator(AudioManager.STREAM_NOTIFICATION, 85)
    }.getOrNull()

    init {
        // Collecter depuis la Caméra
        scope.launch {
            cameraScanner.scans.collect { scan ->
                handleIncomingScan(scan)
            }
        }
        // Collecter depuis Zebra DataWedge
        scope.launch {
            zebraScanner.scans.collect { scan ->
                handleIncomingScan(scan)
            }
        }
        // Collecter depuis Honeywell
        scope.launch {
            honeywellScanner.scans.collect { scan ->
                handleIncomingScan(scan)
            }
        }
    }

    private fun handleIncomingScan(scan: BarcodeScan) {
        if (antiDoubleScan.shouldAcceptScan(scan.barcode, scan.source)) {
            triggerFeedback()
            _unifiedScans.tryEmit(scan)
        }
    }

    suspend fun startAll() {
        cameraScanner.start()
        zebraScanner.start()
        honeywellScanner.start()
    }

    suspend fun stopAll() {
        cameraScanner.stop()
        zebraScanner.stop()
        honeywellScanner.stop()
    }

    fun submitManualScan(barcode: String) {
        val scan = BarcodeScan(
            barcode = barcode.trim(),
            symbology = "MANUAL_INPUT",
            source = ScanSource.MANUAL_KEYPAD
        )
        handleIncomingScan(scan)
    }

    private fun triggerFeedback() {
        // Bip audio caisse enregistreuse / terminal durci
        runCatching {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 75)
        }
        // Vibration haptique
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(45)
            }
        }
    }
}
