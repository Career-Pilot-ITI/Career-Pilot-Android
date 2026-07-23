package com.iti.careerpilot.practicesession.presentation.practicescreen.screen.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(barGap, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
        userScrollEnabled = false,
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        itemsIndexed(
            items = volumeBars,
            key = { _, bar -> bar.id }
        ) { index, bar ->
            val al by animateFloatAsState(
                if (index >= volumeBars.size - 2 || index < 2) 0f
                else 1f
            )
            Box(
                modifier = Modifier
                    .animateItem(
                        fadeInSpec = null,
                        fadeOutSpec = null,
                        placementSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    )
                    .alpha(al)
                    .width(barWidth)
                    .heightIn(min = 4.dp)
                    .fillMaxHeight(bar.value)
                    .background(color = color, shape = CircleShape)
            )
        }
    }
}
