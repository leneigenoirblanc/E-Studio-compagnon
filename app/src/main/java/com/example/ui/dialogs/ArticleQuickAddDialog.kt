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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import com.example.domain.model.PrintInstruction
import com.example.domain.repository.ResolvedProduct
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

/**
 * Points 11, 12, 13, 16 : Fiche article après scan
 * - Quantités rapides [ 1 ] [ 2 ] [ 5 ] [ 10 ] [ 20 ] + personnalisée
 * - Multi-gabarits par article (PrintInstruction[])
 * - Boutons [ ANNULER ], [ AJOUTER ], [ AJOUTER & MODIFIER ]
 * - Mention ⚠ NON RÉFÉRENCÉ si produit inconnu
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleQuickAddDialog(
    barcode: String,
    resolvedProduct: ResolvedProduct?,
    defaultTemplateId: String,
    onDismiss: () -> Unit,
    onAddImmediate: (quantity: Int, templateId: String, instructions: List<PrintInstruction>, facing: Int) -> Unit,
    onAddAndEdit: (quantity: Int, templateId: String, instructions: List<PrintInstruction>, facing: Int) -> Unit
) {
    val isUnknown = resolvedProduct == null
    val designation = resolvedProduct?.snapshot?.designation ?: "Article non référencé"
    val regularPriceFormatted = resolvedProduct?.pricing?.regularPrice?.formatted() ?: "-- €"
    val promoPriceFormatted = resolvedProduct?.pricing?.promoPrice?.formatted()

    var primaryQuantity by remember { mutableIntStateOf(resolvedProduct?.snapshot?.facing?.coerceAtLeast(1) ?: 1) }
    var primaryTemplateId by remember { mutableStateOf(defaultTemplateId) }
    var facingCount by remember { mutableIntStateOf(1) }
    var customQtyInput by remember { mutableStateOf("") }
    var templateDropdownExpanded by remember { mutableStateOf(false) }

    // Multi-gabarits additionnels (Point 11)
    val extraInstructions = remember { mutableStateListOf<PrintInstruction>() }
    var showAddExtraTemplate by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = BrandSlateCard,
            border = BorderStroke(1.dp, if (isUnknown) RetailPromoAmber else BrandSlateBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // En-tête avec Statut (Référencé ou ⚠ Inconnu)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isUnknown) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(RetailPromoAmber.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = RetailPromoAmber, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("⚠ NON RÉFÉRENCÉ", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = RetailPromoAmber)
                        }
                    } else {
                        Text(
                            text = resolvedProduct.snapshot.department.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandSkyBlue
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(26.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Désignation & Code-barres
                Text(
                    text = designation,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "EAN : $barcode",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Prix
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = regularPriceFormatted,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (promoPriceFormatted != null) TextSecondary else RetailEmerald
                    )
                    if (promoPriceFormatted != null) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(RetailPromoAmber.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("PROMO : $promoPriceFormatted", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RetailPromoAmber)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Choix du gabarit principal
                val currentTpl = AVAILABLE_TEMPLATES.find { it.id == primaryTemplateId } ?: AVAILABLE_TEMPLATES[0]
                ExposedDropdownMenuBox(
                    expanded = templateDropdownExpanded,
                    onExpandedChange = { templateDropdownExpanded = !templateDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = "${currentTpl.name} (${currentTpl.dimensions})",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Gabarit d'étiquette principal") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = templateDropdownExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandSkyBlue,
                            unfocusedBorderColor = BrandSlateBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = templateDropdownExpanded,
                        onDismissRequest = { templateDropdownExpanded = false }
                    ) {
                        AVAILABLE_TEMPLATES.forEach { tpl ->
                            DropdownMenuItem(
                                text = { Text("${tpl.name} (${tpl.dimensions})") },
                                onClick = {
                                    primaryTemplateId = tpl.id
                                    templateDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quantités rapides (Point 12) : [ 1 ] [ 2 ] [ 5 ] [ 10 ] [ 20 ] + Personnalisée
                Text(
                    text = "Quantité d'étiquettes : $primaryQuantity",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandSkyBlue
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(1, 2, 5, 10, 20).forEach { qty ->
                        val isSelected = primaryQuantity == qty
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, if (isSelected) BrandSkyBlue else BrandSlateBorder, RoundedCornerShape(8.dp))
                                .background(if (isSelected) BrandSkyBlue else BrandNavyDark)
                                .clickable {
                                    primaryQuantity = qty
                                    customQtyInput = ""
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$qty",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else TextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Saisie personnalisée
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = customQtyInput,
                        onValueChange = {
                            customQtyInput = it
                            val parsed = it.toIntOrNull()
                            if (parsed != null && parsed > 0) {
                                primaryQuantity = parsed
                            }
                        },
                        label = { Text("Quantité personnalisée") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandSkyBlue,
                            unfocusedBorderColor = BrandSlateBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Facing", fontSize = 10.sp, color = TextSecondary)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { facingCount = (facingCount - 1).coerceAtLeast(1) }, modifier = Modifier.size(28.dp)) {
                                Text("-", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            Text("$facingCount", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            IconButton(onClick = { facingCount += 1 }, modifier = Modifier.size(28.dp)) {
                                Text("+", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                        }
                    }
                }

                // Section Multi-gabarits par article (Point 11)
                if (extraInstructions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Gabarits additionnels pour cet article :", fontSize = 11.sp, color = TextSecondary)
                    extraInstructions.forEachIndexed { index, instr ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(BrandNavyDark)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("• ${instr.templateName} (x${instr.quantity})", fontSize = 11.sp, color = TextPrimary)
                            IconButton(onClick = { extraInstructions.removeAt(index) }, modifier = Modifier.size(20.dp)) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = RetailErrorRed, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.clickable {
                        extraInstructions.add(
                            PrintInstruction(
                                templateId = "template_promo_40x80",
                                templateName = "Stop-rayon promo (40x80)",
                                quantity = 2
                            )
                        )
                    },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = BrandSkyBlue, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Ajouter un gabarit (ex: Stop-rayon promo)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandSkyBlue)
                }

                Spacer(modifier = Modifier.height(18.dp))

                // LES 3 BOUTONS DU POINT 13 : [ ANNULER ], [ AJOUTER ], [ AJOUTER & MODIFIER ]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, BrandSlateBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                        modifier = Modifier.weight(1f).height(46.dp)
                    ) {
                        Text("ANNULER", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            val instructionsList = listOf(
                                PrintInstruction(primaryTemplateId, currentTpl.name, primaryQuantity)
                            ) + extraInstructions
                            onAddAndEdit(primaryQuantity, primaryTemplateId, instructionsList, facingCount)
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, BrandSkyBlue.copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandSkyBlue),
                        modifier = Modifier.weight(1.3f).height(46.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("AJOUTER & ÉDITER", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val instructionsList = listOf(
                                PrintInstruction(primaryTemplateId, currentTpl.name, primaryQuantity)
                            ) + extraInstructions
                            onAddImmediate(primaryQuantity, primaryTemplateId, instructionsList, facingCount)
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RetailEmerald, contentColor = Color.White),
                        modifier = Modifier.weight(1.2f).height(46.dp).testTag("btn_quick_add_confirm")
                    ) {
                        Text("AJOUTER", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
