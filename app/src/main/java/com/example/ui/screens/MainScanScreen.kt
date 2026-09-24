package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.dialogs.CatalogSyncDialog
import com.example.ui.dialogs.LotSettingsDialog
import com.example.ui.dialogs.LotValidationDialog
import com.example.ui.dialogs.LotWizardDialog
import com.example.ui.dialogs.ManualProductSearchDialog
import com.example.ui.dialogs.OperatorSwitchDialog
import com.example.ui.dialogs.ScannerSettingsDialog
import com.example.ui.dialogs.TerminalDiagnosticDialog
import com.example.ui.dialogs.TrashDialog
import com.example.ui.scan.ScanEvent
import com.example.ui.scan.ScanViewModel
import com.example.ui.screens.tabs.HomeDashboardTab
import com.example.ui.screens.tabs.InstancesTab
import com.example.ui.screens.tabs.LotsManagerTab
import com.example.ui.screens.tabs.PrintJobsTab
import com.example.ui.screens.tabs.ScannerTab
import com.example.ui.screens.tabs.StationTab
import com.example.ui.theme.BrandNavyDark
import com.example.ui.theme.BrandSkyBlue
import com.example.ui.theme.BrandSkyLight
import com.example.ui.theme.BrandSlateBorder
import com.example.ui.theme.BrandSlateCard
import com.example.ui.theme.RetailEmerald
import com.example.ui.theme.RetailPromoAmber
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class MainNavigationTab {
    HOME,
    SCANNER,
    LOTS,
    INSTANCES,
    PRINT_JOBS,
    STATION
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScanScreen(
    viewModel: ScanViewModel,
    onLockSession: () -> Unit,
    onNavigateToPairing: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var currentTab by remember { mutableStateOf(MainNavigationTab.HOME) }
    var showLotWizardDialog by remember { mutableStateOf(false) }
    var showManualSearchDialog by remember { mutableStateOf(false) }
    var showDiagnosticDialog by remember { mutableStateOf(false) }
    var showScannerSettingsDialog by remember { mutableStateOf(false) }
    var showTrashDialog by remember { mutableStateOf(false) }
    var showValidationDialog by remember { mutableStateOf(false) }
    var validationTargetLot by remember { mutableStateOf<com.example.model.MobileScanLot?>(null) }
    var showCatalogSyncDialog by remember { mutableStateOf(false) }
    var showOperatorSwitchDialog by remember { mutableStateOf(false) }
    var showLotSettingsDialog by remember { mutableStateOf(false) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
        viewModel.events.collect { event ->
            when (event) {
                is ScanEvent.ShowToast -> snackbarHostState.showSnackbar(event.message)
                is ScanEvent.TransferSuccess -> snackbarHostState.showSnackbar(event.message)
                is ScanEvent.TransferError -> snackbarHostState.showSnackbar(event.error)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BrandNavyDark,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BrandSlateCard,
                    titleContentColor = TextPrimary
                ),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showOperatorSwitchDialog = true }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (uiState.isLanConnected) RetailEmerald else RetailPromoAmber)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "E-Studio Mobile",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "${uiState.operatorName} • Lot : ${uiState.lotName}",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                actions = {
                    // Badge état LAN / Instance
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(BrandNavyDark)
                            .clickable { showDiagnosticDialog = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (uiState.isLanConnected) Icons.Default.Wifi else Icons.Default.WifiOff,
                            contentDescription = "Réseau LAN",
                            tint = if (uiState.isLanConnected) BrandSkyLight else RetailPromoAmber,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (uiState.activeInstance != null) uiState.activeInstance!!.name.take(10) else if (uiState.isLanConnected) "LAN" else "Offline",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    IconButton(
                        onClick = onLockSession,
                        modifier = Modifier.testTag("lock_session_button")
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = "Verrouiller PIN", tint = RetailPromoAmber)
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = BrandSlateCard,
                contentColor = TextPrimary,
                tonalElevation = 8.dp,
                modifier = Modifier.navigationBarsPadding()
            ) {
                // 1. Accueil (Point 1)
                NavigationBarItem(
                    selected = currentTab == MainNavigationTab.HOME,
                    onClick = { currentTab = MainNavigationTab.HOME },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Accueil"
                        )
                    },
                    label = { Text("Accueil", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BrandNavyDark,
                        selectedTextColor = BrandSkyLight,
                        indicatorColor = BrandSkyLight,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_tab_home")
                )

                // 2. Scanner
                NavigationBarItem(
                    selected = currentTab == MainNavigationTab.SCANNER,
                    onClick = { currentTab = MainNavigationTab.SCANNER },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Scanner"
                        )
                    },
                    label = { Text("Scanner", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BrandNavyDark,
                        selectedTextColor = BrandSkyLight,
                        indicatorColor = BrandSkyLight,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_tab_scanner")
                )

                // 3. Lots Manager (Lot en cours + Tous les lots)
                NavigationBarItem(
                    selected = currentTab == MainNavigationTab.LOTS,
                    onClick = { currentTab = MainNavigationTab.LOTS },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (uiState.totalLabelsCount > 0) {
                                    Badge(
                                        containerColor = RetailEmerald,
                                        contentColor = BrandNavyDark
                                    ) {
                                        Text(
                                            text = "${uiState.totalLabelsCount}",
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = "Lots"
                            )
                        }
                    },
                    label = { Text("Lots", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BrandNavyDark,
                        selectedTextColor = BrandSkyLight,
                        indicatorColor = BrandSkyLight,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_tab_lots")
                )

                // 4. Instances E-Studio (AnyDesk-like, Point 2 & 3)
                NavigationBarItem(
                    selected = currentTab == MainNavigationTab.INSTANCES,
                    onClick = { currentTab = MainNavigationTab.INSTANCES },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Computer,
                            contentDescription = "Instances E-Studio"
                        )
                    },
                    label = { Text("E-Studio", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BrandNavyDark,
                        selectedTextColor = BrandSkyLight,
                        indicatorColor = BrandSkyLight,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_tab_instances")
                )

                // 5. Jobs Spooler
                NavigationBarItem(
                    selected = currentTab == MainNavigationTab.PRINT_JOBS,
                    onClick = { currentTab = MainNavigationTab.PRINT_JOBS },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (uiState.recentPrintJobs.isNotEmpty()) {
                                    Badge(
                                        containerColor = BrandSkyBlue,
                                        contentColor = BrandNavyDark
                                    ) {
                                        Text(
                                            text = "${uiState.recentPrintJobs.size}",
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Print,
                                contentDescription = "Jobs Spooler"
                            )
                        }
                    },
                    label = { Text("Jobs", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BrandNavyDark,
                        selectedTextColor = BrandSkyLight,
                        indicatorColor = BrandSkyLight,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_tab_jobs")
                )

                // 6. Terminal & Diagnostic
                NavigationBarItem(
                    selected = currentTab == MainNavigationTab.STATION,
                    onClick = { currentTab = MainNavigationTab.STATION },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Devices,
                            contentDescription = "Terminal & Diagnostic"
                        )
                    },
                    label = { Text("Terminal", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BrandNavyDark,
                        selectedTextColor = BrandSkyLight,
                        indicatorColor = BrandSkyLight,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_tab_station")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(
                targetState = currentTab,
                label = "retail_execution_tab_crossfade"
            ) { tab ->
                when (tab) {
                    MainNavigationTab.HOME -> {
                        HomeDashboardTab(
                            uiState = uiState,
                            onStartNewLot = { showLotWizardDialog = true },
                            onNavigateToLots = { currentTab = MainNavigationTab.LOTS },
                            onNavigateToInstances = { currentTab = MainNavigationTab.INSTANCES },
                            onStartScanDirect = { currentTab = MainNavigationTab.SCANNER },
                            onContinueLastInstance = { instance ->
                                viewModel.connectToInstance(instance)
                                currentTab = MainNavigationTab.SCANNER
                            },
                            onTriggerCatalogSync = { showCatalogSyncDialog = true }
                        )
                    }

                    MainNavigationTab.SCANNER -> {
                        ScannerTab(
                            viewModel = viewModel,
                            uiState = uiState,
                            hasCameraPermission = hasCameraPermission,
                            onRequestCameraPermission = {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            },
                            onNavigateToLot = {
                                currentTab = MainNavigationTab.LOTS
                            },
                            onOpenSearchDialog = {
                                showManualSearchDialog = true
                            },
                            onOpenScannerSettings = {
                                showScannerSettingsDialog = true
                            }
                        )
                    }

                    MainNavigationTab.LOTS -> {
                        LotsManagerTab(
                            viewModel = viewModel,
                            uiState = uiState,
                            onOpenLotWizard = { showLotWizardDialog = true },
                            onOpenValidationDialog = { lot ->
                                validationTargetLot = lot
                                showValidationDialog = true
                            },
                            onOpenTrashDialog = { showTrashDialog = true },
                            onNavigateToScanner = { currentTab = MainNavigationTab.SCANNER }
                        )
                    }

                    MainNavigationTab.INSTANCES -> {
                        InstancesTab(
                            viewModel = viewModel,
                            uiState = uiState,
                            onScanQrRequested = onNavigateToPairing
                        )
                    }

                    MainNavigationTab.PRINT_JOBS -> {
                        PrintJobsTab(
                            viewModel = viewModel,
                            uiState = uiState
                        )
                    }

                    MainNavigationTab.STATION -> {
                        StationTab(
                            viewModel = viewModel,
                            uiState = uiState,
                            onLockSession = onLockSession,
                            onNavigateToPairing = onNavigateToPairing,
                            onOpenSettingsDialog = { showOperatorSwitchDialog = true }
                        )
                    }
                }
            }
        }
    }

    // DIALOGUES MODAUX

    // 1. Wizard 3 étapes de création de lot avec profils (Points 7 & 8)
    if (showLotWizardDialog) {
        LotWizardDialog(
            currentOperator = uiState.operatorName,
            currentCatalogVersion = uiState.catalogSyncInfo.localVersion,
            onDismiss = { showLotWizardDialog = false },
            onCreateLot = { name, description, department, templateId, operator, isPromo, requiresTemplate, requiresQuantity, duplicateRule, colorTag, catalogVersion ->
                viewModel.createLotViaWizard(
                    name = name,
                    department = department,
                    targetTemplateId = templateId,
                    operatorName = operator,
                    profilePreset = colorTag,
                    isPromo = isPromo,
                    requiresTemplate = requiresTemplate,
                    requiresQuantity = requiresQuantity
                )
                showLotWizardDialog = false
                currentTab = MainNavigationTab.SCANNER
            }
        )
    }

    // 2. Recherche Manuelle Produit / Pavé Numérique Code-Barres (Points 14 & 15)
    if (showManualSearchDialog) {
        ManualProductSearchDialog(
            productRepository = viewModel.productRepository,
            onDismiss = { showManualSearchDialog = false },
            onSelectProduct = { barcode, resolved ->
                viewModel.onPendingScanConfirmed(1, uiState.targetTemplateId, emptyList(), 1)
                showManualSearchDialog = false
            }
        )
    }

    // 3. Dialogue Diagnostic Matériel & Tests Son/Vibration (Point 34)
    if (showDiagnosticDialog) {
        TerminalDiagnosticDialog(
            status = uiState.hardwareDiagnosticStatus,
            onTestSoundAndVibration = {
                viewModel.testSoundAndVibration(context)
            },
            onDismiss = { showDiagnosticDialog = false }
        )
    }

    // 4. Paramètres Scanner Standard & Avancé (Points 35 & 36)
    if (showScannerSettingsDialog) {
        ScannerSettingsDialog(
            currentScanMode = uiState.scanMode,
            currentProfile = uiState.scannerProfile,
            currentUnknownPolicy = uiState.unknownProductPolicy,
            soundEnabled = uiState.isSoundFeedbackEnabled,
            vibrationEnabled = uiState.isVibrationFeedbackEnabled,
            onSaveSettings = { scanMode, profile, unknownPolicy, sound, vib ->
                viewModel.updateScannerSettings(scanMode, profile, unknownPolicy, sound, vib)
                showScannerSettingsDialog = false
            },
            onDismiss = { showScannerSettingsDialog = false }
        )
    }

    // 5. Corbeille des Lots (Point 41)
    if (showTrashDialog) {
        TrashDialog(
            trashLots = uiState.trashedLots,
            onRestoreLot = { lotId -> viewModel.restoreLot(lotId) },
            onDeletePermanently = { lotId -> viewModel.deleteLotPermanently(lotId) },
            onEmptyTrash = { viewModel.emptyTrash() },
            onDismiss = { showTrashDialog = false }
        )
    }

    // 6. Validation & Résumé avant Export (Points 20, 30, 31, 42)
    if (showValidationDialog) {
        val targetLot = validationTargetLot ?: uiState.allLotsList.find { it.id == uiState.lotId } ?: com.example.model.MobileScanLot(
            id = uiState.lotId.ifEmpty { "active_lot" },
            name = uiState.lotName,
            operatorName = uiState.operatorName,
            items = uiState.items,
            catalogVersion = uiState.catalogSyncInfo.localVersion
        )
        LotValidationDialog(
            lot = targetLot,
            isTransferring = uiState.isTransferring,
            onDismiss = { showValidationDialog = false },
            onExportLocalJson = {
                viewModel.exportLotAsJson(targetLot)
                showValidationDialog = false
            },
            onSendToEStudioServer = {
                viewModel.submitWorkSessionToPrintJob()
                showValidationDialog = false
            }
        )
    }

    // 7. Synchronisation Différentielle du Catalogue (Points 4 & 5)
    if (showCatalogSyncDialog) {
        CatalogSyncDialog(
            syncInfo = uiState.catalogSyncInfo,
            onDismiss = { showCatalogSyncDialog = false },
            onStartSync = {
                viewModel.syncCatalogDifferential()
            }
        )
    }

    // 8. Changement d'Opérateur
    if (showOperatorSwitchDialog) {
        OperatorSwitchDialog(
            currentOperator = uiState.operatorName,
            onDismiss = { showOperatorSwitchDialog = false },
            onOperatorChanged = { name, role ->
                viewModel.switchOperator(name, role)
                showOperatorSwitchDialog = false
            }
        )
    }

    // 9. Paramètres rapides du lot
    if (showLotSettingsDialog) {
        LotSettingsDialog(
            state = uiState,
            onDismiss = { showLotSettingsDialog = false },
            onSave = { name, operator, templateId ->
                viewModel.updateSessionSettings(name, operator, templateId)
            },
            onClearLot = {
                viewModel.clearCurrentSession()
            }
        )
    }
}
