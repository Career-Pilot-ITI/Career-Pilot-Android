package com.iti.careerpilot.practicesession.presentation.practicescreen.screen.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.practicesession.presentation.practicescreen.state.VolumeBar

@Composable
fun RecordingWave(
    volumeBars: List<VolumeBar>,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    height: Dp = Dimens.WaveformHeight,
    barWidth: Dp = Dimens.WaveformBarWidth,
    barGap: Dp = Dimens.WaveformBarGap,
) {
    Canvas(
        modifier = modifier
            .height(height)
    ) {
        if (volumeBars.isEmpty()) return@Canvas

        val barWidthPx = barWidth.toPx()
        val barGapPx = barGap.toPx()
        val minBarHeightPx = 4.dp.toPx()
        val totalBars = volumeBars.size

        val totalWaveformWidth = totalBars * barWidthPx + (totalBars - 1) * barGapPx
        val startX = (size.width - totalWaveformWidth) / 2f

        volumeBars.forEachIndexed { index, bar ->
            val alpha = when (index) {
                0, totalBars - 1 -> 0.2f
                1, totalBars - 2 -> 0.6f
                else -> 1f
            }

            val barHeight = (bar.value * size.height).coerceIn(minBarHeightPx, size.height)
            val x = startX + index * (barWidthPx + barGapPx)
            val y = (size.height - barHeight) / 2f

            drawRoundRect(
                color = color.copy(alpha = alpha),
                topLeft = Offset(x, y),
                size = Size(barWidthPx, barHeight),
                cornerRadius = CornerRadius(barWidthPx / 2f, barWidthPx / 2f)
            )
        }
    }
}
