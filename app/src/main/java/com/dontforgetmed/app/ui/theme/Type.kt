package com.dontforgetmed.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val DisplayFont = FontFamily.Default

val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = DisplayFont, fontWeight = FontWeight.Bold, fontSize = 44.sp,
        lineHeight = 52.sp, letterSpacing = (-0.5).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = DisplayFont, fontWeight = FontWeight.Bold, fontSize = 36.sp,
        lineHeight = 44.sp, letterSpacing = (-0.3).sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = DisplayFont, fontWeight = FontWeight.Bold, fontSize = 30.sp,
        lineHeight = 38.sp, letterSpacing = (-0.2).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = DisplayFont, fontWeight = FontWeight.SemiBold, fontSize = 24.sp,
        lineHeight = 32.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = DisplayFont, fontWeight = FontWeight.SemiBold, fontSize = 20.sp,
        lineHeight = 28.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = DisplayFont, fontWeight = FontWeight.SemiBold, fontSize = 18.sp,
        lineHeight = 24.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = DisplayFont, fontWeight = FontWeight.SemiBold, fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = DisplayFont, fontWeight = FontWeight.Medium, fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = DisplayFont, fontWeight = FontWeight.Normal, fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = DisplayFont, fontWeight = FontWeight.Normal, fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = DisplayFont, fontWeight = FontWeight.Normal, fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = DisplayFont, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
        lineHeight = 20.sp, letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = DisplayFont, fontWeight = FontWeight.Medium, fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
)
