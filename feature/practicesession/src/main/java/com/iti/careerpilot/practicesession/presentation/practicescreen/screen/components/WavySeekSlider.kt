package com.iti.careerpilot.practicesession.presentation.practicescreen.screen.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.WavyProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun WavySeekSlider(
    progress: Float,
    isPlaying: Boolean,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var dragValue by remember { mutableStateOf<Float?>(null) }
    val sliderValue = dragValue ?: progress
    val interactionSource = remember { MutableInteractionSource() }

    Slider(
        value = sliderValue,
        onValueChange = { dragValue = it.coerceIn(0f, 1f) },
        onValueChangeFinished = {
            dragValue?.let(onSeek)
            dragValue = null
        },
        valueRange = 0f..1f,
        interactionSource = interactionSource,
        modifier = modifier
            .fillMaxWidth(),
        track = { sliderState ->
            LinearWavyProgressIndicator(
                progress = { sliderState.value },
                modifier = Modifier.fillMaxWidth(),
                amplitude = { p ->
                    if (isPlaying) WavyProgressIndicatorDefaults.indicatorAmplitude(p) else 0f
                }
            )
        }
    )
}
