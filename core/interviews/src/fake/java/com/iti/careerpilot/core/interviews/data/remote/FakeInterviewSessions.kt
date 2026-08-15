package com.iti.careerpilot.core.interviews.data.remote

import com.iti.careerpilot.core.interviews.data.remote.dto.InterviewSessionDto
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

object FakeInterviewSessions {

    val sessions: List<InterviewSessionDto> = buildList {
        add(
            InterviewSessionDto(
                id = 101L,
                sessionId = 101L,
                trackId = 1L,
                trackName = TRACKS[0],
                status = "IN_PROGRESS",
                overallScore = null,
                durationSeconds = 260,
                targetDurationMinutes = 20,
                maxQuestions = 3,
                startedAt = timestampAt(0),
                completedAt = null,
                createdAt = timestampAt(0),
            ),
        )
        repeat(COMPLETED_SESSION_COUNT) { index ->
            val startedAt = timestampAt(index + 1)
            add(
                InterviewSessionDto(
                    id = 102L + index,
                    sessionId = 102L + index,
                    trackId = (index % TRACKS.size + 1).toLong(),
                    trackName = TRACKS[index % TRACKS.size],
                    status = "COMPLETED",
                    overallScore = SCORES[index % SCORES.size],
                    durationSeconds = 900 + index * 30,
                    targetDurationMinutes = 20,
                    maxQuestions = 3,
                    startedAt = startedAt,
                    completedAt = timestampAt(index + 1, minutesLater = 17),
                    createdAt = startedAt,
                ),
            )
        }
    }

    fun findById(sessionId: Long): InterviewSessionDto? =
        sessions.firstOrNull { it.id == sessionId }
}

private fun timestampAt(daysAgo: Int, minutesLater: Long = 0L): String =
    BASE_TIMESTAMP
        .minus(daysAgo.toLong(), ChronoUnit.DAYS)
        .plusMinutes(minutesLater)
        .toString()

private val BASE_TIMESTAMP: LocalDateTime = LocalDateTime.of(2026, 8, 13, 9, 0)
private val TRACKS = listOf(
    "Android Development",
    "Backend Development",
    "UI/UX Design",
)
private val SCORES = listOf(87, 76, 64, 91, 58, 82, 70)
private const val COMPLETED_SESSION_COUNT = 14
