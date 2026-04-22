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
    primary = LavenderPrimary,
    onPrimary = Color.White,
    primaryContainer = LilacSoft,
    onPrimaryContainer = LavenderDeep,
    secondary = LavenderSoft,
    onSecondary = Color.White,
    secondaryContainer = LilacLight,
    onSecondaryContainer = LavenderDeep,
    tertiary = Rose,
    onTertiary = Color.White,
    tertiaryContainer = RoseSoft,
    onTertiaryContainer = RoseDeep,
    background = BackgroundTint,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = TextSecondary,
    outline = Divider,
    outlineVariant = Divider,
    error = RoseDeep,
    onError = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = LavenderSoft,
    onPrimary = DarkBg,
    primaryContainer = LavenderDeep,
    onPrimaryContainer = LilacLight,
    secondary = LilacLight,
    onSecondary = DarkBg,
    secondaryContainer = LavenderDeep,
    onSecondaryContainer = LilacLight,
    tertiary = RoseSoft,
    onTertiary = DarkBg,
    tertiaryContainer = RoseDeep,
    onTertiaryContainer = RoseSoft,
    background = DarkBg,
    onBackground = DarkText,
    surface = DarkSurface,
    onSurface = DarkText,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = DarkTextSecondary,
    outline = Color(0xFF3E3967),
    outlineVariant = Color(0xFF2E2A51),
    error = Rose,
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
