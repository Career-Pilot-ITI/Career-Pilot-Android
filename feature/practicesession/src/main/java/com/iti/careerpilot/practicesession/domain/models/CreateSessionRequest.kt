package com.iti.careerpilot.practicesession.domain.models

data class CreateSessionRequest(
    val trackId: Long,
    val questionCount: Int,
    val durationMinutes: Int,
    val workspaceId: Long? = null,
    val challengeId: String? = null
)
