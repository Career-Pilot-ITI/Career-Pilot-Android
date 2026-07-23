package com.iti.careerpilot.practicesession.presentation.screen.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CenterStage(
    isRecording: Boolean,
    amplitudes: List<Float>, // active recording amplitudes
    isReadingQuestion: Boolean,
    hasRecordedAudio: Boolean,
    onToggleListening: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.size(260.dp),
        contentAlignment = Alignment.Center
    ) {
        AiTalkingAnimation(
            isPulsing = isReadingQuestion,
            onClick = onToggleListening
        )
    }
}
