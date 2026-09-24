package com.example.scanner.honeywell

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.domain.model.ScanSource
import com.example.scanner.api.BarcodeScan
import com.example.scanner.api.BarcodeScanner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class HoneywellScanner(private val context: Context) : BarcodeScanner {

    private val _scans = MutableSharedFlow<BarcodeScan>(extraBufferCapacity = 64)
    override val scans: Flow<BarcodeScan> = _scans.asSharedFlow()

    private var isRegistered = false

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) return
            val action = intent.action
            if (action == "com.honeywell.decode.intent.action.EDIT_DATA" ||
                action == "com.honeywell.action.BARCODE_DATA"
            ) {
                val barcode = intent.getStringExtra("data")
                    ?: intent.getStringExtra("barcode")
                val symbology = intent.getStringExtra("codeId") ?: "HONEYWELL_DECODED"

                if (!barcode.isNullOrBlank()) {
                    _scans.tryEmit(
                        BarcodeScan(
                            barcode = barcode.trim(),
                            symbology = symbology,
                            source = ScanSource.HONEYWELL_INTENT
                        )
                    )
                }
            }
        }
    }

    override suspend fun start() {
        if (!isRegistered) {
            val filter = IntentFilter().apply {
                addAction("com.honeywell.decode.intent.action.EDIT_DATA")
                addAction("com.honeywell.action.BARCODE_DATA")
                addCategory(Intent.CATEGORY_DEFAULT)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_EXPORTED)
            } else {
                ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
            }
            isRegistered = true
        }
    }

    override suspend fun stop() {
        if (isRegistered) {
            runCatching { context.unregisterReceiver(receiver) }
            isRegistered = false
        }
    }
}
