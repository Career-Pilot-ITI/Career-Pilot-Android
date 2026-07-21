package com.iti.careerpilot.practicesession.data.datasource.mapper

import com.iti.careerpilot.practicesession.data.datasource.models.CreateSessionRequestDto
import com.iti.careerpilot.practicesession.data.datasource.models.CurrentQuestionDto
import com.iti.careerpilot.practicesession.data.datasource.models.SessionDto
import com.iti.careerpilot.practicesession.domain.models.CreateSessionRequest
import com.iti.careerpilot.practicesession.domain.models.CurrentQuestion
import com.iti.careerpilot.practicesession.domain.models.Session


fun SessionDto.toDomain(): Session = Session(
    sessionId = sessionId ?: 0,
    status = "",
    trackName = trackName.orEmpty(),
    targetDurationMinutes = targetDurationMinutes ?: 0,
    maxQuestions = maxQuestions ?: 0,
    answeredCount = 0,
    startedAt = startedAt.orEmpty(),
    updatedAt = "",
    currentQuestion = firstQuestion?.toDomain(),
    answeredQuestions = emptyList()
)

fun CurrentQuestionDto.toDomain(): CurrentQuestion = CurrentQuestion(
    id = id ?: 0,
    sessionId = sessionId ?: 0,
    questionText = questionText.orEmpty(),
    questionOrder = questionOrder ?: 0,
    createdAt = createdAt.orEmpty()
)

fun CreateSessionRequest.toDto(): CreateSessionRequestDto = CreateSessionRequestDto(
    trackId = trackId,
    questionCount = questionCount,
    durationMinutes = durationMinutes
)