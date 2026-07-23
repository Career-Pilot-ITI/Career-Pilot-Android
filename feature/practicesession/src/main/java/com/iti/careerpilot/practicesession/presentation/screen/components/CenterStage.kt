package com.iti.careerpilot.practicesession.presentation.screen.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.practicesession.R

@Composable
fun CenterStage(
    isRecording: Boolean,
    amplitudes: List<Float>,
    isReadingQuestion: Boolean,
    hasRecordedAudio: Boolean,
    onToggleListening: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.size(260.dp), contentAlignment = Alignment.Center) {
        if (isRecording) {
            AmplitudeRings(
                amplitudes = amplitudes,
                modifier = Modifier.fillMaxSize()
            )
        }
        AiTalkingAnimation(
            isPulsing = isReadingQuestion,
            onClick = onToggleListening
        )
        val icon = when {
            isRecording -> Icons.Default.Mic
            hasRecordedAudio -> Icons.Default.GraphicEq
            isReadingQuestion -> Icons.AutoMirrored.Filled.VolumeUp
            else -> Icons.Default.RecordVoiceOver
        }
        Icon(
            imageVector = icon,
            contentDescription = stringResource(R.string.session_status_icon),
            tint = Color.White,
            modifier = Modifier.size(42.dp)
        )
    }
}
