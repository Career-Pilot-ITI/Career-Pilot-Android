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
    val success: Color,
    val onSuccessContainer: Color,
    val successContainer: Color,
    val warning: Color,
    val onWarningContainer: Color,
    val warningContainer: Color,
    val info: Color,
    val onInfoContainer: Color,
    val infoContainer: Color,
    val error: Color,
    val onErrorContainer: Color,
    val errorContainer: Color,
    // Studio colors
    val studioBackground: Color,
    val studioSurfaceElevated: Color,
    val studioSurfaceElevated2: Color,
    val studioTextPrimary: Color,
    val studioTextMuted: Color,
    val studioLive: Color,
    val studioGradientStops: List<Color>,
    val studioBrandBrush: Brush,
)

val LightExtendedColors = ExtendedColors(
    radialGradientStart = Color(0xFFE0EAFC),
    radialGradientEnd = Color(0x1C202B0F),
    pulsingRing = Color(0xFFCFD9DF),
    actionBorder = Color(0x80D1D1D1),
    success = Color(0xFF22C55E),
    onSuccessContainer = Color(0xFF22C55E),
    successContainer = Color(0xFFEBFAF1),
    warning = Color(0xFFF59E0B),
    onWarningContainer = CareerPilotPalette.amber,
    warningContainer = Color(0xFFFEF7EB),
    info = Color(0xFF14B8A6),
    onInfoContainer = Color(0xFF2DD4BF),
    infoContainer = Color(0xFFE7F8F6),
    error = Color(0xFFFF0000),
    onErrorContainer = Color(0xFFFF0000),
    errorContainer = Color(0xFFFFE8E8),
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
)

val DarkExtendedColors = ExtendedColors(
    radialGradientStart = Color(0xFF0C1B28),
    radialGradientEnd = Color(0x1C202B0F),
    pulsingRing = Color(0xFF02213B),
    actionBorder = Color(0x80474747),
    success = Color(0xFF4ADE80),
    onSuccessContainer = Color(0xFF22C55E),
    successContainer = Color(0xFF123D28),
    warning = Color(0xFFFBBF24),
    onWarningContainer = CareerPilotPalette.amber,
    warningContainer = Color(0xFF49340D),
    info = Color(0xFF2DD4BF),
    onInfoContainer = Color(0xFF2DD4BF),
    infoContainer = Color(0xFF123F3B),
    error = Color(0xFFE7F8F6),
    onErrorContainer = Color(0xFFE7F8F6),
    errorContainer = Color(0xFFE7F8F6),
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
)

val LocalExtendedColors = staticCompositionLocalOf {
    LightExtendedColors
}
