package com.iti.careerpilot.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color


@Immutable
data class ExtendedColors(
    val primaryOrange: Color,
)

val LocalExtendedColors = staticCompositionLocalOf {
    lightExtendedColors
}

val lightExtendedColors = ExtendedColors(
    primaryOrange = Color(0xFFFB8C00)
)

val darkExtendedColors = ExtendedColors(
    primaryOrange = Color(0xFFFFCC80)
)