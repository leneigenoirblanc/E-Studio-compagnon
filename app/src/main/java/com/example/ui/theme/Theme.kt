package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val EStudioColorScheme = darkColorScheme(
    primary = BrandSkyLight,
    onPrimary = BrandNavyDark,
    primaryContainer = BrandSkyMuted,
    onPrimaryContainer = TextPrimary,
    secondary = RetailEmerald,
    onSecondary = BrandNavyDark,
    secondaryContainer = RetailEmeraldDark,
    onSecondaryContainer = TextPrimary,
    tertiary = RetailPromoAmber,
    onTertiary = BrandNavyDark,
    background = BrandNavyDark,
    onBackground = TextPrimary,
    surface = BrandSlateDark,
    onSurface = TextPrimary,
    surfaceVariant = BrandSlateCard,
    onSurfaceVariant = TextSecondary,
    outline = BrandSlateBorder,
    error = RetailErrorRed,
    onError = TextPrimary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = EStudioColorScheme,
        typography = Typography,
        content = content
    )
}
