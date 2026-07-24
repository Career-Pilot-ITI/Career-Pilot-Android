package com.iti.careerpilot.home.data.mapper

import com.iti.careerpilot.home.data.datasource.remote.dto.InterviewSessionDto
import com.iti.careerpilot.home.domain.model.InterviewSession
import com.iti.careerpilot.home.domain.model.SessionStatus
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.math.roundToInt

fun InterviewSessionDto.toDomain(): InterviewSession? {
    val sessionId = id ?: return null

    return InterviewSession(
        id = sessionId,
        trackName = trackName.orEmpty(),
        overallScore = overallScore,
        durationMinutes = durationSeconds.secondsToWholeMinutes(),
        occurredAt = completedAt.toInstantOrNull()
            ?: startedAt.toInstantOrNull()
            ?: createdAt.toInstantOrNull(),
        status = SessionStatus.fromApi(status),
    )
}

fun List<InterviewSessionDto>.toDomain(): List<InterviewSession> = mapNotNull { it.toDomain() }

private fun String?.toInstantOrNull(): Instant? {
    if (this.isNullOrBlank()) return null
    return runCatching { LocalDateTime.parse(this).toInstant(ZoneOffset.UTC) }.getOrNull()
}

private fun Int?.secondsToWholeMinutes(): Int {
    val seconds = this ?: return 0
    if (seconds <= 0) return 0
    return (seconds / 60f).roundToInt().coerceAtLeast(1)
}
