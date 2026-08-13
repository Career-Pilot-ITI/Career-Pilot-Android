package com.iti.careerpilot.practicesession.presentation.practicescreen.action

sealed interface PracticeSessionAction {

    data class CreateNewPracticeSession(
        val trackId: Long,
        val isVideoSession: Boolean = false,
    ) : PracticeSessionAction

    data class RestartPracticeSession(
        val sessionId: Long,
        val isVideoSession: Boolean = false,
    ) : PracticeSessionAction

    data class ShowOrHidePermissionDialog(
        val show: Boolean
    ) : PracticeSessionAction

    data class ShowOrHideDiscardConfirmDialog(
        val show: Boolean
    ) : PracticeSessionAction

    data class ShowOrHideSettingsBottomSheet(
        val show: Boolean
    ) : PracticeSessionAction

    data class ShowOrHideLeaveConfirmDialog(
        val show: Boolean
    ) : PracticeSessionAction

    data class ToggleAutoReadQuestion(
        val enabled: Boolean
    ) : PracticeSessionAction

    data object ListenToAIReadingCurrentQuestion: PracticeSessionAction
    data object PauseListeningToCurrentQuestion: PracticeSessionAction
    data object StopListeningToCurrentQuestionAndStartAnswering: PracticeSessionAction

    data object StartRecordingAnswer: PracticeSessionAction
    data object PauseRecordingAnswer: PracticeSessionAction
    data object ResumeRecordingAnswer: PracticeSessionAction
    data object StopRecordingAnswer: PracticeSessionAction

    data object TogglePlayingCurrentRecordedAnswer: PracticeSessionAction
    data class SeekAudioTo(val positionMs: Long): PracticeSessionAction

    data object SubmitAnswerToCurrentQuestion: PracticeSessionAction
    data object DiscardCurrentAnswer: PracticeSessionAction
    data object ToggleQuestionCard: PracticeSessionAction

    // Body language actions
    data class ToggleCameraPreview(val visible: Boolean) : PracticeSessionAction
    data class OnFrame(val imageProxy: androidx.camera.core.ImageProxy) : PracticeSessionAction
}