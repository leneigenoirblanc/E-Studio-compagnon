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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.data.RetailCatalog
import com.example.model.MobileScanItem
import com.example.ui.components.CameraScannerView
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

@Composable
fun ScannerTab(
    viewModel: ScanViewModel,
    uiState: ScanUiState,
    hasCameraPermission: Boolean,
    onRequestCameraPermission: () -> Unit,
    onNavigateToLot: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Dernier article scanné
    val lastItem = uiState.items.firstOrNull()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BrandNavyDark)
    ) {
        // 1. Fond Caméra Viseur Plein Écran
        if (hasCameraPermission) {
            CameraScannerView(
                modifier = Modifier.fillMaxSize(),
                isTorchOn = uiState.isTorchOn,
                onBarcodeDetected = { code, _ ->
                    viewModel.onBarcodeScanned(code)
                },
                onToggleTorch = {
                    viewModel.toggleTorch()
                }
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BrandSlateDark)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = BrandSlateCard),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = null,
                            tint = BrandSkyLight,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Autorisation Caméra requise",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "L'accès à la caméra permet le décodage optique à 60 FPS des codes-barres rayon et articles.",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
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
        }

        // 2. HUD Flottant Supérieur : Contrôles d'acquisition
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Barre supérieure des touches rapides (+1, +2, +3, +5, +10)
            Card(
                colors = CardDefaults.cardColors(containerColor = BrandNavyDark.copy(alpha = 0.85f)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BrandSlateBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Qté auto :",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(1, 2, 3, 5, 10).forEach { qty ->
                            val isSelected = uiState.quickQuantity == qty
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) BrandSkyBlue else BrandSlateDark)
                                    .border(
                                        1.dp,
                                        if (isSelected) BrandSkyLight else BrandSlateBorder,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { viewModel.setQuickQuantity(qty) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("quick_qty_$qty"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+$qty",
                                    color = if (isSelected) TextPrimary else TextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // Capsule bascule Sticker Promo
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.isPromoMode) RetailPromoAmberDark.copy(alpha = 0.90f) else BrandNavyDark.copy(alpha = 0.85f)
                ),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (uiState.isPromoMode) RetailPromoAmber else BrandSlateBorder
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalOffer,
                            contentDescription = null,
                            tint = if (uiState.isPromoMode) RetailPromoAmber else TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (uiState.isPromoMode) "Sticker Promo Jaune Actif" else "Balisage Prix Standard",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (uiState.isPromoMode) {
                            OutlinedTextField(
                                value = uiState.promoInputPrice,
                                onValueChange = { viewModel.setPromoPriceInput(it) },
                                placeholder = { Text("Prix €", fontSize = 11.sp, color = TextSecondary) },
                                modifier = Modifier
                                    .width(85.dp)
                                    .height(40.dp)
                                    .testTag("promo_price_input"),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = RetailPromoAmber,
                                    unfocusedBorderColor = RetailPromoAmberDark,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        Switch(
                            checked = uiState.isPromoMode,
                            onCheckedChange = { viewModel.togglePromoMode(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = RetailPromoAmber,
                                checkedTrackColor = RetailPromoAmberDark
                            ),
                            modifier = Modifier.testTag("promo_mode_switch")
                        )
                    }
                }
            }
        }

        // 3. HUD Flottant Inférieur : Carte "Dernier Article Scanné" + Démo
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Bandeau de simulation articles rapides
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BrandNavyDark.copy(alpha = 0.85f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Scan test :",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                RetailCatalog.getAllDemoProducts().forEach { product ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(BrandSlateCard)
                            .border(1.dp, BrandSlateBorder, RoundedCornerShape(6.dp))
                            .clickable { viewModel.onBarcodeScanned(product.code) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = product.designation.split(" ").take(2).joinToString(" "),
                            color = BrandSkyLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Carte Hero contextuelle : Dernier scan
            AnimatedVisibility(
                visible = lastItem != null,
                enter = slideInVertically { it / 2 } + fadeIn(),
                exit = slideOutVertically { it / 2 } + fadeOut()
            ) {
                if (lastItem != null) {
                    LastScannedHeroCard(
                        item = lastItem,
                        totalLabelsInLot = uiState.totalLabelsCount,
                        onIncrement = { viewModel.incrementItemQuantity(lastItem.id, 1) },
                        onDecrement = { viewModel.incrementItemQuantity(lastItem.id, -1) },
                        onNavigateToLot = onNavigateToLot
                    )
                }
            }
        }
    }
}

@Composable
fun LastScannedHeroCard(
    item: MobileScanItem,
    totalLabelsInLot: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onNavigateToLot: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("last_scanned_card"),
        colors = CardDefaults.cardColors(containerColor = BrandSlateCard),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, BrandSkyBlue)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
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
                            .background(RetailEmerald)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ARTICLE AJOUTÉ AU LOT",
                        color = RetailEmerald,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                Text(
                    text = "Lot : $totalLabelsInLot étiquette(s)",
                    color = BrandSkyLight,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.designation ?: "Article EAN ${item.code}",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = item.code,
                            color = BrandSkyLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        if (item.promoPrice != null) {
                            Text(
                                text = String.format(java.util.Locale.US, "%.2f €", item.price ?: 0.0),
                                color = TextSecondary,
                                fontSize = 12.sp,
                                textDecoration = TextDecoration.LineThrough
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = String.format(java.util.Locale.US, "%.2f € PROMO", item.promoPrice),
                                color = RetailPromoAmber,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        } else if (item.price != null) {
                            Text(
                                text = String.format(java.util.Locale.US, "%.2f €", item.price),
                                color = RetailEmerald,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Boutons d'ajustement immédiat de quantité
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(BrandSlateDark)
                            .border(1.dp, BrandSlateBorder, CircleShape)
                            .clickable { onDecrement() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Diminuer",
                            tint = TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(BrandNavyDark)
                            .padding(horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "x${item.quantity}",
                            color = TextPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(BrandSkyBlue)
                            .clickable { onIncrement() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Augmenter",
                            tint = TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bouton vers le lot complet
            Button(
                onClick = onNavigateToLot,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandSlateDark),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Voir et gérer le lot d'impression",
                    color = BrandSkyLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = BrandSkyLight,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
