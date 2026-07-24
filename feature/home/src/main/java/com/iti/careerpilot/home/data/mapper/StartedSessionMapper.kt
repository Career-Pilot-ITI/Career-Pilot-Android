package com.iti.careerpilot.home.data.mapper

import com.iti.careerpilot.home.data.datasource.remote.dto.StartSessionResponseDto
import com.iti.careerpilot.home.domain.model.StartedSession

fun StartSessionResponseDto.toDomain(): StartedSession? {
    val id = sessionId ?: return null

    return StartedSession(
        sessionId = id,
        trackName = trackName.orEmpty(),
        targetDurationMinutes = targetDurationMinutes,
        maxQuestions = maxQuestions,
    )
}
