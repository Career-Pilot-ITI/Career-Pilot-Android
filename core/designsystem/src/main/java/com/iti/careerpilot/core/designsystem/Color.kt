package com.iti.careerpilot.core.designsystem

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

object CareerPilotPalette {
    val navy = Color(0xFF1B2340)
    val navyMid = Color(0xFF232C52)
    val navyDark = Color(0xFF0E1428)
    val navyLight = Color(0xFF2D3B6B)
    val amber = Color(0xFFFF7A45)
    val amberLight = Color(0xFFFF9E72)
    val teal = Color(0xFF2DD4BF)
    val tealLight = Color(0xFF99F6E4)
    val green = Color(0xFF22C55E)
    val yellow = Color(0xFFF59E0B)
    val coral = Color(0xFFEF4444)
    val offWhite = Color(0xFFF7F8FC)
    val white = Color(0xFFFFFFFF)
    val gray100 = Color(0xFFF1F3F9)
    val gray200 = Color(0xFFE4E8F0)
    val gray400 = Color(0xFF9AA3B8)
    val gray600 = Color(0xFF5B657E)
    
    // Dark mode specific surfaces
    val darkSurface = Color(0xFF141B35)
    val darkSurfaceAlt = Color(0xFF1A2140)
    val darkBorder = Color(0x14FFFFFF) // rgba(255,255,255,0.08)
}

val LightColors = lightColorScheme(
    primary = CareerPilotPalette.amber,
    onPrimary = CareerPilotPalette.white,
    secondary = CareerPilotPalette.teal,
    background = CareerPilotPalette.offWhite,
    surface = CareerPilotPalette.white,
    error = CareerPilotPalette.coral,
    outline = CareerPilotPalette.gray200,
    onBackground = CareerPilotPalette.navyDark,
    onSurface = CareerPilotPalette.navyDark
)

val DarkColors = darkColorScheme(
    primary = CareerPilotPalette.amber,
    onPrimary = CareerPilotPalette.white,
    secondary = CareerPilotPalette.teal,
    background = CareerPilotPalette.navyDark,
    surface = CareerPilotPalette.darkSurface,
    error = CareerPilotPalette.coral,
    outline = CareerPilotPalette.darkBorder,
    onBackground = CareerPilotPalette.offWhite,
    onSurface = CareerPilotPalette.offWhite
)


