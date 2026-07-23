package com.iti.careerpilot.practicesession.presentation.practicescreen.screen.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.practicesession.presentation.practicescreen.action.PracticeSessionAction

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

