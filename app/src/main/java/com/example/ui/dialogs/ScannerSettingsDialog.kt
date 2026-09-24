package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.domain.model.ScanMode
import com.example.domain.model.ScannerProfilePreset
import com.example.domain.model.UnknownProductPolicy
import com.example.ui.theme.BrandNavyDark
import com.example.ui.theme.BrandSkyBlue
import com.example.ui.theme.BrandSlateBorder
import com.example.ui.theme.BrandSlateCard
import com.example.ui.theme.RetailEmerald
import com.example.ui.theme.RetailPromoAmber
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Points 9, 35, 36 : Paramètres du scanner séparés en Standard et Avancé
 */
@Composable
fun ScannerSettingsDialog(
    currentScanMode: ScanMode,
    currentProfile: ScannerProfilePreset,
    currentUnknownPolicy: UnknownProductPolicy,
    soundEnabled: Boolean,
    vibrationEnabled: Boolean,
    onSaveSettings: (
        scanMode: ScanMode,
        profile: ScannerProfilePreset,
        unknownPolicy: UnknownProductPolicy,
        sound: Boolean,
        vibration: Boolean
    ) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Standard, 1 = Avancé

    var scanMode by remember { mutableStateOf(currentScanMode) }
    var profile by remember { mutableStateOf(currentProfile) }
    var unknownPolicy by remember { mutableStateOf(currentUnknownPolicy) }
    var sound by remember { mutableStateOf(soundEnabled) }
    var vibration by remember { mutableStateOf(vibrationEnabled) }

    // Symbologies actives (Point 36)
    val activeSymbologies = remember {
        mutableStateListOf(
            "EAN-13", "EAN-8", "UPC-A", "UPC-E", "Code 128", "GS1-128", "ITF-14", "DataMatrix", "QR Code"
        )
    }

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
                            text = "PARAMÈTRES DU SCANNER",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandSkyBlue
                        )
                        Text(
                            text = if (selectedTab == 0) "Configuration Standard" else "Options Industrielles Avancées",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(26.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Onglets Standard / Avancé
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = BrandNavyDark,
                    contentColor = BrandSkyBlue,
                    modifier = Modifier.clip(RoundedCornerShape(8.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Standard", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Avancé (Profils/Symbologies)", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (selectedTab == 0) {
                        // STANDARD : Les 3 modes de scan clairement séparés (Point 9)
                        item {
                            Text("Mode opératoire de scan (Point 9) :", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }

                        item {
                            ModeOption(
                                title = "1. Mode manuel",
                                subtitle = "Caméra active, aucun scan sans appui sur SCAN",
                                isSelected = scanMode == ScanMode.MANUAL,
                                onClick = { scanMode = ScanMode.MANUAL }
                            )
                        }

                        item {
                            ModeOption(
                                title = "2. Auto-scan avec validation",
                                subtitle = "Détection automatique, confirmation opérateur [ ADD ]",
                                isSelected = scanMode == ScanMode.AUTO_SCAN_WITH_VALIDATION,
                                onClick = { scanMode = ScanMode.AUTO_SCAN_WITH_VALIDATION }
                            )
                        }

                        item {
                            ModeOption(
                                title = "3. Entièrement automatique",
                                subtitle = "Ajout instantané au lot (Très haute cadence Zebra/Honeywell)",
                                isSelected = scanMode == ScanMode.FULL_AUTOMATIC,
                                onClick = { scanMode = ScanMode.FULL_AUTOMATIC }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Retours opérateur :", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Bip sonore de confirmation", fontSize = 12.sp, color = TextSecondary)
                                Switch(
                                    checked = sound,
                                    onCheckedChange = { sound = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = BrandSkyBlue, checkedTrackColor = BrandSkyBlue.copy(alpha = 0.5f))
                                )
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Vibration haptique", fontSize = 12.sp, color = TextSecondary)
                                Switch(
                                    checked = vibration,
                                    onCheckedChange = { vibration = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = BrandSkyBlue, checkedTrackColor = BrandSkyBlue.copy(alpha = 0.5f))
                                )
                            }
                        }
                    } else {
                        // AVANCÉ : Profils scanner (Point 35) & Symbologies (Point 36)
                        item {
                            Text("Profils scanner pré-configurés (Point 35) :", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }

                        item {
                            listOf(
                                ScannerProfilePreset.SUPERMARKET to "Supermarché (EAN-13, EAN-8, détection rapide)",
                                ScannerProfilePreset.LOGISTICS to "Logistique (Code 128, GS1-128, ITF-14 cartons)",
                                ScannerProfilePreset.WAREHOUSE to "Entrepôt (SSCC, DataMatrix, cadences extrêmes)",
                                ScannerProfilePreset.GENERAL_PUBLIC to "Grand public (Paramètres standard tolérants)"
                            ).forEach { (preset, label) ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { profile = preset }
                                        .padding(vertical = 4.dp)
                                ) {
                                    RadioButton(
                                        selected = profile == preset,
                                        onClick = { profile = preset },
                                        colors = RadioButtonDefaults.colors(selectedColor = BrandSkyBlue)
                                    )
                                    Text(label, fontSize = 11.sp, color = TextPrimary)
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Symbologies 1D/2D activées (Point 36) :", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }

                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf("EAN-13 / EAN-8", "UPC-A / UPC-E", "Code 128 / GS1-128", "ITF-14", "SSCC Palettes", "DataMatrix 2D", "QR Code").forEach { sym ->
                                    val isChecked = activeSymbologies.contains(sym.split(" ")[0])
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth().clickable {
                                            val key = sym.split(" ")[0]
                                            if (isChecked) activeSymbologies.remove(key) else activeSymbologies.add(key)
                                        }
                                    ) {
                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = { check ->
                                                val key = sym.split(" ")[0]
                                                if (check) activeSymbologies.add(key) else activeSymbologies.remove(key)
                                            },
                                            colors = CheckboxDefaults.colors(checkedColor = BrandSkyBlue)
                                        )
                                        Text(sym, fontSize = 11.sp, color = TextPrimary)
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Articles inconnus au catalogue (Point 16) :", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }

                        item {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = unknownPolicy == UnknownProductPolicy.WARN_AND_ALLOW,
                                    onClick = { unknownPolicy = UnknownProductPolicy.WARN_AND_ALLOW },
                                    colors = RadioButtonDefaults.colors(selectedColor = RetailPromoAmber)
                                )
                                Text("Signaler ⚠ NON RÉFÉRENCÉ et autoriser", fontSize = 11.sp, color = TextPrimary)
                            }
                        }

                        item {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = unknownPolicy == UnknownProductPolicy.BLOCK,
                                    onClick = { unknownPolicy = UnknownProductPolicy.BLOCK },
                                    colors = RadioButtonDefaults.colors(selectedColor = RetailPromoAmber)
                                )
                                Text("Bloquer l'ajout d'articles non référencés", fontSize = 11.sp, color = TextPrimary)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        onSaveSettings(scanMode, profile, unknownPolicy, sound, vibration)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RetailEmerald)
                ) {
                    Text("APPLIQUER LA CONFIGURATION", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun ModeOption(title: String, subtitle: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, if (isSelected) BrandSkyBlue else BrandSlateBorder, RoundedCornerShape(8.dp))
            .background(if (isSelected) BrandSkyBlue.copy(alpha = 0.15f) else BrandNavyDark)
            .clickable { onClick() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = BrandSkyBlue)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(subtitle, fontSize = 10.sp, color = TextSecondary)
        }
    }
}
