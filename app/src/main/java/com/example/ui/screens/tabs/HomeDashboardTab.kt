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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.ui.scan.ScanUiState
import com.example.ui.theme.BrandNavyDark
import com.example.ui.theme.BrandSkyBlue
import com.example.ui.theme.BrandSkyLight
import com.example.ui.theme.BrandSlateBorder
import com.example.ui.theme.BrandSlateCard
import com.example.ui.theme.BrandSlateDark
import com.example.ui.theme.RetailEmerald
import com.example.ui.theme.RetailPromoAmber
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

/**
 * Point 1 : Page d'accueil « Choisir son action » & Détection intelligente
 */
@Composable
fun HomeDashboardTab(
    uiState: ScanUiState,
    onStartNewLot: () -> Unit,
    onNavigateToLots: () -> Unit,
    onNavigateToInstances: () -> Unit,
    onStartScanDirect: () -> Unit,
    onContinueLastInstance: (EStudioInstance) -> Unit,
    onTriggerCatalogSync: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val lastInstance = uiState.activeInstance ?: uiState.savedInstances.firstOrNull()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BrandNavyDark)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // En-tête de bienvenue
        Column {
            Text(
                text = "E-STUDIO MOBILE",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.5.sp,
                color = BrandSkyBlue
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Bonjour ${uiState.operatorName}",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Terminal : ${uiState.deviceName} • Profil : ${uiState.scannerProfile.name}",
                fontSize = 11.sp,
                color = TextSecondary
            )
        }

        // DÉTECTION INTELLIGENTE (Point 1) : Dernière connexion E-Studio
        if (lastInstance != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = BrandSlateCard),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BrandSkyBlue.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DERNIÈRE CONNEXION RETENUE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandSkyLight,
                            letterSpacing = 0.5.sp
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (lastInstance.status == InstanceStatus.ONLINE) RetailEmerald else RetailPromoAmber)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (lastInstance.status == InstanceStatus.ONLINE) "Disponible" else "Hors ligne",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (lastInstance.status == InstanceStatus.ONLINE) RetailEmerald else RetailPromoAmber
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "E-STUDIO — ${lastInstance.name}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )

                    Text(
                        text = "IP : ${lastInstance.host}:${lastInstance.port} • Catalogue : ${lastInstance.catalogArticlesCount} articles",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { onContinueLastInstance(lastInstance) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandSkyBlue),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("btn_continue_instance")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "CONTINUER SUR CETTE INSTANCE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 3 GRANDES ACTIONS PRINCIPALES (Point 1)
        Text(
            text = "CHOISIR VOTRE ACTION",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 0.8.sp
        )

        // Action 1 : + NOUVEAU LOT (Créer une nouvelle table)
        ActionBigCard(
            title = "+ NOUVEAU LOT",
            subtitle = "Créer une nouvelle session avec gabarit et département",
            icon = Icons.Default.AddCircleOutline,
            accentColor = BrandSkyBlue,
            onClick = onStartNewLot,
            testTag = "home_btn_new_lot"
        )

        // Action 2 : MES LOTS (8 lots locaux)
        ActionBigCard(
            title = "MES LOTS",
            subtitle = "${uiState.allLotsList.size} lots locaux enregistrés dans le terminal",
            icon = Icons.Default.FolderSpecial,
            accentColor = RetailPromoAmber,
            onClick = onNavigateToLots,
            testTag = "home_btn_my_lots"
        )

        // Action 3 : SE CONNECTER À E-STUDIO (AnyDesk-style)
        ActionBigCard(
            title = "SE CONNECTER À E-STUDIO",
            subtitle = if (uiState.activeInstance != null) "Connecté : ${uiState.activeInstance?.name}" else "Découverte réseau & scan QR",
            icon = Icons.Default.Computer,
            accentColor = RetailEmerald,
            onClick = onNavigateToInstances,
            testTag = "home_btn_connect_estudio"
        )

        // Action 4 : SCAN DIRECT
        ActionBigCard(
            title = "VISEUR SCAN RAPIDE",
            subtitle = "Reprendre le scan immédiat du lot actif (${uiState.items.size} articles)",
            icon = Icons.Default.QrCodeScanner,
            accentColor = Color(0xFFA855F7),
            onClick = onStartScanDirect,
            testTag = "home_btn_scan_direct"
        )

        Spacer(modifier = Modifier.height(8.dp))

        // ÉTAT DU CATALOGUE HORS-LIGNE & SYNCHRONISATION DIFFÉRENTIELLE (Points 4, 5)
        Card(
            colors = CardDefaults.cardColors(containerColor = BrandSlateCard),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, BrandSlateBorder),
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
                            text = "CATALOGUE HORS-LIGNE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandSkyLight
                        )
                        Text(
                            text = "${uiState.catalogSyncInfo.articlesCount} articles référencés",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Button(
                        onClick = onTriggerCatalogSync,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandNavyDark),
                        border = BorderStroke(1.dp, BrandSlateBorder),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(36.dp).testTag("btn_sync_catalog_home")
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null, tint = BrandSkyLight, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SYNCHRONISER", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BrandSkyLight)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Version locale : ${uiState.catalogSyncInfo.localVersion} • Serveur : ${uiState.catalogSyncInfo.serverVersion}",
                    fontSize = 10.sp,
                    color = TextTertiary
                )

                if (uiState.catalogSyncInfo.isUpdateAvailable) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(RetailPromoAmber.copy(alpha = 0.15f))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "⚡ Nouvelle mise à jour du catalogue disponible (${uiState.catalogSyncInfo.newArticlesCount} nouveaux, ${uiState.catalogSyncInfo.updatedArticlesCount} modifiés)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = RetailPromoAmber
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionBigCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = BrandSlateCard),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, BrandSlateBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.15f))
                    .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = TextTertiary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
