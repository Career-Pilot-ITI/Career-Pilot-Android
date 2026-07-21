package com.iti.careerpilot.practicesession.domain.models

data class CreateSessionRequest(
    val trackId: Int,
    val questionCount: Int,
    val durationMinutes: Int
)
