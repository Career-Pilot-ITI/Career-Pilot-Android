package com.iti.careerpilot.quiz.presentation.event


sealed interface QuizEvent {
    data object QuizCompleted : QuizEvent
    data class NavigateToPaywall(val showGetCoins: Boolean = false) : QuizEvent
}
