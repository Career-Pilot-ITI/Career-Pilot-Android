package com.iti.careerpilot.practicesession.presentation.screen.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.iti.careerpilot.practicesession.R
import com.iti.careerpilot.practicesession.presentation.action.PracticeSessionAction
import com.iti.careerpilot.practicesession.presentation.screen.util.formatDuration

@Composable
fun PracticeSessionBottomSection(
    isRecording: Boolean,
    recordedAudioPath: String?,
    isPlayingAudio: Boolean,
    playbackDurationMs: Long,
    playbackPositionMs: Long,
    amplitudes: List<Float>,
    showQuestionCard: Boolean,
    onAction: (PracticeSessionAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(modifier = Modifier.padding(bottom = 8.dp)) {
            if (recordedAudioPath != null && !isRecording) {
                AudioReviewRow(
                    isPlayingAudio = isPlayingAudio,
                    playbackDurationMs = playbackDurationMs,
                    playbackPositionMs = playbackPositionMs,
                    onDiscard = { onAction(PracticeSessionAction.ShowOrHideDiscardConfirmDialog(true)) },
                    onTogglePlay = { onAction(PracticeSessionAction.TogglePlayingCurrentRecordedAnswer) },
                    onSeek = { ms -> onAction(PracticeSessionAction.SeekAudioTo(ms)) },
                    onSubmit = { onAction(PracticeSessionAction.SubmitAnswerToCurrentQuestion) }
                )
            } else {
                ActionBottomBar(
                    isRecording = isRecording,
                    amplitudes = amplitudes,
                    showQuestionCard = showQuestionCard,
                    onOpenSettings = { onAction(PracticeSessionAction.ShowOrHideSettingsBottomSheet(true)) },
                    onStartRecording = { onAction(PracticeSessionAction.StartRecordingAnswer) },
                    onStopRecording = { onAction(PracticeSessionAction.StopRecordingAnswer) },
                    onShowPermissionDialog = { onAction(PracticeSessionAction.ShowOrHidePermissionDialog(true)) },
                    onToggleQuestionCard = { onAction(PracticeSessionAction.ToggleQuestionCard) }
                )
            }
        }
    }
}

@Composable
private fun AudioReviewRow(
    isPlayingAudio: Boolean,
    playbackDurationMs: Long,
    playbackPositionMs: Long,
    onDiscard: () -> Unit,
    onTogglePlay: () -> Unit,
    onSeek: (Long) -> Unit,
    onSubmit: () -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
        Box(modifier = Modifier.fillMaxWidth()) {
            IconButton(
                modifier = Modifier.align(Alignment.TopStart),
                onClick = onDiscard,
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete_recording),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalIconButton(onClick = onTogglePlay) {
                Icon(
                    imageVector = if (isPlayingAudio) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlayingAudio) {
                        stringResource(R.string.pause)
                    } else {
                        stringResource(R.string.play)
                    }
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
            LargeGradientIconButton(
                icon = Icons.AutoMirrored.Rounded.Send,
                contentDescription = stringResource(R.string.submit_answer),
                onClick = onSubmit,
                size = 56.dp
            )
        }
    }
}

@Composable
private fun ActionBottomBar(
    isRecording: Boolean,
    amplitudes: List<Float>,
    showQuestionCard: Boolean,
    onOpenSettings: () -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onShowPermissionDialog: () -> Unit,
    onToggleQuestionCard: () -> Unit,
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = onToggleQuestionCard,
            ) {
                Icon(
                    imageVector = if (showQuestionCard) Icons.Default.Description else Icons.Default.GraphicEq,
                    contentDescription = stringResource(R.string.toggle_question_card),
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Box(
            modifier = Modifier.weight(2f),
            contentAlignment = Alignment.Center
        ) {
            AmplitudeRings(
                amplitudes = amplitudes,
                isRecording = isRecording,
            )
            LargeGradientIconButton(
                icon = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                contentDescription = if (isRecording) {
                    stringResource(R.string.stop_recording)
                } else {
                    stringResource(R.string.start_recording)
                },
                onClick = {
                    if (isRecording) {
                        onStopRecording()
                    } else {
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                        if (hasPermission) onStartRecording()
                        else onShowPermissionDialog()
                    }
                },
                size = 80.dp,
            )
        }

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            IconButton(onClick = onOpenSettings) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(R.string.session_settings),
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
