package com.iti.careerpilot.practicesession.presentation.practicescreen.screen.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.random.Random


@Composable
fun AmbientStageBackdrop(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "ambient")
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drift"
    )
    val particles = remember {
        List(28) { Random.nextFloat() to Random.nextFloat() }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(studioColors.Background)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // two soft blobs drifting slowly, kept low-opacity so they read as
            // atmosphere, not decoration
            drawCircle(
                brush = Brush.radialGradient(
                    0f to studioColors.GradientStops[0].copy(alpha = 0.18f),
                    1f to Color.Transparent,
                    center = Offset(w * (0.15f + drift * 0.1f), h * 0.2f),
                    radius = w * 0.65f
                ),
                radius = w * 0.65f,
                center = Offset(w * (0.15f + drift * 0.1f), h * 0.2f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    0f to studioColors.GradientStops[2].copy(alpha = 0.14f),
                    1f to Color.Transparent,
                    center = Offset(w * (0.9f - drift * 0.12f), h * 0.85f),
                    radius = w * 0.7f
                ),
                radius = w * 0.7f,
                center = Offset(w * (0.9f - drift * 0.12f), h * 0.85f)
            )

            // fine dust — fixed positions, just a static texture, cheap to draw
            particles.forEach { (px, py) ->
                drawCircle(
                    color = studioColors.TextMuted.copy(alpha = 0.06f),
                    radius = 1.4.dp.toPx(),
                    center = Offset(w * px, h * py)
                )
            }
        }
    }
}

@Immutable
data class StudioColors(
    val Background: Color,
    val SurfaceElevated: Color,
    val SurfaceElevated2: Color,
    val TextPrimary: Color,
    val TextMuted: Color,
    val Live: Color,
    val GradientStops: List<Color>,
    val BrandBrush: Brush,
)

val studioColors = StudioColors(
    Background = Color(0xFF0E0F1A),
    SurfaceElevated = Color(0xFF171929),
    SurfaceElevated2 = Color(0xFF1E2036),
    TextPrimary = Color(0xFFF5F4FA),
    TextMuted = Color(0xFF9490B0),
    Live = Color(0xFFFF5C72),
    GradientStops = listOf(
        Color(0xFF6D5DF6), // indigo
        Color(0xFF9D5CF9), // violet
        Color(0xFF3ED6C6), // teal
    ),
    BrandBrush = Brush.linearGradient(
        listOf(
            Color(0xFF6D5DF6), // indigo
            Color(0xFF9D5CF9), // violet
            Color(0xFF3ED6C6), // teal
        )
    ),
)
