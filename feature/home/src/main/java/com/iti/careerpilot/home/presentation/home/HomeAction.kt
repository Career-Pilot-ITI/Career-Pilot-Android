package com.iti.careerpilot.home.presentation.home

sealed interface HomeAction {
    data object Refresh : HomeAction
    data object PracticeInterviewClicked : HomeAction
    data object UpgradeClicked : HomeAction
    data object ScoreCardClicked : HomeAction
    data object SeeAllSessionsClicked : HomeAction
    data object SeeAllInterviewsClicked : HomeAction
    data class InterviewTrackClicked(val trackId: Long, val trackName: String) : HomeAction
    data class SessionClicked(val sessionId: Long) : HomeAction
}