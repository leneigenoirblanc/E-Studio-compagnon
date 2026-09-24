package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.scan.ScanUiState
import com.example.ui.theme.BrandNavyDark
import com.example.ui.theme.BrandSkyBlue
import com.example.ui.theme.BrandSkyLight
import com.example.ui.theme.BrandSlateBorder
import com.example.ui.theme.BrandSlateCard
import com.example.ui.theme.RetailEmerald
import com.example.ui.theme.RetailErrorRed
import com.example.ui.theme.RetailPromoAmber
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Point 1 : Page d'accueil « Choisir son action »
 * - En-tête : E-STUDIO MOBILE & Bonjour Alexandre
 * - Détection intelligente après appairage : "Dernière connexion : E-STUDIO — Magasin Central ● Disponible [ CONTINUER ]"
 * - Cartes principales : + NOUVEAU LOT, MES LOTS, SE CONNECTER À E-STUDIO
 * - Indicateur de synchronisation de catalogue et état réseau
 */
@Composable
fun HomeScreen(
    uiState: ScanUiState,
    onNewLotClick: () -> Unit,
    onMyLotsClick: () -> Unit,
    onConnectInstanceClick: () -> Unit,
    onContinueActiveLotClick: () -> Unit,
    onSyncCatalogClick: () -> Unit,
    onOpenDiagnosticClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BrandNavyDark)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. En-tête personnalisé
        item {
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text(
                    text = "E-STUDIO MOBILE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandSkyBlue,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "Bonjour ${uiState.operatorName}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                Text(
                    text = "Terminal industriel de Retail Execution & Étiquetage",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        // 2. Détection intelligente de la dernière connexion (Point 1)
        item {
            val activeInstanceName = uiState.activeInstance?.name ?: "E-STUDIO — Magasin Central"
            val isOnline = uiState.isLanConnected

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = BrandSlateCard
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, if (isOnline) RetailEmerald.copy(alpha = 0.4f) else BrandSlateBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("intelligent_reconnect_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Dernière connexion",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = activeInstanceName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        // Badge de statut disponible
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isOnline) RetailEmerald.copy(alpha = 0.15f) else RetailErrorRed.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isOnline) RetailEmerald else RetailErrorRed)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isOnline) "Disponible" else "Hors ligne",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isOnline) RetailEmerald else RetailErrorRed
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onContinueActiveLotClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RetailEmerald,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("btn_continue_session")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (uiState.items.isNotEmpty()) "CONTINUER LE LOT EN COURS (${uiState.items.size} art.)" else "ACCÉDER AU SCANNER",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // 3. Les 3 Cartes d'Action Principales (Point 1)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                // CARTE 1 : + NOUVEAU LOT
                ActionCard(
                    title = "+ NOUVEAU LOT",
                    subtitle = "Créer une nouvelle table de scan (Wizard 3 étapes ou profil)",
                    icon = Icons.Default.AddCircle,
                    accentColor = BrandSkyBlue,
                    testTag = "card_new_lot",
                    onClick = onNewLotClick
                )

                // CARTE 2 : MES LOTS
                val totalLots = uiState.allLotsList.size.coerceAtLeast(uiState.offlinePendingLotsCount.coerceAtLeast(1))
                ActionCard(
                    title = "MES LOTS",
                    subtitle = "$totalLots lots locaux enregistrés dans Room",
                    icon = Icons.Default.Inventory2,
                    accentColor = RetailPromoAmber,
                    testTag = "card_my_lots",
                    badgeText = "$totalLots lots",
                    onClick = onMyLotsClick
                )

                // CARTE 3 : SE CONNECTER À E-STUDIO
                val serverName = uiState.activeInstance?.name ?: "E-STUDIO-Caisse-01"
                ActionCard(
                    title = "SE CONNECTER À E-STUDIO",
                    subtitle = "Poste : $serverName (AnyDesk discovery & QR)",
                    icon = Icons.Default.Dns,
                    accentColor = RetailEmerald,
                    testTag = "card_connect_estudio",
                    badgeText = if (uiState.isLanConnected) "LAN OK" else "HORS LIGNE",
                    onClick = onConnectInstanceClick
                )
            }
        }

        // 4. Centre de Synchronisation du Catalogue (Points 4, 5, 33, 38)
        item {
            val sync = uiState.catalogSyncInfo
            Card(
                colors = CardDefaults.cardColors(containerColor = BrandSlateCard),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, if (sync.isObsolete) RetailPromoAmber else BrandSlateBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (sync.isObsolete) Icons.Default.Warning else Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = if (sync.isObsolete) RetailPromoAmber else RetailEmerald,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Catalogue local E-Studio",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        IconButton(
                            onClick = onSyncCatalogClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Actualiser",
                                tint = BrandSkyBlue
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "${sync.totalArticlesCount} articles • Version mobile : ${sync.localVersion}",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    if (sync.isObsolete) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "⚠ Catalogue non actualisé depuis ${sync.daysSinceLastSync} jours (v${sync.serverVersion} dispo)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = RetailPromoAmber
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (sync.isUpToDate) "✓ Catalogue à jour (100% hors-ligne)" else "+${sync.addedArticlesCount} nouv. / ${sync.modifiedArticlesCount} modif.",
                            fontSize = 11.sp,
                            color = if (sync.isUpToDate) RetailEmerald else BrandSkyBlue
                        )

                        OutlinedButton(
                            onClick = onSyncCatalogClick,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, BrandSkyBlue.copy(alpha = 0.6f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandSkyBlue),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(
                                text = if (sync.isUpToDate) "VÉRIFIER" else "METTRE À JOUR",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 5. Raccourci vers le Centre de Diagnostic (Point 34)
        item {
            Surface(
                color = Color.Transparent,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BrandSlateBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenDiagnosticClick() }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                            tint = BrandSkyLight,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Diagnostic du terminal (Scanner, Son, Keystore, Base)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    testTag: String,
    badgeText: String? = null,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = BrandSlateCard),
        shape = RoundedCornerShape(16.dp),
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
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accentColor.copy(alpha = 0.15f)),
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

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        if (badgeText != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(accentColor.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = badgeText,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = accentColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
