package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.scanner.DataWedgeReceiver
import com.example.ui.dialogs.CreateTableWizardDialog
import com.example.ui.dialogs.ScannerSettingsDialog
import com.example.ui.pairing.PairingViewModel
import com.example.ui.scan.ScanViewModel
import com.example.ui.screens.DesktopConnectionScreen
import com.example.ui.screens.MainScanScreen
import com.example.ui.screens.WelcomeEntryScreen
import com.example.ui.screens.tabs.LotsManagerTab
import com.example.ui.theme.BrandNavyDark
import com.example.ui.theme.MyApplicationTheme

enum class AppDestination {
    WELCOME,            // S01: Accueil / Choix initial (Connecter Desktop ou Créer un lot)
    DESKTOP_CONNECT,    // S02 / S03 / S04 / S05: Connexion logicielle Desktop (QR / Réseau LAN AnyDesk)
    TABLE_MANAGER,      // S06: Gestionnaire des tables & lots
    SCANNER             // S08: Scanner d'articles avec caméra, bouton scan, auto-scan, auto-validate & export
}

class MainActivity : ComponentActivity() {

    private val scanViewModel: ScanViewModel by viewModels()
    private val pairingViewModel: PairingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Vérifier si un code-barres a été scanné via un Intent matériel (Zebra / Honeywell)
        handleScanIntent(intent)

        setContent {
            MyApplicationTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BrandNavyDark
                ) {
                    val pairingState by pairingViewModel.sessionManager.pairingState.collectAsStateWithLifecycle()
                    val scanUiState by scanViewModel.uiState.collectAsStateWithLifecycle()

                    var currentDestination by remember { mutableStateOf(AppDestination.WELCOME) }
                    var showCreateWizardGlobal by remember { mutableStateOf(false) }
                    var showScannerSettingsGlobal by remember { mutableStateOf(false) }

                    Crossfade(
                        targetState = currentDestination,
                        label = "app_navigation_crossfade"
                    ) { target ->
                        when (target) {
                            AppDestination.WELCOME -> {
                                WelcomeEntryScreen(
                                    uiState = scanUiState,
                                    isDesktopConnected = pairingState.isPaired,
                                    connectedInstanceName = pairingState.instanceId,
                                    onConnectDesktopClick = {
                                        currentDestination = AppDestination.DESKTOP_CONNECT
                                    },
                                    onCreateNewBatchClick = {
                                        showCreateWizardGlobal = true
                                    },
                                    onOpenTableManagerClick = {
                                        currentDestination = AppDestination.TABLE_MANAGER
                                    },
                                    onOpenScannerSettingsClick = {
                                        showScannerSettingsGlobal = true
                                    }
                                )
                            }

                            AppDestination.DESKTOP_CONNECT -> {
                                DesktopConnectionScreen(
                                    viewModel = pairingViewModel,
                                    onBackClick = {
                                        currentDestination = AppDestination.WELCOME
                                    },
                                    onConnectionSuccess = {
                                        currentDestination = AppDestination.TABLE_MANAGER
                                    }
                                )
                            }

                            AppDestination.TABLE_MANAGER -> {
                                LotsManagerTab(
                                    viewModel = scanViewModel,
                                    uiState = scanUiState,
                                    onOpenLotWizard = {
                                        showCreateWizardGlobal = true
                                    },
                                    onOpenTrashDialog = {},
                                    onOpenValidationDialog = {},
                                    onNavigateToScanner = {
                                        currentDestination = AppDestination.SCANNER
                                    },
                                    onBackToWelcome = {
                                        currentDestination = AppDestination.WELCOME
                                    }
                                )
                            }

                            AppDestination.SCANNER -> {
                                MainScanScreen(
                                    viewModel = scanViewModel,
                                    onNavigateToTableManager = {
                                        currentDestination = AppDestination.TABLE_MANAGER
                                    },
                                    onNavigateToWelcome = {
                                        currentDestination = AppDestination.WELCOME
                                    },
                                    onNavigateToPairing = {
                                        currentDestination = AppDestination.DESKTOP_CONNECT
                                    }
                                )
                            }
                        }
                    }

                    // Assistant de création de Table / Lot (S07)
                    if (showCreateWizardGlobal) {
                        CreateTableWizardDialog(
                            onDismiss = { showCreateWizardGlobal = false },
                            onCreateTable = { name, colorTag, department, templateId ->
                                scanViewModel.createTableAndOpen(
                                    name = name,
                                    colorTag = colorTag,
                                    department = department,
                                    templateId = templateId,
                                    onCreated = {
                                        showCreateWizardGlobal = false
                                        currentDestination = AppDestination.SCANNER
                                    }
                                )
                            }
                        )
                    }

                    // Paramètres Scanner (S12 / S13)
                    if (showScannerSettingsGlobal) {
                        ScannerSettingsDialog(
                            isAutoScan = scanUiState.isAutoScan,
                            isAutoValidate = scanUiState.isAutoValidate,
                            autoScanDelayMs = scanUiState.autoScanDelayMillis,
                            soundEnabled = scanUiState.isSoundFeedbackEnabled,
                            vibrationEnabled = scanUiState.isVibrationFeedbackEnabled,
                            onSaveSettings = { autoScan, autoValidate, autoScanDelay, sound, vib, symbologies ->
                                scanViewModel.saveScannerSettings(autoScan, autoValidate, autoScanDelay, sound, vib, symbologies)
                                showScannerSettingsGlobal = false
                            },
                            onDismiss = { showScannerSettingsGlobal = false }
                        )
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
