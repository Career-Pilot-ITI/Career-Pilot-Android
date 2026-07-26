package com.iti.careerpilot.practicesession.presentation.practicescreen.screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.core.designsystem.CareerPilotTheme
import com.iti.careerpilot.practicesession.R
import com.iti.careerpilot.practicesession.presentation.practicescreen.screen.util.formatDuration


@Composable
fun AudioReviewRow(
    isPlayingAudio: Boolean,
    playbackDurationMs: Long,
    playbackPositionMs: Long,
    onDiscard: () -> Unit,
    onTogglePlay: () -> Unit,
    onSeek: (Long) -> Unit,
    onSubmit: () -> Unit,
) {
    val extendedColors = CareerPilotTheme.extendedColors

    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
        Box(modifier = Modifier.fillMaxWidth()) {
            PracticeIconButton(
                onClick = onDiscard,
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete_recording),
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PracticeIconButton(onClick = onTogglePlay) {
                Icon(
                    imageVector = if (isPlayingAudio) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlayingAudio) {
                        stringResource(R.string.pause)
                    } else {
                        stringResource(R.string.play)
                    },
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                val progress = if (playbackDurationMs > 0) {
                    (playbackPositionMs.toFloat() / playbackDurationMs).coerceIn(0f, 1f)
                } else 0f
                WavySeekSlider(
                    progress = progress,
                    isPlaying = isPlayingAudio,
                    onSeek = { p: Float ->
                        val targetMs = (p * playbackDurationMs).toLong()
                        onSeek(targetMs)
                    }
                )
                Spacer(Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatDuration(playbackPositionMs),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatDuration(playbackDurationMs),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.background, CircleShape)
                    .drawBehind {
                        drawRect(
                            brush = Brush.radialGradient(
                                0f to extendedColors.radialGradientStart,
                                1f to extendedColors.radialGradientEnd,
                                radius = 150f,
                                center = Offset(75f, 75f)
                            )
                        )
                    }
                    .border(1.dp, extendedColors.actionBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                LargeGradientIconButton(
                    icon = ImageVector.vectorResource(R.drawable.ic_send),
                    contentDescription = stringResource(R.string.submit_answer),
                    onClick = onSubmit,
                    size = 56.dp
                )
            }
        }
    }
}
