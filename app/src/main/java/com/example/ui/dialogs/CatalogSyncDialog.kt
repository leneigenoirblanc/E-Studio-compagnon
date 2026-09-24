package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.window.Dialog
import com.example.domain.model.CatalogSyncInfo
import com.example.ui.theme.BrandNavyDark
import com.example.ui.theme.BrandSkyBlue
import com.example.ui.theme.BrandSlateBorder
import com.example.ui.theme.BrandSlateCard
import com.example.ui.theme.RetailEmerald
import com.example.ui.theme.RetailPromoAmber
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Points 4, 5, 38 : Synchronisation différentielle et gestion du catalogue local
 */
@Composable
fun CatalogSyncDialog(
    syncInfo: CatalogSyncInfo,
    onStartSync: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = { if (!syncInfo.isSyncing) onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = BrandSlateCard,
            border = BorderStroke(1.dp, BrandSlateBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Titre
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SYNCHRONISATION CATALOGUE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandSkyBlue
                        )
                        Text(
                            text = "Base de Prix & Articles E-Studio",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                    }

                    if (!syncInfo.isSyncing) {
                        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Fermer", tint = TextSecondary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Vérifications préliminaires
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(BrandNavyDark)
                        .padding(12.dp)
                ) {
                    StatusRow("Instance E-Studio reconnue", isOk = true)
                    StatusRow("Autorisation cryptographique validée", isOk = true)
                    StatusRow("Moteur hors-ligne Room actif", isOk = true)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Comparaison des versions (Point 4)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1E293B))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text("Version Mobile", fontSize = 10.sp, color = TextSecondary)
                            Text(syncInfo.localVersion, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("${syncInfo.totalArticlesCount} articles", fontSize = 11.sp, color = BrandSkyBlue)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1E293B))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text("Version E-Studio", fontSize = 10.sp, color = TextSecondary)
                            Text(syncInfo.serverVersion, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = RetailEmerald)
                            Text("Dernière MAJ", fontSize = 11.sp, color = RetailEmerald)
                        }
                    }
                }

                // Différentiel (Point 5)
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0F172A))
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = "Synchronisation différentielle optimisée :",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• Articles nouveaux : +${syncInfo.addedArticlesCount}\n• Articles modifiés : ${syncInfo.modifiedArticlesCount}\n• Articles supprimés : -${syncInfo.deletedArticlesCount}",
                            fontSize = 12.sp,
                            color = BrandSkyBlue
                        )
                    }
                }

                // Barre de progression (Point 4)
                if (syncInfo.isSyncing) {
                    Spacer(modifier = Modifier.height(16.dp))
                    val processedCount = (syncInfo.syncProgress * syncInfo.totalArticlesCount).toInt()
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Téléchargement du différentiel...", fontSize = 12.sp, color = TextSecondary)
                            Text("$processedCount / ${syncInfo.totalArticlesCount}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandSkyBlue)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { syncInfo.syncProgress },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = RetailEmerald,
                            trackColor = BrandSlateBorder
                        )
                    }
                }

                if (syncInfo.isUpToDate) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(RetailEmerald.copy(alpha = 0.15f))
                            .padding(10.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = RetailEmerald, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("✓ Catalogue à jour • Totalement utilisable hors-ligne", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RetailEmerald)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Boutons d'action
                if (!syncInfo.isSyncing) {
                    Button(
                        onClick = {
                            if (syncInfo.isUpToDate) {
                                onDismiss()
                            } else {
                                onStartSync()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(46.dp).testTag("btn_trigger_catalog_sync"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (syncInfo.isUpToDate) RetailEmerald else BrandSkyBlue,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = if (syncInfo.isUpToDate) Icons.Default.Check else Icons.Default.Sync,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (syncInfo.isUpToDate) "TERMINER" else "METTRE À JOUR LE CATALOGUE",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusRow(label: String, isOk: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isOk) Icons.Default.CheckCircle else Icons.Default.Warning,
            contentDescription = null,
            tint = if (isOk) RetailEmerald else RetailPromoAmber,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, fontSize = 11.sp, color = TextPrimary)
    }
}
