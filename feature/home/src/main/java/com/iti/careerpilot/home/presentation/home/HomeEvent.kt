package com.iti.careerpilot.home.presentation.home

sealed interface HomeEvent {
    data class NavigateToReadyToPractice(val trackId: Long, val trackName: String) : HomeEvent
    data class NavigateToSessionDetails(val sessionId: Long) : HomeEvent
    data class NavigateToPracticeSession(val trackId: Long, val sessionId: Long) : HomeEvent
    data object NavigateToInterviews : HomeEvent
    data object NavigateToPaywall : HomeEvent
    data object NavigateToReports : HomeEvent
}
