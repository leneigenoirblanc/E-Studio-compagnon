package com.example.scanner

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build

/**
 * BroadcastReceiver pour la capture automatique des scans Zebra DataWedge & terminaux durcis (Honeywell, Datalogic)
 * Écoute com.symbol.datawedge.api.RESULT_ACTION et l'extra com.symbol.datawedge.data_string
 */
class DataWedgeReceiver(
    private val onBarcodeScanned: (code: String, symbology: String?) -> Unit
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null) return

        val action = intent.action ?: return

        // 1. Détection Zebra DataWedge
        val zebraData = intent.getStringExtra(EXTRA_ZEBRA_DATA_STRING)
        val zebraSymbology = intent.getStringExtra(EXTRA_ZEBRA_LABEL_TYPE)

        if (!zebraData.isNullOrBlank()) {
            onBarcodeScanned(zebraData.trim(), zebraSymbology)
            return
        }

        // 2. Détection Honeywell
        val honeywellData = intent.getStringExtra(EXTRA_HONEYWELL_DATA)
        if (!honeywellData.isNullOrBlank()) {
            onBarcodeScanned(honeywellData.trim(), "HONEYWELL")
            return
        }

        // 3. Détection générique (autre scanner matériel configuré en Intent)
        val genericData = intent.getStringExtra("data") 
            ?: intent.getStringExtra("barcode_string")
            ?: intent.getStringExtra("scanner_data")

        if (!genericData.isNullOrBlank()) {
            onBarcodeScanned(genericData.trim(), "GENERIC_TERMINAL")
        }
    }

    companion object {
        const val ACTION_ZEBRA_RESULT = "com.symbol.datawedge.api.RESULT_ACTION"
        const val ACTION_ZEBRA_SCAN = "com.symbol.datawedge.scan_action"
        const val ACTION_CUSTOM_SCAN = "com.example.ACTION_SCAN"
        const val ACTION_HONEYWELL = "com.honeywell.decode.intent.action.EDIT_DATA"

        const val EXTRA_ZEBRA_DATA_STRING = "com.symbol.datawedge.data_string"
        const val EXTRA_ZEBRA_LABEL_TYPE = "com.symbol.datawedge.label_type"
        const val EXTRA_HONEYWELL_DATA = "data"

        /**
         * Construit l'IntentFilter complet pour intercepter les broadcasts DataWedge
         */
        fun createIntentFilter(): IntentFilter {
            return IntentFilter().apply {
                addAction(ACTION_ZEBRA_RESULT)
                addAction(ACTION_ZEBRA_SCAN)
                addAction(ACTION_CUSTOM_SCAN)
                addAction(ACTION_HONEYWELL)
                addCategory(Intent.CATEGORY_DEFAULT)
            }
        }
    }
}
