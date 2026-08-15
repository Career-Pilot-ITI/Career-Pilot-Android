package com.iti.careerpilot.home.presentation.home

sealed interface HomeEvent {
    data class NavigateToReadyToPractice(val trackId: Long, val trackName: String) : HomeEvent
    data class NavigateToQuiz(val trackId: Long, val trackName: String) : HomeEvent
    data class NavigateToSessionDetails(val sessionId: Long) : HomeEvent
    data class NavigateToPracticeSession(val trackId: Long, val sessionId: Long) : HomeEvent
    data object NavigateToInterviews : HomeEvent
    data class NavigateToPlansPaywall(val showMySubscription: Boolean = false) : HomeEvent
    data object NavigateToCoinsPaywall : HomeEvent
    data object NavigateToReports : HomeEvent
}
