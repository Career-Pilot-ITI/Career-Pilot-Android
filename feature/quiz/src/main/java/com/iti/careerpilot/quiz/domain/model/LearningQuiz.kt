package com.iti.careerpilot.quiz.domain.model

data class LearningQuiz(
    val questions: List<QuizQuestion>
)

data class QuizQuestion(
    val id: String,
    val question: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String
)
