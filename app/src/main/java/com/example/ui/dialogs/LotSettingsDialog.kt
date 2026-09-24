package com.example.ui.dialogs

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.scan.AVAILABLE_TEMPLATES
import com.example.ui.scan.ScanUiState
import com.example.ui.theme.BrandNavyDark
import com.example.ui.theme.BrandSkyBlue
import com.example.ui.theme.BrandSkyLight
import com.example.ui.theme.BrandSlateBorder
import com.example.ui.theme.BrandSlateCard
import com.example.ui.theme.BrandSlateDark
import com.example.ui.theme.RetailEmerald
import com.example.ui.theme.RetailErrorRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun LotSettingsDialog(
    state: ScanUiState,
    onDismiss: () -> Unit,
    onSave: (lotName: String, operatorName: String, templateId: String) -> Unit,
    onClearLot: () -> Unit
) {
    var lotName by remember { mutableStateOf(state.lotName) }
    var operatorName by remember { mutableStateOf(state.operatorName) }
    var selectedTemplateId by remember { mutableStateOf(state.targetTemplateId) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BrandSlateCard,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Label, contentDescription = null, tint = BrandSkyLight)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Configuration du Lot d'Impression",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = lotName,
                    onValueChange = { lotName = it },
                    label = { Text("Nom du lot") },
                    modifier = Modifier.fillMaxWidth().testTag("dialog_lot_name_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandSkyLight,
                        unfocusedBorderColor = BrandSlateBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                OutlinedTextField(
                    value = operatorName,
                    onValueChange = { operatorName = it },
                    label = { Text("Opérateur magasin") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TextSecondary) },
                    modifier = Modifier.fillMaxWidth().testTag("dialog_operator_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandSkyLight,
                        unfocusedBorderColor = BrandSlateBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                // Info terminal durci / Android
                Card(
                    colors = CardDefaults.cardColors(containerColor = BrandSlateDark),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Devices, contentDescription = null, tint = RetailEmerald, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Terminal : ${state.deviceName}",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Mode de capture : ${if (state.deviceType == "native_terminal") "Zebra DataWedge Pro" else "Caméra IA / ML Kit"}",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Choix du gabarit d'étiquette
                Text(
                    text = "Gabarit d'étiquette d'impression cible :",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                AVAILABLE_TEMPLATES.forEach { template ->
                    val isSelected = template.id == selectedTemplateId
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) BrandSkyBlue.copy(alpha = 0.2f) else BrandSlateDark)
                            .border(1.dp, if (isSelected) BrandSkyLight else BrandSlateBorder, RoundedCornerShape(8.dp))
                            .clickable { selectedTemplateId = template.id }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                            .testTag("dialog_template_${template.id}")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedTemplateId = template.id },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = BrandSkyLight,
                                    unselectedColor = TextSecondary
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = template.name,
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Dimensions : ${template.dimensions}",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Bouton vider le lot
                OutlinedButton(
                    onClick = {
                        onClearLot()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth().testTag("dialog_clear_lot_btn"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RetailErrorRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Vider entièrement ce lot", fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(lotName, operatorName, selectedTemplateId)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandSkyBlue),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("dialog_save_button")
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Enregistrer")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Annuler")
            }
        }
    )
}
