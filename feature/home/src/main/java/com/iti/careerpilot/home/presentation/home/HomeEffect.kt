package com.iti.careerpilot.home.presentation.home

sealed interface HomeEffect {
    data class NavigateToReadyToPractice(val trackId: Long, val trackName: String) : HomeEffect
    data class NavigateToQuiz(val trackId: Long, val trackName: String) : HomeEffect
    data class NavigateToSessionDetails(val sessionId: Long) : HomeEffect
    data class NavigateToPracticeSession(val trackId: Long, val sessionId: Long) : HomeEffect
    data object NavigateToInterviews : HomeEffect
    data class NavigateToPlansPaywall(val showMySubscription: Boolean = false) : HomeEffect
    data object NavigateToCoinsPaywall : HomeEffect
    data object NavigateToReports : HomeEffect
    data object NavigateToAts : HomeEffect
}
