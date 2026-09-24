package com.example.ui.dialogs

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.domain.model.DuplicateRule
import com.example.domain.model.LotProfile
import com.example.model.LabelTemplate
import com.example.ui.scan.AVAILABLE_TEMPLATES
import com.example.ui.theme.BrandNavyDark
import com.example.ui.theme.BrandSkyBlue
import com.example.ui.theme.BrandSlateBorder
import com.example.ui.theme.BrandSlateCard
import com.example.ui.theme.RetailEmerald
import com.example.ui.theme.RetailPromoAmber
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

val AVAILABLE_DEPARTMENTS = listOf(
    "Épicerie",
    "Boissons & Liquides",
    "Frais & Crèmerie",
    "Bazar & Maison",
    "Textile",
    "Cash & Carry"
)

val LOT_PROFILES = listOf(
    LotProfile("prof_rayon", "Étiquettes rayon", "Balisage régulier 50x30", "Épicerie", "template_38x70", false, DuplicateRule.INCREMENT_QTY, "NORMAL"),
    LotProfile("prof_promo", "Promo Jaune", "Stickers promo et stop-rayons", "Boissons & Liquides", "template_promo_40x80", true, DuplicateRule.INCREMENT_QTY, "PROMO"),
    LotProfile("prof_chgt_prix", "Changement de prix", "Actualisation suite à MAJ centrale", "Épicerie", "template_38x70", false, DuplicateRule.NEW_LINE, "URGENT"),
    LotProfile("prof_inventaire", "Inventaire visuel", "Comptage et vérification de facing", "Frais & Crèmerie", "template_a5", false, DuplicateRule.INCREMENT_QTY, "INVENTAIRE"),
    LotProfile("prof_cash", "Cash & Carry", "Grands formats et palettes", "Cash & Carry", "template_a5", false, DuplicateRule.INCREMENT_QTY, "NORMAL")
)

