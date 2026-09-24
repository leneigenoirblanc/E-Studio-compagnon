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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.window.Dialog
import com.example.ui.scan.AVAILABLE_TEMPLATES
import com.example.ui.theme.BrandNavyDark
import com.example.ui.theme.BrandSkyBlue
import com.example.ui.theme.BrandSlateBorder
import com.example.ui.theme.BrandSlateCard
import com.example.ui.theme.RetailEmerald
import com.example.ui.theme.RetailErrorRed
import com.example.ui.theme.RetailPromoAmber
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

data class VisualTagOption(
    val id: String,
    val label: String,
    val color: Color
)

val VISUAL_TAG_OPTIONS = listOf(
    VisualTagOption("BLUE", "Rayon Frais", BrandSkyBlue),
    VisualTagOption("GREEN", "Épicerie", RetailEmerald),
    VisualTagOption("AMBER", "Promo Semaine", RetailPromoAmber),
    VisualTagOption("RED", "Démarque / Soldes", RetailErrorRed),
    VisualTagOption("PURPLE", "Hygiène & Beauté", Color(0xFFA855F7)),
    VisualTagOption("ORANGE", "Liquides & Boissons", Color(0xFFF97316))
)

/**
 * S07 — Assistant (Wizard) de création rapide d'une Table / Lot de scan
 * Permet :
 * - Nom de la table (obligatoire)
 * - Balisage visuel par couleur (Tag visuel permettant de distinguer 2 tables ayant le même nom)
 * - Rayon / Département
 * - Gabarit d'étiquettes par défaut
 * - Action : "CRÉER ET SCANNER" -> ouvre directement l'interface de scan (S08)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTableWizardDialog(
    onDismiss: () -> Unit,
    onCreateTable: (name: String, colorTag: String, department: String, templateId: String) -> Unit
) {
    var tableName by remember { mutableStateOf("Scan Rayon - ${java.text.SimpleDateFormat("HH:mm", java.util.Locale.FRANCE).format(java.util.Date())}") }
    var selectedTag by remember { mutableStateOf(VISUAL_TAG_OPTIONS[0]) }
    var selectedDepartment by remember { mutableStateOf("Épicerie") }
    var selectedTemplateId by remember { mutableStateOf(AVAILABLE_TEMPLATES[0].id) }
    var templateDropdownExpanded by remember { mutableStateOf(false) }

    val templateLabel = AVAILABLE_TEMPLATES.find { it.id == selectedTemplateId }?.name ?: selectedTemplateId

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = BrandSlateCard,
            border = BorderStroke(1.dp, BrandSlateBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // En-tête
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(RetailEmerald.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddCircle,
                                contentDescription = null,
                                tint = RetailEmerald,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Créer une Table / Lot",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Nouvelle session de scan locale",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fermer",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Champ 1 : Nom de la table
                Text(
                    text = "Nom de la table / lot * :",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = tableName,
                    onValueChange = { tableName = it },
                    placeholder = { Text("Ex: Changement prix Épicerie", color = TextTertiary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("table_name_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = BrandNavyDark,
                        unfocusedContainerColor = BrandNavyDark,
                        focusedBorderColor = BrandSkyBlue,
                        unfocusedBorderColor = BrandSlateBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Champ 2 : Balisage visuel / Tag de couleur
                Text(
                    text = "Balisage visuel (Tag couleur) :",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Permet de reconnaître facilement la table même si plusieurs tables ont le même nom.",
                    fontSize = 10.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VISUAL_TAG_OPTIONS.forEach { tagOpt ->
                        val isSelected = selectedTag.id == tagOpt.id
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) tagOpt.color.copy(alpha = 0.25f) else BrandNavyDark)
                                .border(
                                    1.5.dp,
                                    if (isSelected) tagOpt.color else BrandSlateBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    selectedTag = tagOpt
                                    selectedDepartment = tagOpt.label
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(tagOpt.color)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tag sélectionné : ${selectedTag.label}",
                    fontSize = 11.sp,
                    color = selectedTag.color,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Champ 3 : Gabarit d'étiquette par défaut
                Text(
                    text = "Gabarit d'impression par défaut :",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))

                ExposedDropdownMenuBox(
                    expanded = templateDropdownExpanded,
                    onExpandedChange = { templateDropdownExpanded = !templateDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = templateLabel,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = templateDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = BrandNavyDark,
                            unfocusedContainerColor = BrandNavyDark,
                            focusedBorderColor = BrandSkyBlue,
                            unfocusedBorderColor = BrandSlateBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    )

                    ExposedDropdownMenu(
                        expanded = templateDropdownExpanded,
                        onDismissRequest = { templateDropdownExpanded = false },
                        modifier = Modifier.background(BrandSlateCard)
                    ) {
                        AVAILABLE_TEMPLATES.forEach { tmpl ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(tmpl.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        Text("${tmpl.dimensions} • Type ${tmpl.type}", fontSize = 10.sp, color = TextSecondary)
                                    }
                                },
                                onClick = {
                                    selectedTemplateId = tmpl.id
                                    templateDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Boutons d'Action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BrandSlateBorder)
                    ) {
                        Text("Annuler", color = TextSecondary, fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            if (tableName.isNotBlank()) {
                                onCreateTable(
                                    tableName.trim(),
                                    selectedTag.id,
                                    selectedDepartment,
                                    selectedTemplateId
                                )
                            }
                        },
                        enabled = tableName.isNotBlank(),
                        modifier = Modifier
                            .weight(1.4f)
                            .height(48.dp)
                            .testTag("create_and_scan_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RetailEmerald)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "CRÉER ET SCANNER",
                            color = Color.Black,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
