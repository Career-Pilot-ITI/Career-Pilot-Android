package com.iti.careerpilot.practicesession.data.datasource.mapper

import com.iti.careerpilot.practicesession.data.datasource.models.SessionQuestionResultDto
import com.iti.careerpilot.practicesession.data.datasource.models.SessionResultDto
import com.iti.careerpilot.practicesession.domain.models.SessionQuestionResult
import com.iti.careerpilot.practicesession.domain.models.SessionResult

fun SessionResultDto.toDomain(): SessionResult = SessionResult(
    id = id ?: 0,
    sessionId = sessionId ?: 0,
    overallScore = overallScore ?: 0,
    clarityScore = clarityScore ?: 0,
    confidenceScore = confidenceScore ?: 0,
    pacingScore = pacingScore ?: 0,
    fillerWordsScore = fillerWordsScore ?: 0,
    contentRelevanceScore = contentRelevanceScore ?: 0,
    coachingTips = coachingTips ?: emptyList(),
    generatedAt = generatedAt.orEmpty(),
    createdAt = createdAt.orEmpty(),
    questions = questions?.map { it.toDomain() } ?: emptyList()
)

fun SessionQuestionResultDto.toDomain(): SessionQuestionResult = SessionQuestionResult(
    id = id ?: 0,
    sessionId = sessionId ?: 0,
    questionText = questionText.orEmpty(),
    questionOrder = questionOrder ?: 0,
    userTranscript = userTranscript.orEmpty(),
    durationMs = durationMs ?: 0,
    speechRateWpm = speechRateWpm ?: 0.0,
    avgPauseMs = avgPauseMs ?: 0,
    silenceRatio = silenceRatio ?: 0.0,
    createdAt = createdAt.orEmpty(),
    completedAt = completedAt.orEmpty(),
    score = score?.toDomain()
)
