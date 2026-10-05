package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DiamondCyan,
    onPrimary = Color(0xFF002028),
    primaryContainer = Color(0xFF004D5C),
    onPrimaryContainer = Color(0xFF99F4FF),
    secondary = ElectricBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF0D327B),
    onSecondaryContainer = Color(0xFFD6E2FF),
    tertiary = DiamondPurple,
    onTertiary = Color(0xFF280058),
    tertiaryContainer = Color(0xFF481F85),
    onTertiaryContainer = Color(0xFFEDDCFF),
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder,
    outlineVariant = DarkSurfaceHighlight,
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Premium Dark by default
    dynamicColor: Boolean = false, // Keep consistent cyber luxury palette
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
