package com.example.ui.screens.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.PrintPriority
import com.example.domain.model.ScanItem
import com.example.ui.scan.AVAILABLE_PRINTERS
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
import com.example.ui.theme.RetailErrorRed
import com.example.ui.theme.RetailPromoAmber
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun WorkSessionTab(
    viewModel: ScanViewModel,
    uiState: ScanUiState,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPriority by remember { mutableStateOf(PrintPriority.NORMAL) }
    var showPrinterDropdown by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BrandNavyDark)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        // 1. En-tête WorkSession avec Statut et Réglages
        Card(
            colors = CardDefaults.cardColors(containerColor = BrandSlateCard),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BrandSlateBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(RetailEmerald.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "SESSION ACTIVE",
                                    color = RetailEmerald,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Opérateur : ${uiState.operatorName}",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = uiState.lotName,
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.testTag("btn_session_settings")
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Réglages Session", tint = BrandSkyLight)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Sélecteur d'imprimante cible
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(BrandSlateDark)
                        .clickable { showPrinterDropdown = true }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Print, contentDescription = null, tint = BrandSkyLight, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Imprimante cible :", color = TextSecondary, fontSize = 10.sp)
                            Text(uiState.selectedPrinterId, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Text("Modifier ▼", color = BrandSkyLight, fontSize = 11.sp)

                    DropdownMenu(
                        expanded = showPrinterDropdown,
                        onDismissRequest = { showPrinterDropdown = false }
                    ) {
                        AVAILABLE_PRINTERS.forEach { printer ->
                            DropdownMenuItem(
                                text = { Text(printer) },
                                onClick = {
                                    viewModel.setSelectedPrinter(printer)
                                    showPrinterDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Métriques totales (Références vs Étiquettes)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(BrandSlateDark)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${uiState.totalReferencesCount}",
                                color = BrandSkyLight,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(text = "Références Scannées", color = TextSecondary, fontSize = 11.sp)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(BrandSlateDark)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${uiState.totalLabelsCount}",
                                color = RetailEmerald,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(text = "Étiquettes Totales", color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Liste des Articles Scannés
        if (uiState.currentScanItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Print,
                        contentDescription = null,
                        tint = BrandSlateBorder,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Aucun article dans cette session",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Scannez des produits dans l'onglet Scanner ou via la gâchette Zebra",
                        color = TextTertiary,
                        fontSize = 11.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.currentScanItems, key = { it.id }) { item ->
                    WorkSessionItemCard(
                        item = item,
                        onIncrement = { viewModel.incrementItemQuantity(item.id) },
                        onDecrement = { viewModel.decrementItemQuantity(item.id) },
                        onRemove = { viewModel.removeItem(item.id) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 3. Barre d'action basse : Priorité + Bouton d'impression Idempotent
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
                    // Sélecteur de Priorité (Basse, Normale, Urgente)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Priorité :", color = TextSecondary, fontSize = 11.sp)
                        listOf(PrintPriority.NORMAL, PrintPriority.HIGH, PrintPriority.URGENT).forEach { p ->
                            val isSelected = selectedPriority == p
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) BrandSkyBlue else BrandSlateDark)
                                    .clickable { selectedPriority = p }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = p.name,
                                    color = if (isSelected) TextPrimary else TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Bouton Vider la session
                    IconButton(
                        onClick = { viewModel.clearCurrentSession() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Vider la session", tint = RetailErrorRed, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { viewModel.submitWorkSessionToPrintJob(selectedPriority) },
                    enabled = uiState.totalReferencesCount > 0 && !uiState.isTransferring,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RetailEmerald,
                        disabledContainerColor = BrandSlateDark
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_submit_session_print_job")
                ) {
                    if (uiState.isTransferring) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Envoi au Spooler E-Studio (Idempotent)...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Lancer l'Impression (${uiState.totalLabelsCount} étiquettes)",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WorkSessionItemCard(
    item: ScanItem,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = BrandSlateCard),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BrandSlateBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
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
                    val reg = item.pricing?.regularPrice?.formatted() ?: "1.99 €"
                    val promo = item.pricing?.promoPrice?.formatted()

                    if (promo != null) {
                        Text(text = reg, color = TextSecondary, fontSize = 11.sp, textDecoration = TextDecoration.LineThrough)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = promo, color = RetailPromoAmber, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Text(text = reg, color = BrandSkyLight, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "EAN: ${item.barcode}", color = TextTertiary, fontSize = 10.sp)
                }
            }

            // Stepper tactile pour les gants de manutention (48dp+)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(BrandSlateDark)
                        .clickable { onDecrement() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Moins", tint = TextPrimary, modifier = Modifier.size(16.dp))
                }

                Text(
                    text = "${item.quantity}",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(BrandSkyBlue)
                        .clickable { onIncrement() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Plus", tint = TextPrimary, modifier = Modifier.size(16.dp))
                }

                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = RetailErrorRed, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
