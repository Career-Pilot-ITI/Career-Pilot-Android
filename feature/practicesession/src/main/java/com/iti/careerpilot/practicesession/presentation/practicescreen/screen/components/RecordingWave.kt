package com.iti.careerpilot.practicesession.presentation.practicescreen.screen.components

import android.content.res.Configuration
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.iti.careerpilot.core.designsystem.CareerPilotPalette
import com.iti.careerpilot.core.designsystem.CareerPilotTheme
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.core.designsystem.components.LoadingWave
import kotlin.math.abs
import kotlin.math.sin

@Composable
fun RecordingWave(
    modifier: Modifier = Modifier,
    barCount: Int = 28,
    color: Color = CareerPilotPalette.teal,
    height: Dp = Dimens.WaveformHeight,
    barWidth: Dp = Dimens.WaveformBarWidth,
    barGap: Dp = Dimens.WaveformBarGap,
) {
    val transition = rememberInfiniteTransition(label = "waveform")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100),
            repeatMode = RepeatMode.Restart,
        ),
        label = "phase",
    )

    Row(
        modifier = modifier.height(height),
        horizontalArrangement = Arrangement.spacedBy(barGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(barCount) { index ->
            val baseAmplitude = 0.35f + 0.65f * abs(sin(index * 1.3f))
            val wave = 0.5f + 0.5f * sin(phase + index * 0.55f)
            val fraction = (0.2f + baseAmplitude * wave).coerceIn(0.12f, 1f)

            WaveBar(
                barWidth = barWidth,
                heightFraction = fraction,
                color = color,
            )
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

@Preview(
    name = "Light",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "Dark",
    showBackground = true,
    backgroundColor = 0xFF121212,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun LoadingWavePreview() {
    CareerPilotTheme {
        LoadingWave()
    }
}