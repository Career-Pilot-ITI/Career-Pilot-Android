package com.iti.careerpilot.home.domain.model

data class StartedSession(
    val sessionId: Long,
    val trackName: String,
    val targetDurationMinutes: Int?,
    val maxQuestions: Int?,
)
