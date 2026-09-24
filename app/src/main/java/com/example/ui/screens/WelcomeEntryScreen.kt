package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.scan.ScanUiState
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

/**
 * S01 — Écran d'accueil principal (Entry / Welcome)
 * Propose les 2 choix initiaux requis par la spécification :
 * 1. "Se connecter au Desktop" (Connexion logicielle QR / mDNS)
 * 2. "Créer un nouveau lot / table" (Démarrage immédiat en local)
 * Avec accès rapide aux tables existantes et statut de connexion desktop persistant.
 */
@Composable
fun WelcomeEntryScreen(
    uiState: ScanUiState,
    isDesktopConnected: Boolean,
    connectedInstanceName: String?,
    onConnectDesktopClick: () -> Unit,
    onCreateNewBatchClick: () -> Unit,
    onOpenTableManagerClick: () -> Unit,
    onOpenScannerSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BrandNavyDark)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // En-tête / Logo Brand
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(BrandSkyBlue, Color(0xFF0284C7))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Print,
                        contentDescription = "Logo",
                        tint = TextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "E-STUDIO MOBILE",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Scan Retail & Gestion des Étiquettes",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            IconButton(
                onClick = onOpenScannerSettingsClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(BrandSlateCard)
                    .border(1.dp, BrandSlateBorder, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Réglages",
                    tint = BrandSkyBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Statut de connexion Desktop persistant
        Surface(
            color = BrandSlateCard,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, if (isDesktopConnected) RetailEmerald.copy(alpha = 0.4f) else BrandSlateBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isDesktopConnected) RetailEmerald else RetailPromoAmber)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isDesktopConnected) "Connecté au Desktop" else "Mode Local / Non connecté",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (isDesktopConnected) (connectedInstanceName ?: "E-STUDIO Magasin") else "Données persistées sur le téléphone",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                OutlinedButton(
                    onClick = onConnectDesktopClick,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, BrandSkyBlue.copy(alpha = 0.6f)),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isDesktopConnected) "Changer" else "Connecter",
                        fontSize = 11.sp,
                        color = BrandSkyBlue,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Titre de bienvenue & invitation à l'action
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Que souhaitez-vous faire ?",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Sélectionnez votre mode de travail pour commencer la session.",
                fontSize = 13.sp,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // CARTE ACTION 1 : SE CONNECTER AU DESKTOP
        MainActionCard(
            title = "Se connecter au Desktop",
            badge = "Appairage PC",
            badgeColor = BrandSkyBlue,
            description = "Appairez le terminal avec le logiciel E-Studio sur PC par QR Code ou détection réseau pour synchroniser vos tables en direct.",
            icon = Icons.Default.Computer,
            iconTint = BrandSkyBlue,
            buttonText = "CONNECTER AU PC",
            buttonContainerColor = BrandSlateDark,
            buttonContentColor = BrandSkyLight,
            onClick = onConnectDesktopClick,
            testTag = "action_connect_desktop"
        )

        Spacer(modifier = Modifier.height(14.dp))

        // CARTE ACTION 2 : CRÉER UN NOUVEAU LOT / TABLE (Priorité Scan)
        MainActionCard(
            title = "Créer un nouveau lot / table",
            badge = "Scan Immédiat",
            badgeColor = RetailEmerald,
            description = "Ouvrez une nouvelle table de scan locale. Nommez votre table, choisissez son balisage visuel et commencez à scanner sans attendre.",
            icon = Icons.Default.AddCircle,
            iconTint = RetailEmerald,
            buttonText = "+ CRÉER ET SCANNER",
            buttonContainerColor = RetailEmerald,
            buttonContentColor = Color.Black,
            onClick = onCreateNewBatchClick,
            testTag = "action_create_batch"
        )

        Spacer(modifier = Modifier.height(14.dp))

        // CARTE ACTION 3 : GESTIONNAIRE DE TABLES (Tables existantes)
        Card(
            colors = CardDefaults.cardColors(containerColor = BrandSlateCard),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, BrandSlateBorder),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenTableManagerClick() }
                .testTag("action_open_table_manager")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(BrandSkyBlue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = null,
                            tint = BrandSkyLight,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Gestionnaire de Tables & Lots",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Consulter, modifier, verrouiller ou exporter vos tables",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun MainActionCard(
    title: String,
    badge: String,
    badgeColor: Color,
    description: String,
    icon: ImageVector,
    iconTint: Color,
    buttonText: String,
    buttonContainerColor: Color,
    buttonContentColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = BrandSlateCard,
        border = BorderStroke(1.dp, BrandSlateBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(iconTint.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = badge,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = badgeColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = description,
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag(testTag),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonContainerColor,
                    contentColor = buttonContentColor
                )
            ) {
                Text(
                    text = buttonText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
