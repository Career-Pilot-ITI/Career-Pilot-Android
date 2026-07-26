package com.iti.careerpilot.practicesession.presentation.practicescreen.screen.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin


@Composable
fun AmplitudeRings(
    amplitudes: List<Float>,
    isRecording: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wavy_border")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "phase"
    )

    val recentAmps = amplitudes.takeLast(10).ifEmpty { listOf(0.1f) }
    val rawAvgAmp = recentAmps.average().toFloat().coerceIn(0.1f, 1f)

    val targetAmp = if (isRecording) rawAvgAmp else 0f
    val avgAmp by animateFloatAsState(targetValue = targetAmp, animationSpec = tween(400))
    val effectivePhase = if (isRecording) phase else 0f


    val rings = listOf(
        Ring(0, MaterialTheme.colorScheme.primary, 2.dp.toPxLocal()),
        Ring(8, MaterialTheme.colorScheme.tertiary, 1.5.dp.toPxLocal()),
        Ring(16, MaterialTheme.colorScheme.secondary, 1.5.dp.toPxLocal()),
    )

    Canvas(modifier = modifier.size(100.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val points = 100

        rings.forEach { ring ->
            val baseRadius = size.width / 2 - 5.dp.toPx() - ring.radiusOffset.dp.toPx()
            val path = Path()

            for (i in 0 until points) {
                val angle = (i.toFloat() / points) * 2 * PI.toFloat()
                val wave1 = sin(angle * 6 + effectivePhase) * 6.dp.toPx()
                val wave2 = sin(angle * 14 - effectivePhase * 1.2f) * 4.dp.toPx()
                val wave3 = cos(angle * 8 + effectivePhase * 0.8f) * 3.dp.toPx()
                val noise = wave1 + wave2 + wave3
                val r = baseRadius + noise * avgAmp
                val x = center.x + r * cos(angle)
                val y = center.y + r * sin(angle)
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()

            drawPath(
                path = path,
                color = ring.color,
                style = Stroke(width = ring.strokeWidth, cap = StrokeCap.Round)
            )
        }
    }
}

data class Ring(
    val radiusOffset: Int,
    val color: Color,
    val strokeWidth: Float
)

@Composable
private fun Dp.toPxLocal(): Float {
    val density = LocalDensity.current
    return with(density) { toPx() }
}