package com.iti.careerpilot.practicesession.domain.models


data class Session(
    val sessionId: Int,
    val trackName: String,
    val targetDurationMinutes: Int,
    val maxQuestions: Int,
    val startedAt: String,
    val firstQuestion: Question?
)

data class Question(
    val id: Int,
    val sessionId: Int,
    val questionText: String,
    val questionOrder: Int,
    val createdAt: String
)
