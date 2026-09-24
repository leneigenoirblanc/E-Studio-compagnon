package com.example.ui.screens.tabs

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.EStudioInstance
import com.example.domain.model.InstanceStatus
import com.example.ui.dialogs.CatalogSyncDialog
import com.example.ui.scan.ScanUiState
import com.example.ui.scan.ScanViewModel
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
 * Points 2 & 3 : Gestion des instances E-Studio (AnyDesk-style)
 * Découverte mDNS locale, Instances enregistrées & Fiche détaillée
 */
@Composable
fun InstancesTab(
    viewModel: ScanViewModel,
    uiState: ScanUiState,
    onScanQrRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedInstanceForDetail by remember { mutableStateOf<EStudioInstance?>(null) }
    var showCatalogSyncDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BrandNavyDark)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Bouton "SCANNER LE QR E-STUDIO" (Point 2)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = BrandSlateCard),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, BrandSkyBlue.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(BrandSkyBlue.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = BrandSkyLight, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("APPAIRAGE PAR QR CODE", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                            Text("Flashez le QR affiché sur l'écran du logiciel E-Studio", fontSize = 11.sp, color = TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onScanQrRequested,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandSkyBlue),
                        modifier = Modifier.fillMaxWidth().height(42.dp).testTag("btn_scan_estudio_qr")
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SCANNER LE QR E-STUDIO", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 2. Fiche détaillée d'une instance sélectionnée (Point 3)
        item {
            val instance = selectedInstanceForDetail ?: uiState.activeInstance ?: uiState.savedInstances.firstOrNull()
            if (instance != null) {
                InstanceDetailCard(
                    instance = instance,
                    isConnected = uiState.activeInstance?.id == instance.id,
                    onConnect = {
                        viewModel.connectToInstance(instance)
                        showCatalogSyncDialog = true
                    },
                    onForget = {
                        viewModel.forgetInstance(instance.id)
                        if (selectedInstanceForDetail?.id == instance.id) {
                            selectedInstanceForDetail = null
                        }
                    },
                    onToggleFavorite = { viewModel.toggleFavoriteInstance(instance.id) }
                )
            }
        }

        // 3. Section "INSTANCES ENREGISTRÉES" (Point 2)
        item {
            Text(
                text = "INSTANCES ENREGISTRÉES (${uiState.savedInstances.size})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 0.8.sp
            )
        }

        if (uiState.savedInstances.isEmpty()) {
            item {
                Text("Aucune instance enregistrée en mémoire.", fontSize = 11.sp, color = TextTertiary)
            }
        } else {
            items(uiState.savedInstances, key = { it.id }) { instance ->
                InstanceRowItem(
                    instance = instance,
                    isSelected = selectedInstanceForDetail?.id == instance.id,
                    isCurrentActive = uiState.activeInstance?.id == instance.id,
                    onSelect = { selectedInstanceForDetail = instance },
                    onToggleFavorite = { viewModel.toggleFavoriteInstance(instance.id) }
                )
            }
        }

        // 4. Section "INSTANCES DISPONIBLES SUR LE RÉSEAU LOCAL (mDNS)" (Point 2)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SUR LE RÉSEAU LOCAL (${uiState.discoveredInstances.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 0.8.sp
                )

                IconButton(
                    onClick = { viewModel.instanceManager.refreshDiscovery() },
                    modifier = Modifier.size(28.dp).testTag("btn_refresh_discovery")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Rafraîchir", tint = BrandSkyLight, modifier = Modifier.size(16.dp))
                }
            }
        }

        if (uiState.discoveredInstances.isEmpty()) {
            item {
                Text("Recherche mDNS/NSD en cours sur le subnet local...", fontSize = 11.sp, color = TextTertiary)
            }
        } else {
            items(uiState.discoveredInstances, key = { "discovered_${it.id}" }) { instance ->
                InstanceRowItem(
                    instance = instance,
                    isSelected = selectedInstanceForDetail?.id == instance.id,
                    isCurrentActive = uiState.activeInstance?.id == instance.id,
                    onSelect = { selectedInstanceForDetail = instance },
                    onToggleFavorite = { viewModel.toggleFavoriteInstance(instance.id) }
                )
            }
        }
    }

    // Dialogue de synchronisation différentielle du catalogue (Points 4, 5)
    if (showCatalogSyncDialog) {
        CatalogSyncDialog(
            syncInfo = uiState.catalogSyncInfo,
            onDismiss = { showCatalogSyncDialog = false },
            onStartSync = { viewModel.syncCatalogDifferential() }
        )
    }
}

/**
 * Fiche détaillée d'une instance (Point 3)
 */
@Composable
private fun InstanceDetailCard(
    instance: EStudioInstance,
    isConnected: Boolean,
    onConnect: () -> Unit,
    onForget: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = BrandSlateCard),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (isConnected) RetailEmerald else BrandSlateBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = instance.name.uppercase(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Adresse : ${instance.host}:${instance.port}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onToggleFavorite, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = if (instance.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Favori",
                            tint = if (instance.isFavorite) RetailPromoAmber else TextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (instance.status == InstanceStatus.ONLINE) RetailEmerald.copy(alpha = 0.2f) else RetailPromoAmber.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (instance.status == InstanceStatus.ONLINE) "● Disponible" else "○ Hors ligne",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (instance.status == InstanceStatus.ONLINE) RetailEmerald else RetailPromoAmber
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Informations de version et catalogue (Point 3)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(BrandNavyDark)
                    .padding(10.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    val lastConnText = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.FRANCE).format(java.util.Date(instance.lastConnectedAt))
                    DetailRow("Dernière connexion", lastConnText)
                    DetailRow("Catalogue", "${instance.catalogArticlesCount} articles")
                    DetailRow("Version catalogue", instance.catalogVersion)
                    DetailRow("Signature ECDSA", "ECDSA-SHA256 validée")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onConnect,
                    colors = ButtonDefaults.buttonColors(containerColor = if (isConnected) RetailEmerald else BrandSkyBlue),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).height(42.dp).testTag("btn_connect_instance_detail")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isConnected) "CONNECTÉ" else "SE CONNECTER", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onForget,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, RetailErrorRed.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RetailErrorRed),
                    modifier = Modifier.height(42.dp).testTag("btn_forget_instance")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("OUBLIER", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun InstanceRowItem(
    instance: EStudioInstance,
    isSelected: Boolean,
    isCurrentActive: Boolean,
    onSelect: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = if (isSelected) BrandSlateDark else BrandSlateCard),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, if (isCurrentActive) RetailEmerald else if (isSelected) BrandSkyBlue else BrandSlateBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onToggleFavorite, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = if (instance.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = null,
                        tint = if (instance.isFavorite) RetailPromoAmber else TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = instance.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${instance.host} • ${instance.catalogArticlesCount} art.",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (instance.status == InstanceStatus.ONLINE) RetailEmerald.copy(alpha = 0.2f) else BrandNavyDark)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (instance.status == InstanceStatus.ONLINE) "● Dispo" else "○ Off",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (instance.status == InstanceStatus.ONLINE) RetailEmerald else TextSecondary
                )
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 10.sp, color = TextSecondary)
        Text(text = value, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
    }
}
