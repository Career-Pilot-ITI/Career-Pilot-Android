package com.iti.careerpilot.practicesession.presentation.practicescreen.state

import androidx.compose.runtime.Immutable
import com.iti.careerpilot.challengefirestore.ChallengeSession
import com.iti.careerpilot.practicesession.domain.audio.models.AudioPlaybackState
import com.iti.careerpilot.practicesession.domain.models.Session
import kotlin.time.Duration

@Immutable
data class PracticeSessionState(
    val isLoadingSession: Boolean = false,
    val sessionId: Long = 0L,
    val firestoreSessionId: String? = null,
    val challengeId: String? = null,
    val showPermissionDialog: Boolean = false,
    val currentSession: Session? = null,
    val firestoreSession: ChallengeSession? = null,
    val isRecording: Boolean = false,
    val isReadingQuestion: Boolean = false,
    val recordedAudioPath: String? = null,
    val amplitudes: List<Float> = emptyList(),
    val volumeBars: List<VolumeBar> = emptyList(),
    val recordingDuration: Duration = Duration.ZERO,
    val isUploadingAndTranscribingAudio: Boolean = false,
    val isSendingAnswer: Boolean = false,
    val isEmptyAnswer: Boolean = false,
    val uploadProgress: Int = 0,
    val playbackDurationMs: Long = 0,
    val playbackPositionMs: Long = 0,
    val transcription: String? = null,
    val error: String? = null,
    val isFinished: Boolean = false,
    val isPlayingAudio: Boolean = false,
    val playbackState: AudioPlaybackState = AudioPlaybackState.STOPPED,
    val totalSessionDuration: Duration = Duration.ZERO,
    val showQuestionCard: Boolean = true,
    val showDiscardConfirm: Boolean = false,
    val showLeaveConfirm: Boolean = false,
    val showSettingsBottomSheet: Boolean = false,
    val autoReadQuestion: Boolean = true,

    // Body language
    val isVideoSessionSelected: Boolean = false,
    val bodyLanguageEnabled: Boolean = false,
    val bodyLanguageConsentGiven: Boolean = false,
    val isCameraPreviewVisible: Boolean = true,
    val isBodyLanguageAnalyzing: Boolean = false,
    val enablePostureTracking: Boolean = false,
    val enableHandTracking: Boolean = false,
) {
    val displayQuestionText: String?
        get() = firestoreSession?.currentQuestion?.questionText ?: currentSession?.currentQuestion?.questionText

    val displayQuestionOrder: Int?
        get() = firestoreSession?.currentQuestion?.questionOrder ?: currentSession?.currentQuestion?.questionOrder

    val displayMaxQuestions: Int?
        get() = firestoreSession?.maxQuestions ?: currentSession?.maxQuestions
}
