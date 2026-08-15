package com.iti.careerpilot.home.presentation.home

sealed interface HomeAction {
    data object Initial : HomeAction
    data object Refresh : HomeAction
    data object PracticeInterviewClicked : HomeAction
    data object LessonClicked : HomeAction
    data object UpgradeClicked : HomeAction
    data object CoinsClicked : HomeAction
    data object ScoreCardClicked : HomeAction
    data object AtsJobMatchClicked : HomeAction
    data object SeeAllSessionsClicked : HomeAction
    data object SeeAllInterviewsClicked : HomeAction
    data class InterviewTrackClicked(val trackId: Long, val trackName: String) : HomeAction
    data class SessionClicked(val sessionId: Long) : HomeAction
    data class ResumeSessionClicked(val sessionId: Long) : HomeAction
}
