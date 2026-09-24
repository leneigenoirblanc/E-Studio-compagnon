package com.example.ui.screens.tabs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ScanItem
import com.example.domain.model.ScanMode
import com.example.ui.components.CameraScannerView
import com.example.ui.dialogs.ArticleQuickAddDialog
import com.example.ui.scan.ScanUiState
import com.example.ui.scan.ScanViewModel
import com.example.ui.theme.BrandNavyDark
import com.example.ui.theme.BrandSkyBlue
import com.example.ui.theme.BrandSkyLight
import com.example.ui.theme.BrandSlateBorder
import com.example.ui.theme.BrandSlateCard
import com.example.ui.theme.BrandSlateDark
import com.example.ui.theme.RetailEmerald
import com.example.ui.theme.RetailPromoAmber
import com.example.ui.theme.RetailPromoAmberDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun ScannerTab(
    viewModel: ScanViewModel,
    uiState: ScanUiState,
    hasCameraPermission: Boolean,
    onRequestCameraPermission: () -> Unit,
    onNavigateToLot: () -> Unit,
    onOpenSearchDialog: () -> Unit,
    onOpenScannerSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lastItem: ScanItem? = uiState.currentScanItems.firstOrNull()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BrandNavyDark)
    ) {
        // 1. Fond Caméra Viseur
        if (hasCameraPermission) {
            CameraScannerView(
                modifier = Modifier.fillMaxSize(),
                isTorchOn = uiState.isTorchOn,
                onBarcodeDetected = { code, _ ->
                    viewModel.scannerManager.cameraScanner.onCodeScannedFromCamera(code)
                },
                onToggleTorch = { viewModel.toggleTorch() }
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BrandSlateDark),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = null,
                        tint = BrandSkyLight,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Autorisation Caméra Requise",
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Pour scanner les codes-barres avec le capteur photo, autorisez l'accès caméra ou utilisez un terminal Zebra/Honeywell.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        onClick = onRequestCameraPermission,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandSkyBlue),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Activer la Caméra", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 2. HUD Supérieur : Statut Détection, Mode Scan (Point 9) & Raccourcis
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pillule de Mode de Scan (Point 9 : MANUEL, AUTO_SCAN, FULL_AUTOMATIC)
            val (modeLabel, modeColor) = when (uiState.scanMode) {
                ScanMode.MANUAL -> "MANUEL" to BrandSkyLight
                ScanMode.AUTO_SCAN_WITH_VALIDATION -> "AUTO-VALIDATION" to RetailPromoAmber
                ScanMode.FULL_AUTOMATIC -> "100% AUTO" to RetailEmerald
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xCC090D16))
                    .border(1.dp, modeColor.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                    .clickable { onOpenScannerSettings() }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
                    .testTag("pill_scan_mode")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(modeColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "MODE : $modeLabel",
                        color = modeColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // Boutons d'action rapides (Recherche, Paramètres Scanner, Undo, Torche)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Bouton Recherche Catalogue / Saisie Clavier (Points 14, 15)
                IconButton(
                    onClick = onOpenSearchDialog,
                    modifier = Modifier.size(36.dp).testTag("btn_search_product"),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color(0xCC090D16),
                        contentColor = BrandSkyBlue
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Recherche manuelle",
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Bouton Paramètres Scanner (Points 35, 36)
                IconButton(
                    onClick = onOpenScannerSettings,
                    modifier = Modifier.size(36.dp).testTag("btn_scanner_settings"),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color(0xCC090D16),
                        contentColor = TextSecondary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Paramètres scanner",
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Bouton Annuler (Undo) du dernier scan
                if (uiState.canUndo) {
                    IconButton(
                        onClick = { viewModel.undoLastScan() },
                        modifier = Modifier.size(36.dp).testTag("btn_undo_scan"),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color(0xCC090D16),
                            contentColor = RetailPromoAmber
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Undo,
                            contentDescription = "Annuler le dernier scan",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Bouton Torche
                IconButton(
                    onClick = { viewModel.toggleTorch() },
                    modifier = Modifier.size(36.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (uiState.isTorchOn) RetailPromoAmber else Color(0xCC090D16),
                        contentColor = if (uiState.isTorchOn) Color.Black else TextPrimary
                    )
                ) {
                    Icon(
                        imageVector = if (uiState.isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Torche",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // 3. Panneau Inférieur Flottant Ergonomique (Commandes de pouce)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Carte Contextuelle "Dernier Article Scanné" (Snapshot Produit Résolu)
            AnimatedVisibility(
                visible = lastItem != null,
                enter = slideInVertically { it / 2 } + fadeIn(),
                exit = slideOutVertically { it / 2 } + fadeOut()
            ) {
                lastItem?.let { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandSlateCard),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BrandSlateBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.productSnapshot?.designation ?: "Code : ${item.barcode}",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val regular = item.pricing?.regularPrice?.formatted() ?: "1.99 €"
                                    val promo = item.pricing?.promoPrice?.formatted()

                                    if (promo != null) {
                                        Text(
                                            text = regular,
                                            color = TextSecondary,
                                            fontSize = 11.sp,
                                            textDecoration = TextDecoration.LineThrough
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = promo,
                                            color = RetailPromoAmber,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    } else {
                                        Text(
                                            text = regular,
                                            color = BrandSkyLight,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "• ${item.barcode}",
                                        color = TextTertiary,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            // Stepper de quantité tactile (+ / -)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(BrandSlateDark)
                                        .clickable { viewModel.decrementItemQuantity(item.id) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Moins", tint = TextPrimary, modifier = Modifier.size(16.dp))
                                }

                                Text(
                                    text = "${item.quantity}",
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp)
                                )

                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(BrandSkyBlue)
                                        .clickable { viewModel.incrementItemQuantity(item.id) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Plus", tint = TextPrimary, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Bandeau de Contrôle Rapide : Sélecteur de Quantité [1] [2] [3] [5] [10] + Mode Promo
            Card(
                colors = CardDefaults.cardColors(containerColor = BrandSlateCard),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BrandSlateBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Sélecteur de Quantité Rapide [1, 2, 3, 5, 10]
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Quantité :",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            listOf(1, 2, 3, 5, 10).forEach { qty ->
                                val isSelected = uiState.quickQuantity == qty
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) BrandSkyBlue else BrandSlateDark)
                                        .clickable { viewModel.setQuickQuantity(qty) }
                                        .padding(horizontal = 9.dp, vertical = 6.dp)
                                        .testTag("qty_pill_$qty"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$qty",
                                        color = if (isSelected) TextPrimary else TextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Toggle Mode Sticker Promo
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (uiState.isPromoMode) RetailPromoAmberDark.copy(alpha = 0.3f) else Color.Transparent)
                                .clickable { viewModel.togglePromoMode() }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalOffer,
                                contentDescription = null,
                                tint = if (uiState.isPromoMode) RetailPromoAmber else TextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Promo",
                                color = if (uiState.isPromoMode) RetailPromoAmber else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Switch(
                                checked = uiState.isPromoMode,
                                onCheckedChange = { viewModel.togglePromoMode() },
                                modifier = Modifier.size(24.dp),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = RetailPromoAmber,
                                    checkedTrackColor = RetailPromoAmberDark,
                                    uncheckedThumbColor = TextSecondary,
                                    uncheckedTrackColor = BrandSlateDark
                                )
                            )
                        }
                    }

                    // Champ de saisie prix promo personnalisé si activé
                    AnimatedVisibility(visible = uiState.isPromoMode) {
                        Column(modifier = Modifier.padding(top = 8.dp)) {
                            OutlinedTextField(
                                value = uiState.promoInputPrice,
                                onValueChange = { viewModel.updatePromoPrice(it) },
                                label = { Text("Prix Promotionnel (€)", fontSize = 11.sp) },
                                placeholder = { Text("Ex: 1.49") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("input_promo_price"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = RetailPromoAmber,
                                    unfocusedBorderColor = BrandSlateBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 4. Barre d'action finale : Compteurs & Bouton "Transférer à E-Studio"
            Card(
                colors = CardDefaults.cardColors(containerColor = BrandSlateCard),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BrandSlateBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.clickable { onNavigateToLot() }
                    ) {
                        Text(
                            text = "${uiState.totalReferencesCount} réf. • ${uiState.totalLabelsCount} étiquettes",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Gabarit : ${uiState.selectedTemplate.name.take(22)}...",
                            color = BrandSkyLight,
                            fontSize = 10.sp
                        )
                    }

                    Button(
                        onClick = { viewModel.submitWorkSessionToPrintJob() },
                        enabled = uiState.totalReferencesCount > 0 && !uiState.isTransferring,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RetailEmerald,
                            disabledContainerColor = BrandSlateDark
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .height(44.dp)
                            .testTag("btn_submit_print_job")
                    ) {
                        if (uiState.isTransferring) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Envoi...", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Imprimer", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Modal dialogue "Fiche Article Rapide" après scan (Points 10, 11, 12, 13, 16)
    if (uiState.pendingScanBarcode != null) {
        ArticleQuickAddDialog(
            barcode = uiState.pendingScanBarcode!!,
            resolvedProduct = uiState.pendingScanProduct,
            defaultTemplateId = uiState.targetTemplateId,
            onDismiss = { viewModel.dismissPendingScan() },
            onAddImmediate = { qty, templateId, instructions, facing ->
                viewModel.onPendingScanConfirmed(qty, templateId, instructions, facing)
            },
            onAddAndEdit = { qty, templateId, instructions, facing ->
                viewModel.onPendingScanConfirmed(qty, templateId, instructions, facing)
                onNavigateToLot()
            }
        )
    }
}

