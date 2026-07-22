package com.iti.careerpilot.practicesession.presentation.state

import androidx.compose.runtime.Immutable
import com.iti.careerpilot.practicesession.domain.models.Session
import kotlin.time.Duration

@Immutable
data class PracticeSessionState(
    val isLoading: Boolean = false,
    val sessionId: Long = 0L,
    val showPermissionDialog: Boolean = false,
    val currentSession: Session? = null,
    val isRecording: Boolean = false,
    val isReadingQuestion: Boolean = false,
    val recordedAudioPath: String? = null,
    val amplitudes: List<Float> = emptyList(),
    val recordingDuration: Duration = Duration.ZERO,
    val isTranscribing: Boolean = false,
    val isUploading: Boolean = false,
    val uploadProgress: Int = 0,
    val transcription: String? = null,
    val error: String? = null,
    val isFinished: Boolean = false,
    val isPlayingAudio: Boolean = false
)