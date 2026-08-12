package com.iti.careerpilot.quiz.presentation.event

import com.iti.common.util.UIText

sealed interface QuizEvent {
    data class ShowError(val message: UIText) : QuizEvent
    data object QuizCompleted : QuizEvent
}
