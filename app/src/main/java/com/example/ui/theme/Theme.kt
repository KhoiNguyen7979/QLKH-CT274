package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/**
 * Color Scheme cho Light theme.
 * Primary: WarehouseBlue, Error: StockDanger
 */
private val LightColorScheme = lightColorScheme(
    primary = WarehouseBlue,
    onPrimary = Color.White,
    primaryContainer = WarehouseBlueLight,
    onPrimaryContainer = WarehouseBlueDark,
    secondary = Color(0xFF535F70),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD7E3F7),
    onSecondaryContainer = Color(0xFF101C2B),
    tertiary = CategoryTech,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD4E8FF),
    onTertiaryContainer = Color(0xFF001E31),
    background = LightBackground,
    onBackground = Color(0xFF1C1B1F),
    surface = LightSurface,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE0E2EC),
    onSurfaceVariant = Color(0xFF44474F),
    outline = Color(0xFF74777F),
    outlineVariant = Color(0xFFC4C6D0),
    error = StockDanger,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    inverseSurface = Color(0xFF303033),
    inverseOnSurface = Color(0xFFF3F0F4),
    inversePrimary = Color(0xFFA8C7FA),
    surfaceDim = Color(0xFFD9D9E0),
    surfaceBright = Color(0xFFF8F9FF),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF2F3FA),
    surfaceContainer = Color(0xFFECEDF4),
    surfaceContainerHigh = Color(0xFFE6E7EE),
    surfaceContainerHighest = Color(0xFFE1E1E8)
)

/**
 * Color Scheme cho Dark theme.
 * Primary: blue nhạt hơn, surface tối.
 */
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF2962A8),
    onPrimary = Color.White,
    primaryContainer = WarehouseBlueDark,
    onPrimaryContainer = Color(0xFFD4E3FF),
    secondary = Color(0xFFBBC7DB),
    onSecondary = Color(0xFF253140),
    secondaryContainer = Color(0xFF3B4858),
    onSecondaryContainer = Color(0xFFD7E3F7),
    tertiary = Color(0xFF86D1F2),
    onTertiary = Color(0xFF003448),
    tertiaryContainer = Color(0xFF004C66),
    onTertiaryContainer = Color(0xFFC6E7FF),
    background = DarkBackground,
    onBackground = Color(0xFFE3E2E6),
    surface = DarkSurface,
    onSurface = Color(0xFFE3E2E6),
    surfaceVariant = Color(0xFF44474F),
    onSurfaceVariant = Color(0xFFC4C6D0),
    outline = Color(0xFF8E9099),
    outlineVariant = Color(0xFF44474F),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    inverseSurface = Color(0xFFE3E2E6),
    inverseOnSurface = Color(0xFF1C1B1F),
    inversePrimary = WarehouseBlue,
    surfaceDim = Color(0xFF0F1118),
    surfaceBright = Color(0xFF3A3A40),
    surfaceContainerLowest = Color(0xFF0A0C13),
    surfaceContainerLow = Color(0xFF1A1D27),
    surfaceContainer = Color(0xFF1E2029),
    surfaceContainerHigh = Color(0xFF282A33),
    surfaceContainerHighest = Color(0xFF33353E)
)

/**
 * Theme function chính của ứng dụng.
 * - Hỗ trợ Dynamic Color (Android 12+)
 * - Tự động chuyển Light/Dark theo system setting
 * - Áp dụng Typography, Shapes, ColorScheme
 */
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = WarehouseShapes,
        content = content
    )
}
