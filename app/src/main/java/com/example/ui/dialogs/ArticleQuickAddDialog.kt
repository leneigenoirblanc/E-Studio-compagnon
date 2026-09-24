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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.domain.repository.ResolvedProduct
import com.example.model.MobileScanItem
import com.example.ui.scan.AVAILABLE_TEMPLATES
import com.example.ui.theme.BrandNavyDark
import com.example.ui.theme.BrandSkyBlue
import com.example.ui.theme.BrandSkyLight
import com.example.ui.theme.BrandSlateBorder
import com.example.ui.theme.BrandSlateCard
import com.example.ui.theme.RetailEmerald
import com.example.ui.theme.RetailPromoAmber
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Boîte de dialogue universelle de fiche article scanné
 * Utilisée pour :
 * 1. Mode AJOUT (Add) après un scan manuel ou semi-automatique
 * 2. Mode ÉDITION (Save) lorsqu'on clique sur le bouton "Edit" d'un article déjà scanné
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleQuickAddDialog(
    barcode: String,
    resolvedProduct: ResolvedProduct? = null,
    existingItem: MobileScanItem? = null,
    defaultTemplateId: String = "template_38x70",
    isEditMode: Boolean = existingItem != null,
    onDismiss: () -> Unit,
    onConfirm: (quantity: Int, templateId: String, facing: Int, customDesignation: String?, customPrice: Double?) -> Unit
) {
    val isUnknown = (resolvedProduct == null && existingItem == null) || (existingItem?.isUnknown == true)

    val initialDesignation = existingItem?.designation
        ?: resolvedProduct?.snapshot?.designation
        ?: "Article non référencé"

    val initialPrice = existingItem?.price
        ?: resolvedProduct?.pricing?.regularPrice?.toDouble()
        ?: 0.0

    val promoPrice = resolvedProduct?.pricing?.promoPrice?.toDouble()

    var quantity by remember { mutableIntStateOf(existingItem?.quantity ?: 1) }
    var templateId by remember { mutableStateOf(existingItem?.templateId ?: defaultTemplateId) }
    var facingCount by remember { mutableIntStateOf(existingItem?.facing ?: 1) }
    var designationInput by remember { mutableStateOf(initialDesignation) }
    var priceInput by remember { mutableStateOf(if (initialPrice > 0.0) String.format("%.2f", initialPrice) else "") }
    var templateDropdownExpanded by remember { mutableStateOf(false) }

    val templateLabel = AVAILABLE_TEMPLATES.find { it.id == templateId }?.name ?: templateId

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = BrandSlateCard,
            border = BorderStroke(1.dp, if (isUnknown) RetailPromoAmber else BrandSlateBorder),
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
                // En-tête avec titre et bouton fermer
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
                                .background(if (isEditMode) BrandSkyBlue.copy(alpha = 0.2f) else RetailEmerald.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isEditMode) Icons.Default.Edit else Icons.Default.QrCode,
                                contentDescription = null,
                                tint = if (isEditMode) BrandSkyBlue else RetailEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isEditMode) "Modifier l'article" else "Article Scanné",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = barcode,
                                color = BrandSkyBlue,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
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

                Spacer(modifier = Modifier.height(14.dp))

                // Avertissement si article non référencé
                if (isUnknown) {
                    Surface(
                        color = RetailPromoAmber.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, RetailPromoAmber.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = RetailPromoAmber,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Article non trouvé dans la base locale. Vous pouvez ajuster son nom et son prix ci-dessous.",
                                color = RetailPromoAmber,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Désignation (Modifiable si non référencé ou en mode édition)
                Text(
                    text = "Désignation de l'article :",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = designationInput,
                    onValueChange = { designationInput = it },
                    modifier = Modifier.fillMaxWidth(),
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

                Spacer(modifier = Modifier.height(12.dp))

                // Prix & Rayon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Prix (€) :",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = priceInput,
                            onValueChange = { priceInput = it },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = BrandNavyDark,
                                unfocusedContainerColor = BrandNavyDark,
                                focusedBorderColor = BrandSkyBlue,
                                unfocusedBorderColor = BrandSlateBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Facing (Facettes) :",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(BrandNavyDark)
                                .border(1.dp, BrandSlateBorder, RoundedCornerShape(10.dp))
                                .padding(horizontal = 6.dp)
                        ) {
                            IconButton(
                                onClick = { if (facingCount > 1) facingCount-- },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Moins", tint = BrandSkyBlue, modifier = Modifier.size(16.dp))
                            }
                            Text(
                                text = "$facingCount",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(
                                onClick = { facingCount++ },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Plus", tint = BrandSkyBlue, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Choix du Gabarit d'étiquette
                Text(
                    text = "Gabarit d'impression :",
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
                                    templateId = tmpl.id
                                    templateDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Nombre d'exemplaires d'étiquettes
                Text(
                    text = "Nombre d'étiquettes à imprimer :",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(BrandNavyDark)
                            .border(1.dp, BrandSlateBorder, RoundedCornerShape(12.dp)),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = { if (quantity > 1) quantity-- },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Diminuer quantité",
                                tint = BrandSkyBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Text(
                            text = "$quantity",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )

                        IconButton(
                            onClick = { quantity++ },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Augmenter quantité",
                                tint = BrandSkyBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Raccourcis rapides de quantité
                    listOf(1, 2, 5, 10).forEach { q ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (quantity == q) BrandSkyBlue else BrandNavyDark)
                                .border(1.dp, if (quantity == q) BrandSkyLight else BrandSlateBorder, RoundedCornerShape(10.dp))
                                .clickable { quantity = q },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$q",
                                color = if (quantity == q) Color.Black else TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Boutons d'Action (ADD vs SAVE)
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
                            val parsedPrice = priceInput.replace(",", ".").toDoubleOrNull()
                            onConfirm(
                                quantity,
                                templateId,
                                facingCount,
                                designationInput.ifBlank { null },
                                parsedPrice
                            )
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .height(48.dp)
                            .testTag(if (isEditMode) "save_article_button" else "add_article_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isEditMode) BrandSkyBlue else RetailEmerald
                        )
                    ) {
                        Icon(
                            imageVector = if (isEditMode) Icons.Default.Save else Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEditMode) "ENREGISTRER" else "AJOUTER (ADD)",
                            color = Color.Black,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
