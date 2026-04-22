package com.dontforgetmed.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = TealPrimary,
    onPrimary = Color.White,
    primaryContainer = MintSoft,
    onPrimaryContainer = TealDeep,
    secondary = TealSoft,
    onSecondary = Color.White,
    secondaryContainer = MintLight,
    onSecondaryContainer = TealDeep,
    tertiary = Coral,
    onTertiary = Color.White,
    tertiaryContainer = CoralSoft,
    onTertiaryContainer = CoralDeep,
    background = SandLight,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = TextSecondary,
    outline = Divider,
    outlineVariant = Divider,
    error = CoralDeep,
    onError = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = TealSoft,
    onPrimary = DarkBg,
    primaryContainer = TealDeep,
    onPrimaryContainer = MintLight,
    secondary = MintLight,
    onSecondary = DarkBg,
    secondaryContainer = TealDeep,
    onSecondaryContainer = MintLight,
    tertiary = CoralSoft,
    onTertiary = DarkBg,
    tertiaryContainer = CoralDeep,
    onTertiaryContainer = CoralSoft,
    background = DarkBg,
    onBackground = DarkText,
    surface = DarkSurface,
    onSurface = DarkText,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = DarkTextSecondary,
    outline = Color(0xFF3A4E52),
    outlineVariant = Color(0xFF2B3D41),
    error = Coral,
    onError = DarkBg,
)

@Composable
fun DontForgetMedTheme(
    darkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
