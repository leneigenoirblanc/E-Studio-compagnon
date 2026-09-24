package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.domain.repository.ProductRepository
import com.example.domain.repository.ResolvedProduct
import com.example.ui.theme.BrandNavyDark
import com.example.ui.theme.BrandSkyBlue
import com.example.ui.theme.BrandSlateBorder
import com.example.ui.theme.BrandSlateCard
import com.example.ui.theme.RetailEmerald
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Points 14 & 15 : Recherche manuelle de produit & Saisie de code-barres de secours
 */
@Composable
fun ManualProductSearchDialog(
    productRepository: ProductRepository,
    onDismiss: () -> Unit,
    onSelectProduct: (barcode: String, resolved: ResolvedProduct?) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var manualBarcodeEntry by remember { mutableStateOf("") }
    val searchResults = remember { mutableStateListOf<ResolvedProduct>() }

    LaunchedEffect(searchQuery) {
        if (searchQuery.length >= 2) {
            val results = productRepository.searchCatalog(searchQuery)
            searchResults.clear()
            searchResults.addAll(results)
        } else {
            searchResults.clear()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = BrandSlateCard,
            border = BorderStroke(1.dp, BrandSlateBorder),
            modifier = Modifier.fillMaxWidth().height(480.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // En-tête
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RECHERCHE & SAISIE MANUELLE",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BrandSkyBlue
                    )

                    IconButton(onClick = onDismiss, modifier = Modifier.size(26.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Onglets : Recherche Catalogue (Point 14) vs Saisie Code (Point 15)
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = BrandNavyDark,
                    contentColor = BrandSkyBlue,
                    modifier = Modifier.clip(RoundedCornerShape(8.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Recherche Nom / Réf", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Code-barres Clavier", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                when (selectedTab) {
                    0 -> {
                        // Point 14 : Recherche par nom, EAN, référence
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Ex: café arabica, 356007...", fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = BrandSkyBlue) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("input_manual_product_search"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrandSkyBlue,
                                unfocusedBorderColor = BrandSlateBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        if (searchResults.isEmpty() && searchQuery.length >= 2) {
                            Box(
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Aucun article trouvé pour \"$searchQuery\"", fontSize = 12.sp, color = TextSecondary)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(searchResults) { item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(BrandNavyDark)
                                            .clickable {
                                                onSelectProduct(item.snapshot.barcode, item)
                                                onDismiss()
                                            }
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.snapshot.designation,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = "${item.snapshot.barcode} • ${item.pricing.regularPrice.formatted()}",
                                                fontSize = 11.sp,
                                                color = BrandSkyBlue
                                            )
                                        }

                                        Button(
                                            onClick = {
                                                onSelectProduct(item.snapshot.barcode, item)
                                                onDismiss()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = RetailEmerald),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.height(30.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text("AJOUTER", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        // Point 15 : Saisie directe de code-barres de secours
                        Column(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Dialpad, contentDescription = null, tint = BrandSkyBlue, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Code-barres illisible ou endommagé",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Saisissez les chiffres sous le code-barres (EAN-13, EAN-8)",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = manualBarcodeEntry,
                                onValueChange = { if (it.all { char -> char.isDigit() }) manualBarcodeEntry = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {
                                    if (manualBarcodeEntry.isNotBlank()) {
                                        onSelectProduct(manualBarcodeEntry, null)
                                        onDismiss()
                                    }
                                }),
                                label = { Text("Code numérique EAN / UPC") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("input_manual_barcode"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BrandSkyBlue,
                                    unfocusedBorderColor = BrandSlateBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    if (manualBarcodeEntry.isNotBlank()) {
                                        onSelectProduct(manualBarcodeEntry, null)
                                        onDismiss()
                                    }
                                },
                                enabled = manualBarcodeEntry.length >= 6,
                                modifier = Modifier.fillMaxWidth().height(44.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandSkyBlue)
                            ) {
                                Text("RECHERCHER ET AJOUTER AU LOT", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
