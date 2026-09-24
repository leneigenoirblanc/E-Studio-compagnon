package com.example

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.scanner.DataWedgeReceiver
import com.example.ui.pairing.PairingViewModel
import com.example.ui.scan.ScanViewModel
import com.example.ui.screens.MainScanScreen
import com.example.ui.screens.OnboardingPairingScreen
import com.example.ui.screens.PinUnlockScreen
import com.example.ui.theme.MyApplicationTheme

enum class AppDestination {
    PAIRING,
    PIN_LOCK,
    MAIN_SCAN
}

class MainActivity : ComponentActivity() {

    private val scanViewModel: ScanViewModel by viewModels()
    private val pairingViewModel: PairingViewModel by viewModels()

    private var dataWedgeReceiver: DataWedgeReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialisation du récepteur DataWedge pour Zebra et terminaux durcis
        dataWedgeReceiver = DataWedgeReceiver { code, symbology ->
            if (!code.isNullOrBlank()) {
                scanViewModel.onBarcodeScanned(
                    code,
                    symbology ?: "EAN_13",
                    com.example.domain.model.ScanSource.ZEBRA_DATAWEDGE
                )
            }
        }

        // Vérifier si un code-barres a été passé via l'Intent de lancement
        handleScanIntent(intent)

        setContent {
            MyApplicationTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = com.example.ui.theme.BrandNavyDark
                ) {
                    val pairingState by pairingViewModel.sessionManager.pairingState.collectAsStateWithLifecycle()
                    val isLocked by pairingViewModel.sessionManager.isLocked.collectAsStateWithLifecycle()

                    var destination by remember(pairingState.isPaired, isLocked) {
                        mutableStateOf(
                            when {
                                !pairingState.isPaired -> AppDestination.PAIRING
                                isLocked -> AppDestination.PIN_LOCK
                                else -> AppDestination.MAIN_SCAN
                            }
                        )
                    }

                    // Enregistrement dynamique du DataWedgeReceiver
                    DisposableEffect(Unit) {
                        val filter = DataWedgeReceiver.createIntentFilter()
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            ContextCompat.registerReceiver(
                                this@MainActivity,
                                dataWedgeReceiver!!,
                                filter,
                                ContextCompat.RECEIVER_EXPORTED
                            )
                        } else {
                            ContextCompat.registerReceiver(
                                this@MainActivity,
                                dataWedgeReceiver!!,
                                filter,
                                ContextCompat.RECEIVER_NOT_EXPORTED
                            )
                        }

                        onDispose {
                            runCatching {
                                unregisterReceiver(dataWedgeReceiver)
                            }
                        }
                    }

                    Crossfade(
                        targetState = destination,
                        label = "screen_navigation"
                    ) { target ->
                        when (target) {
                            AppDestination.PAIRING -> {
                                OnboardingPairingScreen(
                                    viewModel = pairingViewModel,
                                    onPairingSuccess = {
                                        destination = AppDestination.MAIN_SCAN
                                    }
                                )
                            }
                            AppDestination.PIN_LOCK -> {
                                PinUnlockScreen(
                                    viewModel = pairingViewModel,
                                    onUnlocked = {
                                        destination = AppDestination.MAIN_SCAN
                                    },
                                    onResetPairing = {
                                        pairingViewModel.sessionManager.resetPairing()
                                        destination = AppDestination.PAIRING
                                    }
                                )
                            }
                            AppDestination.MAIN_SCAN -> {
                                MainScanScreen(
                                    viewModel = scanViewModel,
                                    onLockSession = {
                                        pairingViewModel.sessionManager.setLocked(true)
                                        destination = AppDestination.PIN_LOCK
                                    },
                                    onNavigateToPairing = {
                                        destination = AppDestination.PAIRING
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleScanIntent(intent)
    }

    private fun handleScanIntent(intent: Intent?) {
        if (intent == null) return
        val zebraData = intent.getStringExtra(DataWedgeReceiver.EXTRA_ZEBRA_DATA_STRING)
        val honeywellData = intent.getStringExtra(DataWedgeReceiver.EXTRA_HONEYWELL_DATA)
        val genericData = intent.getStringExtra("data") ?: intent.getStringExtra("scanner_data")

        val code = zebraData ?: honeywellData ?: genericData
        if (!code.isNullOrBlank()) {
            scanViewModel.onBarcodeScanned(code.trim())
        }
    }
}
