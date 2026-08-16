package com.iti.careerpilot.home.presentation.interviews

sealed interface InterviewsEffect {
    data class NavigateToReadyToPractice(val trackId: Long, val trackName: String) : InterviewsEffect
    data class NavigateToQuiz(val trackId: Long, val trackName: String) : InterviewsEffect
}
