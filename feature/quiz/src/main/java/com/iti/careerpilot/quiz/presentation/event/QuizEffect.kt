package com.iti.careerpilot.quiz.presentation.event


sealed interface QuizEffect {
    data object QuizCompleted : QuizEffect
    data class NavigateToPaywall(val showGetCoins: Boolean = false) : QuizEffect
}
