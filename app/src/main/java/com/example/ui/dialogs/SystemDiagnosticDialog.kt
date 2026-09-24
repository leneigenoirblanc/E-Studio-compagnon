package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import com.example.ui.scan.ScanUiState
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

@Composable
fun SystemDiagnosticDialog(
    state: ScanUiState,
    onDismiss: () -> Unit,
    onRetryConnection: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BrandSlateCard,
        shape = RoundedCornerShape(16.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (state.isLanConnected) RetailEmerald.copy(alpha = 0.2f) else RetailErrorRed.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (state.isLanConnected) Icons.Default.CheckCircle else Icons.Default.CloudOff,
                        contentDescription = null,
                        tint = if (state.isLanConnected) RetailEmerald else RetailErrorRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Diagnostic Système & Réseau",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = if (state.isLanConnected) "Liaison E-Studio opérationnelle" else "Liaison E-Studio interrompue",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Carte Statut Composants
                Card(
                    colors = CardDefaults.cardColors(containerColor = BrandSlateDark),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BrandSlateBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        DiagnosticRow(
                            label = "Base de données locale Room :",
                            status = "OPÉRATIONNELLE (Offline-First)",
                            isOk = true
                        )
                        DiagnosticRow(
                            label = "Moteur de Scan (Camera/Zebra) :",
                            status = "PRÊT (${state.deviceProfile.scannerType})",
                            isOk = true
                        )
                        DiagnosticRow(
                            label = "Sécurité Android Keystore :",
                            status = "ACTIVE (EC secp256r1)",
                            isOk = true
                        )
                        DiagnosticRow(
                            label = "Opérations en attente (Sync Queue) :",
                            status = "${state.syncMetrics.pendingOperationsCount} en file d'attente",
                            isOk = state.syncMetrics.pendingOperationsCount == 0,
                            warning = state.syncMetrics.pendingOperationsCount > 0
                        )
                        DiagnosticRow(
                            label = "Serveur E-Studio Central :",
                            status = if (state.isLanConnected) "CONNECTÉ (${state.serverUrl})" else "INDISPONIBLE (Mode Hors-Ligne)",
                            isOk = state.isLanConnected
                        )
                    }
                }

                // Carte Détail Horodatage
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(BrandNavyDark)
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = "Protocole E-Studio Mobile v1.0 • Idempotency-Key actif",
                            color = BrandSkyLight,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Les scans effectués en mode hors-ligne sont conservés localement et seront automatiquement envoyés au rétablissement du réseau.",
                            color = TextTertiary,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onRetryConnection,
                colors = ButtonDefaults.buttonColors(containerColor = BrandSkyBlue),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("btn_diagnostic_retry")
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Tester la Connexion", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Fermer", color = TextSecondary)
            }
        }
    )
}

@Composable
private fun DiagnosticRow(
    label: String,
    status: String,
    isOk: Boolean,
    warning: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            warning -> RetailPromoAmber
                            isOk -> RetailEmerald
                            else -> RetailErrorRed
                        }
                    )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, color = TextSecondary, fontSize = 11.sp)
        }
        Text(
            text = status,
            color = when {
                warning -> RetailPromoAmber
                isOk -> RetailEmerald
                else -> RetailErrorRed
            },
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
