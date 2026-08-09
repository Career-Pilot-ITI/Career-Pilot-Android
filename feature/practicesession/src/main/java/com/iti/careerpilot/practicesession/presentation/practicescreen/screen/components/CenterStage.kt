package com.iti.careerpilot.practicesession.presentation.practicescreen.screen.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CenterStage(
    isReadingQuestion: Boolean,
    isRecording: Boolean,
    isVideoInterview: Boolean,
    onToggleListening: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.size(260.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isVideoInterview) {
            // Animated AI Interviewer Robot character for video interviews
            AiInterviewerAvatar(
                isReadingQuestion = isReadingQuestion,
                isRecording = isRecording,
                onClick = onToggleListening
            )
        } else {
            // Classic AI Pulsing Sphere for audio interviews
            AiTalkingAnimation(
                isPulsing = isReadingQuestion,
                onClick = onToggleListening
            )
        }
    }
}
