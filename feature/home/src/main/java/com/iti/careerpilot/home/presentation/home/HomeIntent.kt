package com.iti.careerpilot.home.presentation.home

sealed interface HomeIntent {
    data object Initial : HomeIntent
    data object Refresh : HomeIntent
    data object PracticeInterviewClicked : HomeIntent
    data object LessonClicked : HomeIntent
    data object UpgradeClicked : HomeIntent
    data object CoinsClicked : HomeIntent
    data object ScoreCardClicked : HomeIntent
    data object AtsJobMatchClicked : HomeIntent
    data object SeeAllSessionsClicked : HomeIntent
    data object SeeAllInterviewsClicked : HomeIntent
    data class InterviewTrackClicked(val trackId: Long, val trackName: String) : HomeIntent
    data class SessionClicked(val sessionId: Long) : HomeIntent
    data class ResumeSessionClicked(val sessionId: Long) : HomeIntent
}