/**
 * Points 6, 7, 8 : Wizard de création de lot intelligent (3 étapes max) avec profils
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LotWizardDialog(
    currentOperator: String,
    currentCatalogVersion: String,
    onDismiss: () -> Unit,
    onCreateLot: (
        name: String,
        description: String,
        department: String,
        templateId: String,
        operator: String,
        isPromo: Boolean,
        requiresTemplate: Boolean,
        requiresQuantity: Boolean,
        duplicateRule: DuplicateRule,
        colorTag: String,
        catalogVersion: String
    ) -> Unit
) {
    var currentStep by remember { mutableIntStateOf(1) }

    // Étape 1 : Identification
    var lotName by remember { mutableStateOf("Changement prix Épicerie") }
    var lotDescription by remember { mutableStateOf("") }
    var selectedProfileId by remember { mutableStateOf<String?>(null) }
    var colorTag by remember { mutableStateOf("NORMAL") }

    // Étape 2 : Contexte
    var selectedDepartment by remember { mutableStateOf(AVAILABLE_DEPARTMENTS[0]) }
    var selectedTemplateId by remember { mutableStateOf(AVAILABLE_TEMPLATES[0].id) }
    var operatorName by remember { mutableStateOf(currentOperator) }
    var deptExpanded by remember { mutableStateOf(false) }
    var templateExpanded by remember { mutableStateOf(false) }

    // Étape 3 : Options
    var useDefaultTemplate by remember { mutableStateOf(true) }
    var isPromoMode by remember { mutableStateOf(false) }
    var requireTemplateBeforeExport by remember { mutableStateOf(true) }
    var requireQuantityBeforeExport by remember { mutableStateOf(true) }
    var duplicateRule by remember { mutableStateOf(DuplicateRule.INCREMENT_QTY) }

    fun applyProfile(profile: LotProfile) {
        selectedProfileId = profile.id
        lotName = profile.name
        lotDescription = profile.description
        selectedDepartment = profile.defaultDepartment
        selectedTemplateId = profile.defaultTemplateId
        isPromoMode = profile.isPromoMode
        duplicateRule = profile.duplicateRule
        colorTag = profile.colorTag
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = BrandSlateCard,
            border = BorderStroke(1.dp, BrandSlateBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Barre d'étapes 1 / 2 / 3
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "NOUVEAU LOT • ÉTAPE $currentStep / 3",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandSkyBlue
                        )
                        Text(
                            text = when (currentStep) {
                                1 -> "1. Identification du lot"
                                2 -> "2. Contexte & Rayon"
                                else -> "3. Règles & Validation"
                            },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Fermer", tint = TextSecondary)
                    }
                }

                // Indicateur de progression visuel
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (step in 1..3) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (step <= currentStep) BrandSkyBlue else BrandSlateBorder)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Contenu dynamique selon l'étape
                when (currentStep) {
                    1 -> {
                        // ÉTAPE 1 : Identification & Profils rapides
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "Profils de lot recommandés :",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary
                            )

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(LOT_PROFILES) { prof ->
                                    val isSelected = selectedProfileId == prof.id
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .border(
                                                1.dp,
                                                if (isSelected) BrandSkyBlue else BrandSlateBorder,
                                                RoundedCornerShape(10.dp)
                                            )
                                            .background(if (isSelected) BrandSkyBlue.copy(alpha = 0.2f) else Color.Transparent)
                                            .clickable { applyProfile(prof) }
                                            .padding(horizontal = 10.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = prof.name,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) BrandSkyBlue else TextPrimary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            OutlinedTextField(
                                value = lotName,
                                onValueChange = { lotName = it },
                                label = { Text("Nom du lot *") },
                                modifier = Modifier.fillMaxWidth().testTag("input_lot_name"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BrandSkyBlue,
                                    unfocusedBorderColor = BrandSlateBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = lotDescription,
                                onValueChange = { lotDescription = it },
                                label = { Text("Description facultative") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BrandSkyBlue,
                                    unfocusedBorderColor = BrandSlateBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                maxLines = 2
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Tag visuel :",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf("NORMAL", "PROMO", "URGENT", "INVENTAIRE").forEach { tag ->
                                        val isTagSelected = colorTag == tag
                                        val tagColor = when (tag) {
                                            "PROMO" -> RetailEmerald
                                            "URGENT" -> Color(0xFFEF4444)
                                            "INVENTAIRE" -> RetailPromoAmber
                                            else -> BrandSkyBlue
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .border(1.dp, if (isTagSelected) tagColor else Color.Transparent, RoundedCornerShape(6.dp))
                                                .background(tagColor.copy(alpha = if (isTagSelected) 0.3f else 0.1f))
                                                .clickable { colorTag = tag }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(text = tag, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = tagColor)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        // ÉTAPE 2 : Contexte (Département, Gabarit, Opérateur)
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            // Sélecteur Département
                            ExposedDropdownMenuBox(
                                expanded = deptExpanded,
                                onExpandedChange = { deptExpanded = !deptExpanded }
                            ) {
                                OutlinedTextField(
                                    value = selectedDepartment,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Département") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = deptExpanded) },
                                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = BrandSkyBlue,
                                        unfocusedBorderColor = BrandSlateBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = deptExpanded,
                                    onDismissRequest = { deptExpanded = false }
                                ) {
                                    AVAILABLE_DEPARTMENTS.forEach { dept ->
                                        DropdownMenuItem(
                                            text = { Text(dept) },
                                            onClick = {
                                                selectedDepartment = dept
                                                deptExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Sélecteur Gabarit par défaut
                            ExposedDropdownMenuBox(
                                expanded = templateExpanded,
                                onExpandedChange = { templateExpanded = !templateExpanded }
                            ) {
                                val currentTpl = AVAILABLE_TEMPLATES.find { it.id == selectedTemplateId } ?: AVAILABLE_TEMPLATES[0]
                                OutlinedTextField(
                                    value = currentTpl.name,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Gabarit par défaut") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = templateExpanded) },
                                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = BrandSkyBlue,
                                        unfocusedBorderColor = BrandSlateBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = templateExpanded,
                                    onDismissRequest = { templateExpanded = false }
                                ) {
                                    AVAILABLE_TEMPLATES.forEach { tpl ->
                                        DropdownMenuItem(
                                            text = { Text("${tpl.name} (${tpl.dimensions})") },
                                            onClick = {
                                                selectedTemplateId = tpl.id
                                                templateExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Opérateur
                            OutlinedTextField(
                                value = operatorName,
                                onValueChange = { operatorName = it },
                                label = { Text("Opérateur responsable") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = BrandSkyBlue) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BrandSkyBlue,
                                    unfocusedBorderColor = BrandSlateBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                singleLine = true
                            )

                            // Point 6 : Mention du catalogue figé
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF0F172A))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = "🔒 Le catalogue utilisé pour ce lot sera figé sur la version : $currentCatalogVersion",
                                    fontSize = 11.sp,
                                    color = BrandSkyBlue
                                )
                            }
                        }
                    }

                    3 -> {
                        // ÉTAPE 3 : Options de scan & doublons
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().clickable { useDefaultTemplate = !useDefaultTemplate }
                            ) {
                                Checkbox(
                                    checked = useDefaultTemplate,
                                    onCheckedChange = { useDefaultTemplate = it },
                                    colors = CheckboxDefaults.colors(checkedColor = BrandSkyBlue)
                                )
                                Text("Appliquer ce gabarit par défaut aux articles scannés", fontSize = 13.sp, color = TextPrimary)
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().clickable { isPromoMode = !isPromoMode }
                            ) {
                                Checkbox(
                                    checked = isPromoMode,
                                    onCheckedChange = { isPromoMode = it },
                                    colors = CheckboxDefaults.colors(checkedColor = RetailPromoAmber)
                                )
                                Text("Activer le mode promotion par défaut", fontSize = 13.sp, color = TextPrimary)
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().clickable { requireTemplateBeforeExport = !requireTemplateBeforeExport }
                            ) {
                                Checkbox(
                                    checked = requireTemplateBeforeExport,
                                    onCheckedChange = { requireTemplateBeforeExport = it },
                                    colors = CheckboxDefaults.colors(checkedColor = BrandSkyBlue)
                                )
                                Text("Exiger un gabarit pour chaque article avant export", fontSize = 13.sp, color = TextPrimary)
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().clickable { requireQuantityBeforeExport = !requireQuantityBeforeExport }
                            ) {
                                Checkbox(
                                    checked = requireQuantityBeforeExport,
                                    onCheckedChange = { requireQuantityBeforeExport = it },
                                    colors = CheckboxDefaults.colors(checkedColor = BrandSkyBlue)
                                )
                                Text("Exiger une quantité > 0 avant export", fontSize = 13.sp, color = TextPrimary)
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Comportement si article déjà présent (Point 10) :",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandSkyBlue
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = duplicateRule == DuplicateRule.INCREMENT_QTY,
                                    onClick = { duplicateRule = DuplicateRule.INCREMENT_QTY },
                                    colors = RadioButtonDefaults.colors(selectedColor = BrandSkyBlue)
                                )
                                Text("Augmenter la quantité (recommandé)", fontSize = 12.sp, color = TextPrimary)
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = duplicateRule == DuplicateRule.NEW_LINE,
                                    onClick = { duplicateRule = DuplicateRule.NEW_LINE },
                                    colors = RadioButtonDefaults.colors(selectedColor = BrandSkyBlue)
                                )
                                Text("Créer une nouvelle ligne (multi-gabarits)", fontSize = 12.sp, color = TextPrimary)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Boutons Précédent / Suivant / Créer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (currentStep > 1) {
                        OutlinedButton(
                            onClick = { currentStep -= 1 },
                            modifier = Modifier.weight(1f).height(46.dp),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, BrandSlateBorder),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                        ) {
                            Text("PRÉCÉDENT", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Button(
                        onClick = {
                            if (currentStep < 3) {
                                currentStep += 1
                            } else {
                                onCreateLot(
                                    lotName.ifBlank { "Lot Sans Nom" },
                                    lotDescription,
                                    selectedDepartment,
                                    selectedTemplateId,
                                    operatorName,
                                    isPromoMode,
                                    requireTemplateBeforeExport,
                                    requireQuantityBeforeExport,
                                    duplicateRule,
                                    colorTag,
                                    currentCatalogVersion
                                )
                            }
                        },
                        modifier = Modifier.weight(1f).height(46.dp).testTag("btn_wizard_next"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentStep == 3) RetailEmerald else BrandSkyBlue,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = if (currentStep < 3) "SUIVANT" else "CRÉER LE LOT",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
