package com.example.ui.screens.tabs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CallMerge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.LotProfile
import com.example.domain.model.PrintInstruction
import com.example.model.MobileScanItem
import com.example.model.MobileScanLot
import com.example.ui.scan.ScanUiState
import com.example.ui.scan.ScanViewModel
import com.example.ui.theme.BrandNavyDark
import com.example.ui.theme.BrandSkyBlue
import com.example.ui.theme.BrandSkyLight
import com.example.ui.theme.BrandSlateBorder
import com.example.ui.theme.BrandSlateCard
import com.example.ui.theme.BrandSlateDark
import com.example.ui.theme.RetailEmerald
import com.example.ui.theme.RetailErrorRed
import com.example.ui.theme.RetailPromoAmber
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Points 17, 18, 19, 20, 21, 22, 23, 24, 27, 28, 29, 41
 * Gestionnaire complet des Lots d'impression et de l'exécution terrain
 */
@Composable
fun LotsManagerTab(
    viewModel: ScanViewModel,
    uiState: ScanUiState,
    onOpenLotWizard: () -> Unit,
    onOpenTrashDialog: () -> Unit,
    onOpenValidationDialog: (MobileScanLot?) -> Unit,
    onNavigateToScanner: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedSubTab by remember { mutableIntStateOf(0) } // 0 = Lot en cours, 1 = Tous mes lots
    var showMergePickerForLot by remember { mutableStateOf<MobileScanLot?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BrandNavyDark)
    ) {
        // En-tête avec bascule "LOT EN COURS" / "TOUS MES LOTS"
        Surface(
            color = BrandSlateCard,
            border = BorderStroke(1.dp, BrandSlateBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                TabRow(
                    selectedTabIndex = selectedSubTab,
                    containerColor = BrandNavyDark,
                    contentColor = BrandSkyBlue,
                    modifier = Modifier.clip(RoundedCornerShape(10.dp))
                ) {
                    Tab(
                        selected = selectedSubTab == 0,
                        onClick = { selectedSubTab = 0 },
                        text = {
                            Text(
                                text = "LOT EN COURS (${uiState.items.size})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    )
                    Tab(
                        selected = selectedSubTab == 1,
                        onClick = { selectedSubTab = 1 },
                        text = {
                            Text(
                                text = "MES LOTS (${uiState.allLotsList.size})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    )
                }
            }
        }

        when (selectedSubTab) {
            0 -> {
                // SOUS-ONGLET 1 : LOT EN COURS
                ActiveLotSubView(
                    viewModel = viewModel,
                    uiState = uiState,
                    onOpenValidationDialog = {
                        val activeLot = uiState.activeLotData ?: MobileScanLot(
                            id = uiState.lotId,
                            name = uiState.lotName,
                            items = uiState.items,
                            operatorName = uiState.operatorName,
                            targetTemplateId = uiState.targetTemplateId
                        )
                        onOpenValidationDialog(activeLot)
                    }
                )
            }
            1 -> {
                // SOUS-ONGLET 2 : TOUS MES LOTS
                AllLotsSubView(
                    allLots = uiState.allLotsList,
                    trashCount = uiState.trashLotsList.size,
                    onOpenCreateWizard = onOpenLotWizard,
                    onOpenTrash = onOpenTrashDialog,
                    onTogglePin = { id, pinned -> viewModel.togglePinLot(id, pinned) },
                    onCloneLot = { lot -> viewModel.cloneLot(lot) },
                    onMergeLot = { lot -> showMergePickerForLot = lot },
                    onDeleteLot = { id -> viewModel.softDeleteLot(id) },
                    onValidateLot = { lot -> onOpenValidationDialog(lot) }
                )
            }
        }
    }

    // Modal dialogue pour choisir le 2e lot à fusionner (Point 29)
    showMergePickerForLot?.let { sourceLot ->
        MergeLotSelectionDialog(
            sourceLot = sourceLot,
            availableLots = uiState.allLotsList.filter { it.id != sourceLot.id },
            onDismiss = { showMergePickerForLot = null },
            onConfirmMerge = { targetLot, newName ->
                viewModel.mergeLots(sourceLot, targetLot, newName)
                showMergePickerForLot = null
            }
        )
    }
}

@Composable
private fun ActiveLotSubView(
    viewModel: ScanViewModel,
    uiState: ScanUiState,
    onOpenValidationDialog: () -> Unit
) {
    val totalArticles = uiState.items.size
    val totalLabels = uiState.items.sumOf { it.quantity }
    val unknownArticlesCount = uiState.items.count { it.isUnknown }
    val missingTemplateCount = uiState.items.count { it.templateId == null && uiState.targetTemplateId.isBlank() }
    val validCount = (totalArticles - (unknownArticlesCount + missingTemplateCount)).coerceAtLeast(0)
    val percentage = if (totalArticles > 0) (validCount.toFloat() / totalArticles.toFloat()) else 1f
    val isLocked = uiState.isLotLocked

    Column(modifier = Modifier.fillMaxSize()) {
        // En-tête du lot avec nom, verrouillage (Point 23) et progression visuelle (Point 21)
        Card(
            colors = CardDefaults.cardColors(containerColor = BrandSlateCard),
            shape = RoundedCornerShape(bottomStart = 14.dp, bottomEnd = 14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = uiState.lotName,
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (isLocked) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(RetailPromoAmber.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("🔒 LECTURE SEULE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = RetailPromoAmber)
                                }
                            }
                        }
                        Text(
                            text = "Opérateur : ${uiState.operatorName} • Gabarit : ${uiState.selectedTemplate.name}",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    // Bouton Verrouiller / Déverrouiller (Point 23)
                    IconButton(
                        onClick = { viewModel.lockCurrentLot(!isLocked) },
                        modifier = Modifier.size(32.dp).testTag("btn_lock_lot")
                    ) {
                        Icon(
                            imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = "Verrouillage lecture seule",
                            tint = if (isLocked) RetailPromoAmber else BrandSkyLight
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Progression visuelle métier (Point 21) : 87 articles, 143 étiquettes — ██████████░░ 94% — 84 complets, 3 à vérifier
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(BrandNavyDark)
                        .padding(10.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "$totalArticles articles, $totalLabels étiquettes",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "${(percentage * 100).toInt()}% conforme",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (percentage >= 0.9f) RetailEmerald else RetailPromoAmber
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        LinearProgressIndicator(
                            progress = { percentage },
                            modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)),
                            color = if (percentage >= 0.9f) RetailEmerald else RetailPromoAmber,
                            trackColor = BrandSlateBorder
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "$validCount complets, ${totalArticles - validCount} à vérifier",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Barre de recherche dans le lot (Point 17)
                OutlinedTextField(
                    value = uiState.lotSearchQuery,
                    onValueChange = { viewModel.setLotSearchQuery(it) },
                    placeholder = { Text("Rechercher dans ce lot (nom, EAN)...", fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = BrandSkyBlue, modifier = Modifier.size(16.dp)) },
                    trailingIcon = {
                        if (uiState.lotSearchQuery.isNotBlank()) {
                            IconButton(onClick = { viewModel.setLotSearchQuery("") }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandSkyBlue,
                        unfocusedBorderColor = BrandSlateBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Filtres rapides (Point 17) : [ Tous ] [ Validés ] [ Incomplets ] [ Inconnus ] [ Promo ]
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "ALL" to "Tous ($totalArticles)",
                        "VALIDATED" to "Validés ($validCount)",
                        "INCOMPLETE" to "À vérifier ($missingTemplateCount)",
                        "UNKNOWN" to "Inconnus ($unknownArticlesCount)",
                        "PROMO" to "Promo"
                    ).forEach { (key, label) ->
                        val isSelected = uiState.lotFilter == key
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) BrandSkyBlue else BrandNavyDark)
                                .border(1.dp, if (isSelected) BrandSkyBlue else BrandSlateBorder, RoundedCornerShape(6.dp))
                                .clickable { viewModel.setLotFilter(key) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else TextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Barre d'actions en masse si des articles sont sélectionnés (Point 19)
        AnimatedVisibility(visible = uiState.isMultiSelectActive && !isLocked) {
            Surface(
                color = Color(0xFF1E293B),
                border = BorderStroke(1.dp, BrandSkyBlue.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${uiState.selectedLotItemIds.size} sélectionné(s)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandSkyLight
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        MassActionButton("Gabarit") { viewModel.massApplyTemplate(uiState.targetTemplateId) }
                        MassActionButton("Qté 2") { viewModel.massApplyQuantity(2) }
                        MassActionButton("Promo") { viewModel.massTogglePromo() }
                        MassActionButton("Suppr", isDestructive = true) { viewModel.massDeleteItems() }
                        IconButton(onClick = { viewModel.selectAllItems(false) }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Désélectionner", tint = TextSecondary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // Liste des articles filtrés
        val itemsToShow = uiState.filteredItems

        if (itemsToShow.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (uiState.lotSearchQuery.isNotBlank() || uiState.lotFilter != "ALL") "Aucun article ne correspond aux filtres" else "Le lot est vide. Scannez un article pour commencer.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(itemsToShow, key = { it.id }) { item ->
                    val isSelected = uiState.selectedLotItemIds.contains(item.id)
                    LotItemRow(
                        item = item,
                        isSelected = isSelected,
                        isLocked = isLocked,
                        onToggleSelect = { viewModel.toggleItemSelection(item.id) },
                        onIncrement = { viewModel.incrementItemQuantity(item.id) },
                        onDecrement = { viewModel.decrementItemQuantity(item.id) },
                        onDelete = { viewModel.removeItem(item.id) }
                    )
                }
            }
        }

        // Bouton inférieur fixe : [ VALIDER & EXPORTER LE LOT ] (Point 20, 30, 31, 42)
        if (uiState.items.isNotEmpty()) {
            Surface(
                color = BrandSlateCard,
                border = BorderStroke(1.dp, BrandSlateBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onOpenValidationDialog,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RetailEmerald, contentColor = Color.White),
                        modifier = Modifier.fillMaxWidth().height(46.dp).testTag("btn_validate_lot_summary")
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CONTRÔLER & EXPORTER LE LOT (${uiState.totalLabelsCount} ÉTIQUETTES)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AllLotsSubView(
    allLots: List<MobileScanLot>,
    trashCount: Int,
    onOpenCreateWizard: () -> Unit,
    onOpenTrash: () -> Unit,
    onTogglePin: (String, Boolean) -> Unit,
    onCloneLot: (MobileScanLot) -> Unit,
    onMergeLot: (MobileScanLot) -> Unit,
    onDeleteLot: (String) -> Unit,
    onValidateLot: (MobileScanLot) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
        // Barre supérieure : Nouveau lot + Corbeille
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onOpenCreateWizard,
                colors = ButtonDefaults.buttonColors(containerColor = BrandSkyBlue),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(38.dp).testTag("btn_new_lot_manager")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("+ NOUVEAU LOT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onOpenTrash,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, if (trashCount > 0) RetailErrorRed.copy(alpha = 0.6f) else BrandSlateBorder),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = if (trashCount > 0) RetailErrorRed else TextSecondary),
                modifier = Modifier.height(38.dp).testTag("btn_open_trash")
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("CORBEILLE ($trashCount)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (allLots.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("Aucun lot enregistré dans la base locale.", fontSize = 12.sp, color = TextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(allLots, key = { it.id }) { lot ->
                    SavedLotCard(
                        lot = lot,
                        onTogglePin = { onTogglePin(lot.id, lot.isPinned) },
                        onClone = { onCloneLot(lot) },
                        onMerge = { onMergeLot(lot) },
                        onDelete = { onDeleteLot(lot.id) },
                        onValidate = { onValidateLot(lot) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SavedLotCard(
    lot: MobileScanLot,
    onTogglePin: () -> Unit,
    onClone: () -> Unit,
    onMerge: () -> Unit,
    onDelete: () -> Unit,
    onValidate: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = BrandSlateCard),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (lot.isPinned) BrandSkyBlue else BrandSlateBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Tag coloré du profil (Point 18)
                    val (tagColor, tagLabel) = when (lot.colorTag.uppercase()) {
                        "PROMO" -> RetailPromoAmber to "PROMO"
                        "URGENT" -> RetailErrorRed to "URGENT"
                        "INVENTAIRE" -> Color(0xFFA855F7) to "INVENTAIRE"
                        else -> BrandSkyBlue to "RAYON"
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(tagColor.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(tagLabel, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = tagColor)
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "V${lot.version}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                }

                Row {
                    // Bouton Épingler (Point 27)
                    IconButton(onClick = onTogglePin, modifier = Modifier.size(28.dp)) {
                        Icon(
                            Icons.Default.PushPin,
                            contentDescription = "Épingler",
                            tint = if (lot.isPinned) BrandSkyBlue else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    // Cloner (Point 28)
                    IconButton(onClick = onClone, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Cloner", tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                    // Fusionner (Point 29)
                    IconButton(onClick = onMerge, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.CallMerge, contentDescription = "Fusionner", tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                    // Supprimer (vers corbeille)
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = RetailErrorRed, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = lot.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Text(
                text = "${lot.department} • ${lot.items.size} articles (${lot.items.sumOf { it.quantity }} étiquettes) • Opérateur : ${lot.operatorName}",
                fontSize = 11.sp,
                color = TextSecondary
            )

            Text(
                text = "Catalogue : ${lot.catalogVersion} • Créé le ${lot.createdAt.take(10)}",
                fontSize = 10.sp,
                color = BrandSkyLight.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onValidate,
                colors = ButtonDefaults.buttonColors(containerColor = BrandNavyDark),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, BrandSlateBorder),
                modifier = Modifier.fillMaxWidth().height(36.dp)
            ) {
                Text("OUVRIR LE DIAGNOSTIC & EXPORTER CE LOT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BrandSkyLight)
            }
        }
    }
}

@Composable
private fun LotItemRow(
    item: MobileScanItem,
    isSelected: Boolean,
    isLocked: Boolean,
    onToggleSelect: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = BrandSlateCard),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, if (isSelected) BrandSkyBlue else BrandSlateBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isLocked) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggleSelect() },
                    colors = CheckboxDefaults.colors(checkedColor = BrandSkyBlue),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.designation ?: "Article non référencé (${item.code})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = item.code, fontSize = 10.sp, color = TextSecondary)
                    if (item.isUnknown) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("⚠ Inconnu", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = RetailPromoAmber)
                    }
                    if (item.promoPrice != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${item.promoPrice} € PROMO",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = RetailPromoAmber
                        )
                    }
                }
            }

            // Stepper de quantité
            if (!isLocked) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDecrement, modifier = Modifier.size(28.dp)) {
                        Text("-", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Text(text = "${item.quantity}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    IconButton(onClick = onIncrement, modifier = Modifier.size(28.dp)) {
                        Text("+", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = RetailErrorRed, modifier = Modifier.size(14.dp))
                    }
                }
            } else {
                Text(text = "Qté: ${item.quantity}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandSkyLight)
            }
        }
    }
}

@Composable
private fun MassActionButton(label: String, isDestructive: Boolean = false, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isDestructive) RetailErrorRed.copy(alpha = 0.2f) else BrandNavyDark)
            .border(1.dp, if (isDestructive) RetailErrorRed else BrandSlateBorder, RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDestructive) RetailErrorRed else TextPrimary
        )
    }
}

@Composable
private fun MergeLotSelectionDialog(
    sourceLot: MobileScanLot,
    availableLots: List<MobileScanLot>,
    onDismiss: () -> Unit,
    onConfirmMerge: (targetLot: MobileScanLot, newName: String) -> Unit
) {
    var selectedTargetId by remember { mutableStateOf(availableLots.firstOrNull()?.id ?: "") }
    var mergedName by remember { mutableStateOf("Fusion ${sourceLot.name}") }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = BrandSlateCard,
            border = BorderStroke(1.dp, BrandSlateBorder),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "FUSIONNER DEUX LOTS (Point 29)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandSkyBlue
                )
                Text(
                    text = "Lot source : ${sourceLot.name} (${sourceLot.items.size} art.)",
                    fontSize = 11.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("Choisir le deuxième lot :", fontSize = 11.sp, color = TextPrimary)
                availableLots.forEach { target ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedTargetId = target.id }
                            .padding(vertical = 4.dp)
                    ) {
                        androidx.compose.material3.RadioButton(
                            selected = selectedTargetId == target.id,
                            onClick = { selectedTargetId = target.id }
                        )
                        Text("${target.name} (${target.items.size} art.)", fontSize = 11.sp, color = TextPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = mergedName,
                    onValueChange = { mergedName = it },
                    label = { Text("Nom du nouveau lot fusionné") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        val target = availableLots.find { it.id == selectedTargetId }
                        if (target != null) {
                            onConfirmMerge(target, mergedName)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RetailEmerald)
                ) {
                    Text("CONFIRMER LA FUSION", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
