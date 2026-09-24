package com.example.scanner.zebra

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

class ZebraScanner(private val context: Context) : BarcodeScanner {

    private val _scans = MutableSharedFlow<BarcodeScan>(extraBufferCapacity = 64)
    override val scans: Flow<BarcodeScan> = _scans.asSharedFlow()

    private var isRegistered = false

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) return
            val action = intent.action
            if (action == "com.symbol.datawedge.api.RESULT_ACTION" ||
                action == "com.example.ACTION_DATAWEDGE_SCAN" ||
                action == "com.symbol.datawedge.data.ACTION"
            ) {
                val barcode = intent.getStringExtra("com.symbol.datawedge.data_string")
                    ?: intent.getStringExtra("data")
                    ?: intent.getStringExtra("barcode_string")
                val symbology = intent.getStringExtra("com.symbol.datawedge.label_type") ?: "ZEBRA_DECODED"

                if (!barcode.isNullOrBlank()) {
                    _scans.tryEmit(
                        BarcodeScan(
                            barcode = barcode.trim(),
                            symbology = symbology,
                            source = ScanSource.ZEBRA_DATAWEDGE
                        )
                    )
                }
            }
        }
    }

    override suspend fun start() {
        if (!isRegistered) {
            val filter = IntentFilter().apply {
                addAction("com.symbol.datawedge.api.RESULT_ACTION")
                addAction("com.example.ACTION_DATAWEDGE_SCAN")
                addAction("com.symbol.datawedge.data.ACTION")
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
