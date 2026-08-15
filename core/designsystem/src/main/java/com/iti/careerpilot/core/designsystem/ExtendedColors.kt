package com.iti.careerpilot.core.designsystem

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Immutable
data class ExtendedColors(
    val radialGradientStart: Color,
    val radialGradientEnd: Color,
    val pulsingRing: Color,
    val actionBorder: Color,
    // Studio colors
    val studioBackground: Color,
    val studioSurfaceElevated: Color,
    val studioSurfaceElevated2: Color,
    val studioTextPrimary: Color,
    val studioTextMuted: Color,
    val studioLive: Color,
    val studioGradientStops: List<Color>,
    val studioBrandBrush: Brush,
    // IntelliJ-like code block colors
    val codeBackground: Color,
    val codeForeground: Color,
)

val LightExtendedColors = ExtendedColors(
    radialGradientStart = Color(0xFFE0EAFC),
    radialGradientEnd = Color(0x1C202B0F),
    pulsingRing = Color(0xFFCFD9DF),
    actionBorder = Color(0x80D1D1D1),
    studioBackground = Color(0xFFF7F8FC),
    studioSurfaceElevated = Color(0xFFFFFFFF),
    studioSurfaceElevated2 = Color(0xFFF1F3F9),
    studioTextPrimary = Color(0xFF1B2340),
    studioTextMuted = Color(0xFF5B657E),
    studioLive = Color(0xFFFF5C72),
    studioGradientStops = listOf(
        Color(0xFF6D5DF6),
        Color(0xFF9D5CF9),
        Color(0xFF3ED6C6),
    ),
    studioBrandBrush = Brush.linearGradient(
        listOf(
            Color(0xFF6D5DF6),
            Color(0xFF9D5CF9),
            Color(0xFF3ED6C6),
        )
    ),
    codeBackground = Color(0xFFF8F8F8),
    codeForeground = Color(0xFF000000),
)

val DarkExtendedColors = ExtendedColors(
    radialGradientStart = Color(0xFF0C1B28),
    radialGradientEnd = Color(0x1C202B0F),
    pulsingRing = Color(0xFF02213B),
    actionBorder = Color(0x80474747),
    studioBackground = Color(0xFF0E0F1A),
    studioSurfaceElevated = Color(0xFF171929),
    studioSurfaceElevated2 = Color(0xFF1E2036),
    studioTextPrimary = Color(0xFFF5F4FA),
    studioTextMuted = Color(0xFF9490B0),
    studioLive = Color(0xFFFF5C72),
    studioGradientStops = listOf(
        Color(0xFF6D5DF6),
        Color(0xFF9D5CF9),
        Color(0xFF3ED6C6),
    ),
    studioBrandBrush = Brush.linearGradient(
        listOf(
            Color(0xFF6D5DF6),
            Color(0xFF9D5CF9),
            Color(0xFF3ED6C6),
        )
    ),
    codeBackground = Color(0xFF2B2B2B),
    codeForeground = Color(0xFFB8C5D3),
)

val LocalExtendedColors = staticCompositionLocalOf {
    LightExtendedColors
}
