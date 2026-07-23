package com.iti.careerpilot.practicesession.presentation.screen.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Three rings of bars orbiting the orb, each ring reading a different slice
 * of the recent amplitude history, rotating at different speeds/directions.
 */
@Composable
fun AmplitudeRings(amplitudes: List<Float>, modifier: Modifier = Modifier) {
    val infinite = rememberInfiniteTransition(label = "rings")
    val rotation1 by infinite.animateFloat(
        0f, 360f, infiniteRepeatable(tween(14000, easing = LinearEasing)), label = "rot1"
    )
    val rotation2 by infinite.animateFloat(
        360f, 0f, infiniteRepeatable(tween(10000, easing = LinearEasing)), label = "rot2"
    )
    val rotation3 by infinite.animateFloat(
        0f, 360f, infiniteRepeatable(tween(18000, easing = LinearEasing)), label = "rot3"
    )

    val recent = amplitudes.takeLast(48).ifEmpty { List(48) { 0.05f } }
    val ringA = recent.filterIndexed { i, _ -> i % 3 == 0 }.ifEmpty { listOf(0.05f) }
    val ringB = recent.filterIndexed { i, _ -> i % 3 == 1 }.ifEmpty { listOf(0.05f) }
    val ringC = recent.filterIndexed { i, _ -> i % 3 == 2 }.ifEmpty { listOf(0.05f) }

    val barColor = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier) {
        val ringConfigs = listOf(
            Triple(ringA, size.minDimension * 0.30f, rotation1),
            Triple(ringB, size.minDimension * 0.38f, rotation2),
            Triple(ringC, size.minDimension * 0.46f, rotation3)
        )
        ringConfigs.forEach { (values, radius, rot) ->
            val count = values.size
            rotate(rot) {
                for (i in 0 until count) {
                    val angle = (2 * PI * i / count).toFloat()
                    val amp = values[i].coerceIn(0.05f, 1f)
                    val barLen = 5.dp.toPx() + amp * 16.dp.toPx()
                    val start = Offset(
                        center.x + cos(angle) * radius,
                        center.y + sin(angle) * radius
                    )
                    val end = Offset(
                        center.x + cos(angle) * (radius + barLen),
                        center.y + sin(angle) * (radius + barLen)
                    )
                    drawLine(
                        color = barColor.copy(alpha = 0.5f + amp * 0.5f),
                        start = start,
                        end = end,
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}
