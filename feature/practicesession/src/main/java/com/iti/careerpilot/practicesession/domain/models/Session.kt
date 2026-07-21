package com.iti.careerpilot.practicesession.domain.models


data class Session(
    val sessionId: Long,
    val status: String,
    val trackName: String,
    val targetDurationMinutes: Int,
    val maxQuestions: Int,
    val answeredCount: Int,
    val startedAt: String,
    val updatedAt: String,
    val currentQuestion: CurrentQuestion?,
    val answeredQuestions: List<SessionQuestionResult>
)

data class CurrentQuestion(
    val id: Long,
    val sessionId: Long,
    val questionText: String,
    val questionOrder: Int,
    val createdAt: String
)
