package com.iti.careerpilot.home.data.mapper

import com.iti.careerpilot.home.data.datasource.remote.dto.InterviewSessionDto
import com.iti.careerpilot.home.data.datasource.remote.dto.StartSessionResponseDto
import com.iti.careerpilot.home.data.datasource.remote.dto.TrackDto
import com.iti.careerpilot.home.domain.model.InterviewSession
import com.iti.careerpilot.home.domain.model.InterviewTrack
import com.iti.careerpilot.home.domain.model.SessionStatus
import com.iti.careerpilot.home.domain.model.StartedSession
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.math.roundToInt

private fun String?.toInstantOrNull(): Instant? {
    if (this.isNullOrBlank()) return null
    return runCatching { LocalDateTime.parse(this).toInstant(ZoneOffset.UTC) }.getOrNull()
}

private fun Int?.secondsToWholeMinutes(): Int {
    val seconds = this ?: return 0
    if (seconds <= 0) return 0
    return (seconds / 60f).roundToInt().coerceAtLeast(1)
}

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

fun TrackDto.toDomain(): InterviewTrack? {
    val trackId = id ?: return null
    val trackName = name?.takeIf { it.isNotBlank() } ?: return null

    return InterviewTrack(
        id = trackId,
        name = trackName,
        description = description.orEmpty(),
    )
}

fun List<TrackDto>.toDomain(): List<InterviewTrack> = mapNotNull { it.toDomain() }

fun StartSessionResponseDto.toDomain(): StartedSession? {
    val id = sessionId ?: return null

    return StartedSession(
        sessionId = id,
        trackName = trackName.orEmpty(),
        targetDurationMinutes = targetDurationMinutes,
        maxQuestions = maxQuestions,
    )
}
