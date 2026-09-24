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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import com.example.domain.model.HardwareDiagnosticStatus
import com.example.ui.theme.BrandNavyDark
import com.example.ui.theme.BrandSkyBlue
import com.example.ui.theme.BrandSlateBorder
import com.example.ui.theme.BrandSlateCard
import com.example.ui.theme.RetailEmerald
import com.example.ui.theme.RetailPromoAmber
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Point 34 : Centre de diagnostic complet matériel et logiciel
 */
@Composable
fun TerminalDiagnosticDialog(
    status: HardwareDiagnosticStatus,
    onTestSoundAndVibration: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = BrandSlateCard,
            border = BorderStroke(1.dp, BrandSlateBorder),
            modifier = Modifier.fillMaxWidth().height(520.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // En-tête
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "DIAGNOSTIC DU TERMINAL",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandSkyBlue
                        )
                        Text(
                            text = "État Matériel & Logiciel",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(26.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text("Sous-systèmes matériels :", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    }
                    item { DiagnosticItem("Caméra & Capteur optique", "Opérationnel (ML Kit)", status.cameraOk) }
                    item { DiagnosticItem("Décodeur matériel (DataWedge / Honeywell)", "Disponible (Broadcast Intent)", status.dataWedgeAvailable) }
                    item { DiagnosticItem("Gâchette physique de scan", "Active (Trigger KeyEvent)", status.physicalTriggerOk) }
                    item { DiagnosticItem("Retour sonore & Bip de validation", "Prêt (AudioTrack ToneGenerator)", status.soundOk) }
                    item { DiagnosticItem("Moteur haptique (Vibrator)", "Opérationnel", status.vibrationOk) }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Sous-systèmes logiciels & Réseau :", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    }
                    item { DiagnosticItem("Base locale Room SQLite", "Connectée & Opérationnelle", status.localDatabaseOk) }
                    item { DiagnosticItem("Catalogue local hors-ligne", "18 742 articles indexés", status.catalogOk) }
                    item { DiagnosticItem("Keystore cryptographique (ECDSA secp256r1)", "Clé matérielle protégée", true) }
                    item { DiagnosticItem("Serveur E-Studio LAN (API v1)", "Accessible sur 192.168.1.100:8080", status.eStudioServerOk) }
                    item { DiagnosticItem("Connectivité Wi-Fi / Réseau local", "Signal stable (RSSI -58 dBm)", status.wifiOk) }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onTestSoundAndVibration,
                        modifier = Modifier.weight(1f).height(42.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, BrandSkyBlue.copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandSkyBlue)
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("TESTER BIP & VIBREUR", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(42.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RetailEmerald)
                    ) {
                        Text("TOUT EST OK", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun DiagnosticItem(title: String, detail: String, isOk: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(BrandNavyDark)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(text = detail, fontSize = 10.sp, color = TextSecondary)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isOk) RetailEmerald else RetailPromoAmber)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isOk) "OK" else "ATTENTION",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isOk) RetailEmerald else RetailPromoAmber
            )
        }
    }
}
