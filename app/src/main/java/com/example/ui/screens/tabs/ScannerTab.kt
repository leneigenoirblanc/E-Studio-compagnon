package com.example.ui.screens.tabs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ScanItem
import com.example.model.MobileScanItem
import com.example.ui.components.CameraScannerView
import com.example.ui.dialogs.ArticleQuickAddDialog
import com.example.ui.scan.AVAILABLE_TEMPLATES
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
    var manualScanTrigger by remember { mutableLongStateOf(0L) }
    var itemToEdit by remember { mutableStateOf<MobileScanItem?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BrandNavyDark)
    ) {
        // 1. En-tête de la table de scan active
        Surface(
            color = BrandSlateCard,
            border = BorderStroke(1.dp, BrandSlateBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToLot() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(BrandSkyBlue.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = null,
                            tint = BrandSkyBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = uiState.lotName.ifEmpty { "Table de scan active" },
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(RetailEmerald.copy(alpha = 0.2f))
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "#${uiState.lotId.takeLast(6).uppercase()}",
                                    fontSize = 10.sp,
                                    color = RetailEmerald,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = "${uiState.totalReferencesCount} référence(s) • ${uiState.totalLabelsCount} étiquette(s)",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onOpenSearchDialog,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Recherche manuelle",
                            tint = TextSecondary
                        )
                    }

                    IconButton(
                        onClick = onOpenScannerSettings,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Réglages du scanner",
                            tint = BrandSkyBlue
                        )
                    }
                }
            }
        }

        // 2. Zone Viseur Caméra (Cadre compact 230dp) + Bouton SCAN + Toggles
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .height(230.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Black)
        ) {
            if (hasCameraPermission) {
                CameraScannerView(
                    modifier = Modifier.fillMaxSize(),
                    isAutoScan = uiState.isAutoScan,
                    autoScanDelayMs = uiState.autoScanDelayMillis,
                    manualScanTrigger = manualScanTrigger,
                    isTorchOn = uiState.isTorchOn,
                    onBarcodeDetected = { code, _ ->
                        viewModel.onBarcodeScanned(code)
                    },
                    onToggleTorch = { viewModel.toggleTorch() }
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = null,
                            tint = BrandSkyLight,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Accès caméra requis",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = onRequestCameraPermission,
                            colors = ButtonDefaults.buttonColors(containerColor = BrandSkyBlue),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Autoriser la caméra", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Gros bouton SCAN manuel (au centre en bas du cadre caméra)
            if (!uiState.isAutoScan) {
                Button(
                    onClick = {
                        manualScanTrigger = System.currentTimeMillis()
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                        .height(44.dp)
                        .testTag("manual_scan_trigger_button"),
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandSkyBlue,
                        contentColor = Color.Black
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SCANNER LE CODE",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        // 3. Barre de contrôles rapides & Toggles
        Surface(
            color = BrandSlateCard,
            border = BorderStroke(1.dp, BrandSlateBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Toggle Auto-Scan
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        viewModel.setAutoScan(!uiState.isAutoScan)
                    }
                ) {
                    Switch(
                        checked = uiState.isAutoScan,
                        onCheckedChange = { viewModel.setAutoScan(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = BrandSkyBlue,
                            checkedTrackColor = BrandSkyBlue.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.testTag("auto_scan_toggle")
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "Auto Scan",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (uiState.isAutoScan) "${uiState.autoScanDelayMillis}ms" else "Manuel (Bouton)",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .height(28.dp)
                        .width(1.dp)
                        .background(BrandSlateBorder)
                )

                // Toggle Auto-Validate (Ajout direct vs Boîte de dialogue)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        viewModel.setAutoValidate(!uiState.isAutoValidate)
                    }
                ) {
                    Switch(
                        checked = uiState.isAutoValidate,
                        onCheckedChange = { viewModel.setAutoValidate(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = RetailEmerald,
                            checkedTrackColor = RetailEmerald.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.testTag("auto_validate_toggle")
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "Auto Validate",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (uiState.isAutoValidate) "Ajout direct" else "Éditer (Popup)",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 4. Liste des articles scannés dans la table (même fenêtre)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ARTICLES DE LA TABLE (${uiState.currentScanItems.size})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = BrandSkyLight,
                letterSpacing = 1.sp
            )

            if (uiState.canUndo) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { viewModel.undoLastScan() }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Undo,
                        contentDescription = "Annuler le dernier scan",
                        tint = RetailPromoAmber,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Annuler le dernier",
                        fontSize = 11.sp,
                        color = RetailPromoAmber,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Zone Scrollable des Articles
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(top = 4.dp, bottom = 8.dp)
        ) {
            if (uiState.items.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                tint = TextTertiary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Aucun article scanné dans cette table",
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Visez un code-barres et cliquez sur SCANNER ou activez l'Auto-Scan",
                                color = TextTertiary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            } else {
                items(uiState.items, key = { it.id }) { item ->
                    ScannedItemCard(
                        item = item,
                        onEditClick = {
                            itemToEdit = item
                        },
                        onDeleteClick = {
                            viewModel.removeItem(item.id)
                        }
                    )
                }
            }
        }

        // 5. Bouton d'exportation en bas de la fenêtre
        Surface(
            color = BrandSlateCard,
            border = BorderStroke(1.dp, BrandSlateBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onNavigateToLot,
                    modifier = Modifier
                        .weight(0.9f)
                        .height(46.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, BrandSlateBorder)
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Voir la table",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                Button(
                    onClick = {
                        viewModel.submitWorkSessionToPrintJob()
                    },
                    enabled = uiState.currentScanItems.isNotEmpty() && !uiState.isTransferring,
                    modifier = Modifier
                        .weight(1.3f)
                        .height(46.dp)
                        .testTag("export_table_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RetailEmerald,
                        disabledContainerColor = BrandSlateBorder
                    )
                ) {
                    if (uiState.isTransferring) {
                        CircularProgressIndicator(
                            color = Color.Black,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export en cours...", fontSize = 12.sp)
                    } else {
                        Icon(
                            imageVector = Icons.Default.FileUpload,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "EXPORTER LA TABLE",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }

    // Modal Article Scanné (Mode AJOUT après scan)
    if (uiState.pendingScanBarcode != null) {
        ArticleQuickAddDialog(
            barcode = uiState.pendingScanBarcode!!,
            resolvedProduct = uiState.pendingScanProduct,
            existingItem = null,
            defaultTemplateId = uiState.targetTemplateId,
            isEditMode = false,
            onDismiss = {
                viewModel.dismissPendingScan()
            },
            onConfirm = { quantity, templateId, facing, customDesignation, customPrice ->
                viewModel.confirmScanAdd(
                    barcode = uiState.pendingScanBarcode!!,
                    quantity = quantity,
                    templateId = templateId,
                    facing = facing,
                    customDesignation = customDesignation,
                    customPrice = customPrice
                )
            }
        )
    }

    // Modal Fiche Article (Mode ÉDITION d'un article existant au clic sur edit)
    if (itemToEdit != null) {
        ArticleQuickAddDialog(
            barcode = itemToEdit!!.code,
            existingItem = itemToEdit,
            defaultTemplateId = itemToEdit!!.templateId ?: uiState.targetTemplateId,
            isEditMode = true,
            onDismiss = {
                itemToEdit = null
            },
            onConfirm = { quantity, templateId, facing, customDesignation, customPrice ->
                viewModel.updateExistingScanItem(
                    itemId = itemToEdit!!.id,
                    quantity = quantity,
                    templateId = templateId,
                    facing = facing,
                    customDesignation = customDesignation,
                    customPrice = customPrice
                )
                itemToEdit = null
            }
        )
    }
}

@Composable
private fun ScannedItemCard(
    item: MobileScanItem,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = BrandSlateCard,
        border = BorderStroke(1.dp, BrandSlateBorder),
        modifier = Modifier.fillMaxWidth()
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
                    text = item.designation ?: "Article non référencé",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.code,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BrandSkyBlue
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (item.price != null && item.price > 0.0) String.format("%.2f €", item.price) else "-- €",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = RetailEmerald
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(BrandSkyBlue.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${item.quantity} étiquette${if (item.quantity > 1) "s" else ""}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandSkyLight
                        )
                    }

                    item.templateId?.let { tmpl ->
                        Spacer(modifier = Modifier.width(6.dp))
                        val label = AVAILABLE_TEMPLATES.find { it.id == tmpl }?.name ?: tmpl
                        Text(
                            text = "• $label",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(BrandNavyDark)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Modifier l'article",
                        tint = BrandSkyBlue,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(BrandNavyDark)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Supprimer l'article",
                        tint = TextTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
