package com.iti.careerpilot.practicesession.presentation.practicescreen.screen.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.iti.careerpilot.core.designsystem.Dimens
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.sin

@Composable
fun RecordingWave(
    modifier: Modifier = Modifier,
    barCount: Int = 28,
    amplitudes: List<Float> = emptyList(),
    color: Color = MaterialTheme.colorScheme.primary,
    height: Dp = Dimens.WaveformHeight,
    barWidth: Dp = Dimens.WaveformBarWidth,
    barGap: Dp = Dimens.WaveformBarGap,
) {

    if (amplitudes.isEmpty()) {
        Row(
            modifier = modifier.height(height),
            horizontalArrangement = Arrangement.spacedBy(barGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(barCount) { index ->
                val fraction = (0.15f + 0.2f * abs(sin(index * 0.8f))).coerceIn(0.12f, 1f)
                WaveBar(barWidth = barWidth, heightFraction = fraction, color = color)
            }
        }
    } else {

        val stride = 2
        val windowSize = barCount + 2
        val displayAmps = remember(amplitudes) {
            amplitudes.filterIndexed { i, _ -> i % stride == 0 }.takeLast(windowSize)
        }

        val slide = remember { Animatable(1f) }
        LaunchedEffect(displayAmps) {
            slide.snapTo(1f)
            slide.animateTo(0f, animationSpec = tween(durationMillis = 150, easing = LinearEasing))
        }

        Row(
            modifier = modifier.height(height),
            horizontalArrangement = Arrangement.spacedBy(barGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val offset = barCount - displayAmps.size
            repeat(barCount) { index ->
                val virtualIndex = index + slide.value
                val ampIndex = floor(virtualIndex - offset).toInt()

                val amp = if (ampIndex >= 0 && ampIndex < displayAmps.size) {
                    val weight = virtualIndex - floor(virtualIndex)
                    val current = displayAmps[ampIndex]
                    val next = if (ampIndex + 1 < displayAmps.size) displayAmps[ampIndex + 1] else current
                    current * (1 - weight) + next * weight
                } else 0f

                WaveBar(
                    barWidth = barWidth,
                    heightFraction = (0.12f + amp * 0.88f).coerceIn(0.12f, 1f),
                    color = color,
                )
            }
        }
    }
}

@Composable
private fun WaveBar(
    barWidth: Dp,
    heightFraction: Float,
    color: Color,
) {
    Box(
        modifier = Modifier
            .width(barWidth)
            .fillMaxHeight(heightFraction)
            .background(color = color, shape = RoundedCornerShape(percent = 50)),
    )
}

