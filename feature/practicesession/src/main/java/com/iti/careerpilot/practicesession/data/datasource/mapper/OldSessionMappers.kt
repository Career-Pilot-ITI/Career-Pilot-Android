package com.iti.careerpilot.practicesession.data.datasource.mapper

import com.iti.careerpilot.practicesession.data.datasource.models.OldSessionDto
import com.iti.careerpilot.practicesession.domain.models.Session

fun OldSessionDto.toDomain(): Session = Session(
    sessionId = sessionId ?: 0,
    status = status.orEmpty(),
    trackName = trackName.orEmpty(),
    targetDurationMinutes = 0, // Not present in OldSessionDto
    maxQuestions = totalCount ?: 0,
    answeredCount = answeredCount ?: 0,
    startedAt = startedAt.orEmpty(),
    updatedAt = updatedAt.orEmpty(),
    currentQuestion = currentQuestion?.toDomain(),
    answeredQuestions = answeredQuestions?.map { it.toDomain() } ?: emptyList()
)
