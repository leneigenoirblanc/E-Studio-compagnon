package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.EStudioInstance
import com.example.ui.components.CameraScannerView
import com.example.ui.pairing.PairingEvent
import com.example.ui.pairing.PairingViewModel
import com.example.ui.theme.BrandNavyDark
import com.example.ui.theme.BrandSkyBlue
import com.example.ui.theme.BrandSkyLight
import com.example.ui.theme.BrandSlateBorder
import com.example.ui.theme.BrandSlateCard
import com.example.ui.theme.BrandSlateDark
import com.example.ui.theme.RetailEmerald
import com.example.ui.theme.RetailErrorRed
import com.example.ui.theme.RetailPromoAmber
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

/**
 * S02 / S03 / S04 / S05 — Écran de Connexion Desktop
 * Permet :
 * 1. S03 : Scan du QR Code affiché sur le logiciel PC
 * 2. S04 : Sélection d'une instance disponible sur le réseau local (style AnyDesk / mDNS)
 * 3. S05 : Validation et retour d'état de connexion immédiat
 */
@Composable
fun DesktopConnectionScreen(
    viewModel: PairingViewModel,
    onBackClick: () -> Unit,
    onConnectionSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = QR Code, 1 = Instances Réseau

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var isTorchOn by remember { mutableStateOf(false) }

    // Fallback manuel IP
    var showManualIpEntry by remember { mutableStateOf(false) }
    var manualHost by remember { mutableStateOf("") }
    var manualPort by remember { mutableStateOf("8080") }

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
            if (event is PairingEvent.PairingCompleted) {
                onConnectionSuccess()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BrandNavyDark)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // En-tête avec bouton retour
        Surface(
            color = BrandSlateCard,
            border = BorderStroke(1.dp, BrandSlateBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Retour",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "Connexion au Logiciel Desktop",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Synchronisation PC & Impression d'étiquettes",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        // Onglets S03 / S04
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = BrandNavyDark,
            contentColor = BrandSkyBlue,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(10.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SCANNER LE QR", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Dns, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("INSTANCES RÉSEAU", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        if (selectedTab == 0) {
            // S03 — SCAN DU QR CODE DU LOGICIEL DESKTOP
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Visez le QR Code affiché sur l'écran de votre logiciel Desktop E-Studio",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Viseur Caméra
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.Black)
                        .border(2.dp, BrandSkyBlue.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                ) {
                    if (hasCameraPermission) {
                        CameraScannerView(
                            modifier = Modifier.fillMaxSize(),
                            isAutoScan = true,
                            autoScanDelayMs = 400L,
                            isTorchOn = isTorchOn,
                            onBarcodeDetected = { qrPayload, _ ->
                                viewModel.onQrScanned(qrPayload)
                            },
                            onToggleTorch = { isTorchOn = !isTorchOn }
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.QrCode, contentDescription = null, tint = BrandSkyLight, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("Accès Caméra requis", color = TextPrimary, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandSkyBlue)
                                ) {
                                    Text("Autoriser la caméra", color = Color.Black)
                                }
                            }
                        }
                    }

                    // Bouton Flash
                    IconButton(
                        onClick = { isTorchOn = !isTorchOn },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                    ) {
                        Icon(
                            imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Flash",
                            tint = if (isTorchOn) RetailPromoAmber else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.isLoading) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = BrandSkyBlue)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = uiState.statusMessage ?: "Connexion au PC en cours...",
                            color = BrandSkyLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (uiState.errorMessage != null) {
                    Surface(
                        color = RetailErrorRed.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, RetailErrorRed.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = RetailErrorRed, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = uiState.errorMessage!!, color = RetailErrorRed, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bouton repli manuel IP
                OutlinedButton(
                    onClick = { showManualIpEntry = !showManualIpEntry },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, BrandSlateBorder)
                ) {
                    Text(
                        text = if (showManualIpEntry) "Masquer la saisie manuelle IP" else "Saisie manuelle de l'adresse IP",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                if (showManualIpEntry) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = BrandSlateCard,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BrandSlateBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Adresse IP de l'ordinateur :", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = manualHost,
                                onValueChange = { manualHost = it },
                                placeholder = { Text("Ex: 192.168.1.50", color = TextTertiary) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = BrandNavyDark,
                                    unfocusedContainerColor = BrandNavyDark,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Port :", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = manualPort,
                                onValueChange = { manualPort = it },
                                placeholder = { Text("8080", color = TextTertiary) },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = BrandNavyDark,
                                    unfocusedContainerColor = BrandNavyDark,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    val portInt = manualPort.toIntOrNull() ?: 8080
                                    viewModel.connectManual(manualHost.trim(), portInt)
                                },
                                enabled = manualHost.isNotBlank(),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandSkyBlue)
                            ) {
                                Text("CONNEXION DIRECTE", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        } else {
            // S04 — SÉLECTION D'UNE INSTANCE DESKTOP SUR LE RÉSEAU LOCAL (STYLE ANYDESK)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "INSTANCES DISPONIBLES (${uiState.discoveredInstances.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandSkyLight,
                        letterSpacing = 1.sp
                    )
                    IconButton(
                        onClick = { viewModel.refreshDiscovery() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Rafraîchir", tint = BrandSkyBlue, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (uiState.discoveredInstances.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Dns, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Aucune instance Desktop détectée",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Assurez-vous que le logiciel E-Studio est ouvert sur votre PC et connecté au même réseau Wi-Fi.",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.refreshDiscovery() },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandSkyBlue)
                            ) {
                                Text("Rechercher à nouveau", color = Color.Black, fontSize = 12.sp)
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.discoveredInstances, key = { it.id }) { instance ->
                            DesktopInstanceCard(
                                instance = instance,
                                onConnect = {
                                    viewModel.connectToDiscovered(instance)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DesktopInstanceCard(
    instance: EStudioInstance,
    onConnect: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = BrandSlateCard,
        border = BorderStroke(1.dp, BrandSlateBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(BrandSkyBlue.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Computer,
                        contentDescription = null,
                        tint = BrandSkyBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = instance.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (instance.isOnline) RetailEmerald else RetailPromoAmber)
                        )
                    }
                    Text(
                        text = "${instance.address}:${instance.port} • ${instance.storeName}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            Button(
                onClick = onConnect,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RetailEmerald),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "CONNECTER",
                    color = Color.Black,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp
                )
            }
        }
    }
}
