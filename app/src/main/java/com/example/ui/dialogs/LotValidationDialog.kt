package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.MobileScanLot
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
 * Points 20, 30, 31, 42 : Diagnostic de validation avant export & Transmission à E-Studio
 */
@Composable
fun LotValidationDialog(
    lot: MobileScanLot,
    isTransferring: Boolean,
    onDismiss: () -> Unit,
    onExportLocalJson: () -> Unit,
    onSendToEStudioServer: () -> Unit
) {
    val totalArticles = lot.items.size
    val totalLabels = lot.items.sumOf { it.quantity }
    val unknownArticlesCount = lot.items.count { it.isUnknown }
    val missingTemplateCount = lot.items.count { it.templateId == null && lot.targetTemplateId == null }
    val promoCount = lot.items.count { it.promoPrice != null }
    val validCount = totalArticles - (unknownArticlesCount + missingTemplateCount).coerceAtMost(totalArticles)
    val percentage = if (totalArticles > 0) (validCount.toFloat() / totalArticles.toFloat()) else 1f
    val hasBlockingErrors = missingTemplateCount > 0 && lot.requiresTemplate

    Dialog(onDismissRequest = { if (!isTransferring) onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = BrandSlateCard,
            border = BorderStroke(1.dp, BrandSlateBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // En-tête
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CONTRÔLE & EXPORT DU LOT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandSkyBlue
                        )
                        Text(
                            text = lot.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                    }

                    if (!isTransferring) {
                        IconButton(onClick = onDismiss, modifier = Modifier.size(26.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Fermer", tint = TextSecondary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Progression métier (Point 21) : 87 articles, 143 étiquettes — ██████████░░ 94% — 84 complets, 3 à vérifier
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(BrandNavyDark)
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "$totalArticles articles, $totalLabels étiquettes",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "${(percentage * 100).toInt()}% conforme",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (percentage >= 0.9f) RetailEmerald else RetailPromoAmber
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LinearProgressIndicator(
                            progress = { percentage },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = if (percentage >= 0.9f) RetailEmerald else RetailPromoAmber,
                            trackColor = BrandSlateBorder
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "$validCount complets, ${totalArticles - validCount} à vérifier",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Résumé des métriques avant envoi (Point 42)
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF1E293B))
                        .padding(12.dp)
                ) {
                    MetricLine("Département :", lot.department)
                    MetricLine("Articles en promotion :", "$promoCount articles")
                    MetricLine("Articles non référencés :", if (unknownArticlesCount > 0) "⚠ $unknownArticlesCount articles" else "0")
                    MetricLine("Gabarit par défaut :", lot.targetTemplateId ?: "Auto-assigné")
                    MetricLine("Version catalogue figée :", lot.catalogVersion)
                    MetricLine("Opérateur :", lot.operatorName)
                }

                if (hasBlockingErrors) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(RetailErrorRed.copy(alpha = 0.15f))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = RetailErrorRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Bloquant : $missingTemplateCount article(s) n'ont aucun gabarit défini.",
                            fontSize = 11.sp,
                            color = RetailErrorRed
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Deux boutons distincts (Points 30 & 31) : [ EXPORTER ] et [ ENVOYER À E-STUDIO ]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onExportLocalJson,
                        enabled = !isTransferring,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, BrandSkyBlue.copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandSkyBlue),
                        modifier = Modifier.weight(1f).height(46.dp).testTag("btn_export_local_json")
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("EXPORTER", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onSendToEStudioServer,
                        enabled = !hasBlockingErrors && !isTransferring,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RetailEmerald,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.weight(1.3f).height(46.dp).testTag("btn_send_to_estudio_api")
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isTransferring) "ENVOI EN COURS..." else "ENVOYER À E-STUDIO",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 11.sp, color = TextSecondary)
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
    }
}
