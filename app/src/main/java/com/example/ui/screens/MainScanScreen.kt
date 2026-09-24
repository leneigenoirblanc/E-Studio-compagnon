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
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ReceiptLong
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.dialogs.LotSettingsDialog
import com.example.ui.scan.ScanEvent
import com.example.ui.scan.ScanViewModel
import com.example.ui.screens.tabs.HistoryTab
import com.example.ui.screens.tabs.LotTab
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
    SCANNER,
    LOT,
    HISTORY,
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

    var currentTab by remember { mutableStateOf(MainNavigationTab.SCANNER) }
    var showSettingsDialog by remember { mutableStateOf(false) }

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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (uiState.isLanConnected) RetailEmerald else RetailPromoAmber)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "E-Studio Scan",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = uiState.lotName,
                                fontSize = 11.sp,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                actions = {
                    // Badge état LAN
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(BrandNavyDark)
                            .clickable { currentTab = MainNavigationTab.STATION }
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
                            text = if (uiState.isLanConnected) "LAN" else "Offline",
                            color = TextSecondary,
                            fontSize = 11.sp,
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
            // Barre de Navigation M3 standard conforme aux bonnes pratiques
            NavigationBar(
                containerColor = BrandSlateCard,
                contentColor = TextPrimary,
                tonalElevation = 8.dp,
                modifier = Modifier.navigationBarsPadding()
            ) {
                // 1. Onglet Scanner
                NavigationBarItem(
                    selected = currentTab == MainNavigationTab.SCANNER,
                    onClick = { currentTab = MainNavigationTab.SCANNER },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Scanner"
                        )
                    },
                    label = { Text("Scanner", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BrandNavyDark,
                        selectedTextColor = BrandSkyLight,
                        indicatorColor = BrandSkyLight,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_tab_scanner")
                )

                // 2. Onglet Lot d'impression avec Badge dynamique
                NavigationBarItem(
                    selected = currentTab == MainNavigationTab.LOT,
                    onClick = { currentTab = MainNavigationTab.LOT },
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
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = "Lot d'impression"
                            )
                        }
                    },
                    label = { Text("Lot en cours", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BrandNavyDark,
                        selectedTextColor = BrandSkyLight,
                        indicatorColor = BrandSkyLight,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_tab_lot")
                )

                // 3. Onglet Historique
                NavigationBarItem(
                    selected = currentTab == MainNavigationTab.HISTORY,
                    onClick = { currentTab = MainNavigationTab.HISTORY },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Historique"
                        )
                    },
                    label = { Text("Historique", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BrandNavyDark,
                        selectedTextColor = BrandSkyLight,
                        indicatorColor = BrandSkyLight,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_tab_history")
                )

                // 4. Onglet Station
                NavigationBarItem(
                    selected = currentTab == MainNavigationTab.STATION,
                    onClick = { currentTab = MainNavigationTab.STATION },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Dns,
                            contentDescription = "Station"
                        )
                    },
                    label = { Text("Station", fontSize = 11.sp) },
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
                label = "tab_navigation_crossfade"
            ) { tab ->
                when (tab) {
                    MainNavigationTab.SCANNER -> {
                        ScannerTab(
                            viewModel = viewModel,
                            uiState = uiState,
                            hasCameraPermission = hasCameraPermission,
                            onRequestCameraPermission = {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            },
                            onNavigateToLot = {
                                currentTab = MainNavigationTab.LOT
                            }
                        )
                    }
                    MainNavigationTab.LOT -> {
                        LotTab(
                            viewModel = viewModel,
                            uiState = uiState,
                            onOpenSettingsDialog = { showSettingsDialog = true },
                            onNavigateToScanner = {
                                currentTab = MainNavigationTab.SCANNER
                            }
                        )
                    }
                    MainNavigationTab.HISTORY -> {
                        HistoryTab()
                    }
                    MainNavigationTab.STATION -> {
                        StationTab(
                            viewModel = viewModel,
                            uiState = uiState,
                            onLockSession = onLockSession,
                            onNavigateToPairing = onNavigateToPairing,
                            onOpenSettingsDialog = { showSettingsDialog = true }
                        )
                    }
                }
            }
        }
    }

    if (showSettingsDialog) {
        LotSettingsDialog(
            state = uiState,
            onDismiss = { showSettingsDialog = false },
            onSave = { name, operator, templateId ->
                viewModel.updateLotMetadata(name, operator, templateId)
            },
            onClearLot = {
                viewModel.clearCurrentLot()
            }
        )
    }
}
