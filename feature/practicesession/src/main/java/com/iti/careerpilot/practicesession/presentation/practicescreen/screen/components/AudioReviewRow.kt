package com.iti.careerpilot.practicesession.presentation.practicescreen.screen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.practicesession.R
import com.iti.careerpilot.practicesession.presentation.practicescreen.screen.util.formatDuration

@Composable
fun AudioReviewRow(
    isPlayingAudio: Boolean,
    playbackDurationMs: Long,
    playbackPositionMs: Long,
    onTogglePlay: () -> Unit,
    onSeek: (Long) -> Unit,
    onDiscard: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PracticeIconButton(
            onClick = onDiscard,
            iconId = R.drawable.ic_delete,
            descriptionId = R.string.discard,
            tint = MaterialTheme.colorScheme.error,
        )

        Spacer(Modifier.width(8.dp))

        PracticeIconButton(
            onClick = onTogglePlay,
            iconId = if (isPlayingAudio) R.drawable.ic_pause else R.drawable.ic_play,
            descriptionId = if (isPlayingAudio) R.string.pause else R.string.play
        )

        Spacer(Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            val progress = if (playbackDurationMs > 0) {
                (playbackPositionMs.toFloat() / playbackDurationMs).coerceIn(0f, 1f)
            } else 0f
            WavySeekSlider(
                progress = progress,
                isPlaying = isPlayingAudio,
                onSeek = { p ->
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

        Spacer(Modifier.width(10.dp))

        LargeGradientIconButton(
            icon = ImageVector.vectorResource(R.drawable.ic_send),
            contentDescription = stringResource(R.string.submit_answer),
            onClick = onSubmit,
            size = 52.dp
        )
    }
}
