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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.core.designsystem.CareerPilotTheme
import kotlin.random.Random


@Composable
fun AmbientStageBackdrop(modifier: Modifier = Modifier) {
    val extendedColors = CareerPilotTheme.extendedColors
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
            .background(extendedColors.studioBackground)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // two soft blobs drifting slowly, kept low-opacity so they read as
            // atmosphere, not decoration
            drawCircle(
                brush = Brush.radialGradient(
                    0f to extendedColors.studioGradientStops[0].copy(alpha = 0.18f),
                    1f to Color.Transparent,
                    center = Offset(w * (0.15f + drift * 0.1f), h * 0.2f),
                    radius = w * 0.65f
                ),
                radius = w * 0.65f,
                center = Offset(w * (0.15f + drift * 0.1f), h * 0.2f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    0f to extendedColors.studioGradientStops[2].copy(alpha = 0.14f),
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
                    color = extendedColors.studioTextMuted.copy(alpha = 0.06f),
                    radius = 1.4.dp.toPx(),
                    center = Offset(w * px, h * py)
                )
            }
        }
    }
}
