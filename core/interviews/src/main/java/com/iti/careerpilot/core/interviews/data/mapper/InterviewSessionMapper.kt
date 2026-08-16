package com.iti.careerpilot.core.interviews.data.mapper

import com.iti.careerpilot.core.interviews.data.remote.dto.InterviewSessionDto
import com.iti.careerpilot.core.interviews.data.remote.dto.InterviewSessionPageDto
import com.iti.careerpilot.core.interviews.domain.model.InterviewSession
import com.iti.careerpilot.core.interviews.domain.model.InterviewSessionPage
import com.iti.careerpilot.core.interviews.domain.model.SessionStatus
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

fun InterviewSessionDto.toDomain(): InterviewSession? {
    val sessionId = id ?: sessionId ?: return null

    return InterviewSession(
        id = sessionId,
        trackId = trackId,
        trackName = trackName.orEmpty(),
        status = SessionStatus.fromApi(status),
        score = overallScore?.coerceIn(MIN_SCORE, MAX_SCORE),
        occurredAt = completedAt.toInstantOrNull()
            ?: startedAt.toInstantOrNull()
            ?: createdAt.toInstantOrNull(),
        durationMinutes = durationSeconds.secondsToWholeMinutes(),
        questionCount = maxQuestions?.coerceAtLeast(0) ?: 0,
    )
}

fun InterviewSessionPageDto.toDomain(): InterviewSessionPage = InterviewSessionPage(
    sessions = content.mapNotNull { it.toDomain() },
    pageNumber = number.coerceAtLeast(0),
    totalPages = totalPages.coerceAtLeast(0),
    totalElements = totalElements.coerceAtLeast(0L),
    isFirst = first,
    isLast = last,
)

private fun String?.toInstantOrNull(): Instant? {
    if (isNullOrBlank()) return null
    return runCatching { Instant.parse(this) }
        .recoverCatching { LocalDateTime.parse(this).toInstant(ZoneOffset.UTC) }
        .getOrNull()
}

private fun Int?.secondsToWholeMinutes(): Int {
    val seconds = (this ?: 0).coerceAtLeast(0)
    return (seconds + SECONDS_PER_MINUTE - 1) / SECONDS_PER_MINUTE
}

private const val SECONDS_PER_MINUTE = 60
private const val MIN_SCORE = 0
private const val MAX_SCORE = 100
