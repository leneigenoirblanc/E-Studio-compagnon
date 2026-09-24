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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
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

@Composable
fun ScannerSettingsDialog(
    isAutoScan: Boolean,
    isAutoValidate: Boolean,
    autoScanDelayMs: Long,
    soundEnabled: Boolean,
    vibrationEnabled: Boolean,
    onSaveSettings: (
        autoScan: Boolean,
        autoValidate: Boolean,
        autoScanDelay: Long,
        sound: Boolean,
        vibration: Boolean,
        enabledSymbologies: Set<String>
    ) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Détection & Délais, 1 = Types de Code-Barres GMS

    var autoScan by remember { mutableStateOf(isAutoScan) }
    var autoValidate by remember { mutableStateOf(isAutoValidate) }
    var delayMs by remember { mutableLongStateOf(autoScanDelayMs) }
    var sound by remember { mutableStateOf(soundEnabled) }
    var vibration by remember { mutableStateOf(vibrationEnabled) }

    // Symbologies actives (Toggles individuels)
    val activeSymbologies = remember {
        mutableStateMapOf(
            "EAN-13" to true,
            "UPC-A" to true,
            "EAN-8" to true,
            "UPC-E" to true,
            "Code 128" to true,
            "GS1-128" to true,
            "ITF-14" to true,
            "QR Code" to true,
            "GS1 DataMatrix" to true
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = BrandSlateCard,
            border = BorderStroke(1.dp, BrandSlateBorder),
            modifier = Modifier
                .fillMaxWidth()
                .height(560.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // En-tête
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(BrandSkyBlue.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = BrandSkyBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "RÉGLAGES DU SCANNER",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandSkyBlue
                            )
                            Text(
                                text = "Modes & Symbologies GMS",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Onglets
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = BrandNavyDark,
                    contentColor = BrandSkyBlue,
                    modifier = Modifier.clip(RoundedCornerShape(10.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Modes & Délais", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Types Codes (GMS)", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (selectedTab == 0) {
                        // Onglet 1 : Modes & Délais
                        item {
                            Text(
                                text = "Mode de déclenchement :",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        item {
                            SettingToggleCard(
                                title = "Auto Scan Caméra",
                                subtitle = "Scanne en continu avec délai dès qu'un code est visible",
                                isChecked = autoScan,
                                onCheckedChange = { autoScan = it }
                            )
                        }

                        item {
                            SettingToggleCard(
                                title = "Auto Validate Scan (Ajout direct)",
                                subtitle = if (autoValidate) "Ajout direct sans popup d'édition" else "Ouvre la fiche pour éditer le gabarit et quantité",
                                isChecked = autoValidate,
                                onCheckedChange = { autoValidate = it }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Délai de temporisation Auto-Scan :",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(BrandNavyDark)
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val delays = listOf(
                                    300L to "300 ms (Ultra-rapide)",
                                    500L to "500 ms (Rapide)",
                                    800L to "800 ms (Recommandé / Standard)",
                                    1200L to "1200 ms (Prudent)",
                                    2000L to "2000 ms (Lent / Manuel assisté)"
                                )
                                delays.forEach { (d, label) ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable { delayMs = d }
                                            .padding(vertical = 4.dp)
                                    ) {
                                        RadioButton(
                                            selected = delayMs == d,
                                            onClick = { delayMs = d },
                                            colors = RadioButtonDefaults.colors(selectedColor = BrandSkyBlue)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = label,
                                            fontSize = 12.sp,
                                            color = if (delayMs == d) BrandSkyBlue else TextPrimary,
                                            fontWeight = if (delayMs == d) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Retours Opérateur :",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        item {
                            SettingToggleCard(
                                title = "Bip sonore caisse (70 ms)",
                                subtitle = "Confirmation acoustique à chaque scan réussi",
                                isChecked = sound,
                                onCheckedChange = { sound = it }
                            )
                        }

                        item {
                            SettingToggleCard(
                                title = "Vibration haptique (45 ms)",
                                subtitle = "Impulsion tactile nette sur le smartphone",
                                isChecked = vibration,
                                onCheckedChange = { vibration = it }
                            )
                        }
                    } else {
                        // Onglet 2 : Symbologies GMS
                        item {
                            Text(
                                text = "Codes-barres Supermarché & GMS Supportés :",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        val gmsCodes = listOf(
                            Triple("EAN-13", "Standard européen grande distribution & PGC", true),
                            Triple("UPC-A", "Standard universel / Articles d'importation", true),
                            Triple("EAN-8", "Petits formats, confiserie & cosmétique", true),
                            Triple("UPC-E", "Version compacte UPC pour petits emballages", true),
                            Triple("Code 128", "Logistique, cartons, codes internes magasin", true),
                            Triple("GS1-128", "Identifiants applicatifs GS1, DLUO, n° de lot", true),
                            Triple("ITF-14", "Entrelacé 2 parmi 5 (Cartons et regroupements)", true),
                            Triple("QR Code", "QR Code standard & GS1 Digital Link", true),
                            Triple("GS1 DataMatrix", "Parapharmacie, textile, fruits et légumes", true)
                        )

                        gmsCodes.forEach { (codeKey, description, supported) ->
                            item {
                                val isChecked = activeSymbologies[codeKey] == true
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(BrandNavyDark)
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = codeKey,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(RetailEmerald.copy(alpha = 0.2f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text("GMS ACTIF", fontSize = 9.sp, color = RetailEmerald, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        Text(
                                            text = description,
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }
                                    Switch(
                                        checked = isChecked,
                                        onCheckedChange = { activeSymbologies[codeKey] = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = BrandSkyBlue,
                                            checkedTrackColor = BrandSkyBlue.copy(alpha = 0.4f)
                                        )
                                    )
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Codes Spécialisés / Hors GMS :",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        val nonGmsCodes = listOf(
                            Triple("SSCC", "Serial Shipping Container Code (Palettes)", "Non utilisé en supermarché"),
                            Triple("PDF417", "Cartes d'identité, billetterie", "Non disponible en GMS"),
                            Triple("Aztec Code", "Titres de transport & billets train", "Non disponible en GMS"),
                            Triple("Code 39", "Automobile & industrie lourde", "Rarement utilisé"),
                            Triple("Codabar", "Banques de sang & bibliothèques", "Non disponible en GMS")
                        )

                        nonGmsCodes.forEach { (codeKey, description, reason) ->
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(BrandNavyDark.copy(alpha = 0.6f))
                                        .border(1.dp, BrandSlateBorder, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = codeKey,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = TextSecondary
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(RetailPromoAmber.copy(alpha = 0.15f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(reason, fontSize = 9.sp, color = RetailPromoAmber, fontWeight = FontWeight.Medium)
                                            }
                                        }
                                        Text(
                                            text = description,
                                            fontSize = 10.sp,
                                            color = TextSecondary.copy(alpha = 0.7f)
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Non disponible",
                                        tint = TextSecondary.copy(alpha = 0.5f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        val enabled = activeSymbologies.filterValues { it }.keys.toSet()
                        onSaveSettings(autoScan, autoValidate, delayMs, sound, vibration, enabled)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RetailEmerald)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "APPLIQUER LES RÉGLAGES",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingToggleCard(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(BrandNavyDark)
            .clickable { onCheckedChange(!isChecked) }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = BrandSkyBlue,
                checkedTrackColor = BrandSkyBlue.copy(alpha = 0.4f)
            )
        )
    }
}
