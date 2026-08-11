package com.iti.careerpilot.home.presentation.interviews

sealed interface InterviewsEvent {
    data class NavigateToReadyToPractice(val trackId: Long, val trackName: String) : InterviewsEvent
    data class NavigateToQuiz(val trackId: Long, val trackName: String) : InterviewsEvent
}
