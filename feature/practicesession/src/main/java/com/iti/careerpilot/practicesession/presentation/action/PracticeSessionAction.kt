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
    data object FinishRecordingAnswerAndStartTranscription: PracticeSessionAction

    data object PlayCurrentRecordedAnswer: PracticeSessionAction
    data class SeekAudioTo(val positionMs: Long): PracticeSessionAction

    data object SubmitFinalAnswerToCurrentQuestion: PracticeSessionAction
    data object DiscardCurrentAnswerAndMakeNewOne: PracticeSessionAction
    data object SkipCurrentQuestion: PracticeSessionAction
    data object ToggleQuestionCard: PracticeSessionAction
}