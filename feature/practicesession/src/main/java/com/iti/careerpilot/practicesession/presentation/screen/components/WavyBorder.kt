package com.iti.careerpilot.practicesession.presentation.screen.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun WavyBorder(amplitudes: List<Float>) {
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
    val avgAmp = recentAmps.average().toFloat().coerceIn(0.1f, 1f)

    val gradientColors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.secondary
    )
    Canvas(modifier = Modifier.size(100.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val baseRadius = size.width / 2 - 5.dp.toPx()
        val path = Path()

        val points = 100

        for (i in 0 until points) {
            val angle = (i.toFloat() / points) * 2 * PI.toFloat()

            // Combine multiple sine waves with different frequencies and phases for a "random" organic feel
            val wave1 = sin(angle * 6 + phase) * 6.dp.toPx()
            val wave2 = sin(angle * 14 - phase * 1.2f) * 4.dp.toPx()
            val wave3 = cos(angle * 8 + phase * 0.8f) * 3.dp.toPx()

            val noise = wave1 + wave2 + wave3
            val r = baseRadius + noise * avgAmp
            val x = center.x + r * cos(angle)
            val y = center.y + r * sin(angle)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()

        drawPath(
            path = path,
            brush = Brush.linearGradient(colors = gradientColors),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}
