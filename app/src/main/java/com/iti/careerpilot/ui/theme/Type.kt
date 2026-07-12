package com.iti.careerpilot.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.iti.careerpilot.R

private val InterFontFamily = FontFamily(
    Font(
        resId = R.font.inter_light,
        weight = FontWeight.Light,
    ),
    Font(
        resId = R.font.inter,
        weight = FontWeight.Normal,
    ),
    Font(
        resId = R.font.inter_medium,
        weight = FontWeight.Medium,
    ),
    Font(
        resId = R.font.inter_semibold,
        weight = FontWeight.SemiBold,
    ),
    Font(
        resId = R.font.inter_bold,
        weight = FontWeight.Bold,
    ),
)

private val baseline = Typography()


val Typography = Typography(
    displayLarge = baseline.displayLarge.copy(fontFamily = InterFontFamily),
    displayMedium = baseline.displayMedium.copy(fontFamily = InterFontFamily),
    displaySmall = baseline.displaySmall.copy(fontFamily = InterFontFamily),
    headlineLarge = baseline.headlineLarge.copy(fontFamily = InterFontFamily),
    headlineMedium = baseline.headlineMedium.copy(fontFamily = InterFontFamily),
    headlineSmall = baseline.headlineSmall.copy(fontFamily = InterFontFamily),
    titleLarge = baseline.titleLarge.copy(fontFamily = InterFontFamily),
    titleMedium = baseline.titleMedium.copy(fontFamily = InterFontFamily),
    titleSmall = baseline.titleSmall.copy(fontFamily = InterFontFamily),
    bodyLarge = baseline.bodyLarge.copy(fontFamily = InterFontFamily),
    bodyMedium = baseline.bodyMedium.copy(fontFamily = InterFontFamily),
    bodySmall = baseline.bodySmall.copy(fontFamily = InterFontFamily),
    labelLarge = baseline.labelLarge.copy(fontFamily = InterFontFamily),
    labelMedium = baseline.labelMedium.copy(fontFamily = InterFontFamily),
    labelSmall = baseline.labelSmall.copy(fontFamily = InterFontFamily),
)