package com.iti.careerpilot.practicesession.data.datasource.mapper

import com.iti.careerpilot.practicesession.data.datasource.models.AnswerRequestDto
import com.iti.careerpilot.practicesession.data.datasource.models.AnswerResponseDto
import com.iti.careerpilot.practicesession.data.datasource.models.ScoreDto
import com.iti.careerpilot.practicesession.data.datasource.models.WordDto
import com.iti.careerpilot.practicesession.domain.models.AnswerRequest
import com.iti.careerpilot.practicesession.domain.models.AnswerResponse
import com.iti.careerpilot.practicesession.domain.models.Score
import com.iti.careerpilot.practicesession.domain.models.Word

fun AnswerRequest.toDto(): AnswerRequestDto = AnswerRequestDto(
    transcript = transcript,
    sessionElapsedSeconds = sessionElapsedSeconds,
    durationMs = durationMs,
    audioUrl = audioUrl,
    words = words.map { it.toDto() }
)

fun Word.toDto(): WordDto = WordDto(
    word = word,
    startMs = startMs,
    endMs = endMs
)

fun AnswerResponseDto.toDomain(): AnswerResponse = AnswerResponse(
    sessionStatus = sessionStatus.orEmpty(),
    score = score?.toDomain(),
    nextQuestion = nextQuestion?.toDomain()
)

fun ScoreDto.toDomain(): Score = Score(
    id = id ?: 0,
    sessionQuestionId = sessionQuestionId ?: 0,
    contentRelevance = contentRelevance ?: 0,
    clarity = clarity ?: 0,
    confidence = confidence ?: 0,
    pacing = pacing ?: 0,
    fillerWords = fillerWords ?: 0,
    overallScore = overallScore ?: 0,
    coachingTip = coachingTip.orEmpty(),
    createdAt = createdAt.orEmpty()
)
