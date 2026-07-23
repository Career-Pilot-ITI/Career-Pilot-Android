package com.iti.careerpilot.practicesession.presentation.action

sealed interface PracticeSessionAction {

    data class CreateNewPracticeSession(
        val trackId: Long,
    ) : PracticeSessionAction

    data class RestartPracticeSession(
        val sessionId: Long,
    ) : PracticeSessionAction

    data class ShowOrHidePermissionDialog(
        val show: Boolean
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
    data object SkipCurrentQuestion: PracticeSessionAction
    data object ToggleQuestionCard: PracticeSessionAction
}