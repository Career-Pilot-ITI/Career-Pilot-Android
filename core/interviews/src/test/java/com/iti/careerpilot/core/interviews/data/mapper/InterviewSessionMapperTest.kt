package com.iti.careerpilot.core.interviews.data.mapper

import com.iti.careerpilot.core.interviews.data.remote.dto.InterviewSessionDto
import com.iti.careerpilot.core.interviews.data.remote.dto.InterviewSessionPageDto
import com.iti.careerpilot.core.interviews.domain.model.SessionStatus
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InterviewSessionMapperTest {

    @Test
    fun `zone-less timestamp is read as UTC`() {
        val session = sessionDto(completedAt = "2026-08-11T09:00:00").toDomain()

        assertEquals(Instant.parse("2026-08-11T09:00:00Z"), session?.occurredAt)
    }

    @Test
    fun `offset bearing timestamp keeps its offset`() {
        val session = sessionDto(completedAt = "2026-08-11T09:00:00Z").toDomain()

        assertEquals(Instant.parse("2026-08-11T09:00:00Z"), session?.occurredAt)
    }

    @Test
    fun `occurredAt falls back from completedAt to startedAt to createdAt`() {
        val fromStartedAt = sessionDto(
            completedAt = null,
            startedAt = "2026-08-10T08:00:00",
            createdAt = "2026-08-09T07:00:00",
        ).toDomain()
        val fromCreatedAt = sessionDto(
            completedAt = null,
            startedAt = null,
            createdAt = "2026-08-09T07:00:00",
        ).toDomain()

        assertEquals(Instant.parse("2026-08-10T08:00:00Z"), fromStartedAt?.occurredAt)
        assertEquals(Instant.parse("2026-08-09T07:00:00Z"), fromCreatedAt?.occurredAt)
    }

    @Test
    fun `unparseable timestamps leave occurredAt null without dropping the session`() {
        val session = sessionDto(
            completedAt = "not-a-date",
            startedAt = "",
            createdAt = null,
        ).toDomain()

        assertNull(session?.occurredAt)
        assertEquals(1L, session?.id)
    }

    @Test
    fun `duration rounds up to whole minutes`() {
        assertEquals(2, sessionDto(durationSeconds = 61).toDomain()?.durationMinutes)
        assertEquals(1, sessionDto(durationSeconds = 60).toDomain()?.durationMinutes)
        assertEquals(1, sessionDto(durationSeconds = 1).toDomain()?.durationMinutes)
        assertEquals(0, sessionDto(durationSeconds = 0).toDomain()?.durationMinutes)
        assertEquals(0, sessionDto(durationSeconds = -30).toDomain()?.durationMinutes)
        assertEquals(0, sessionDto(durationSeconds = null).toDomain()?.durationMinutes)
    }

    @Test
    fun `scores are coerced into range and null stays null`() {
        assertEquals(100, sessionDto(overallScore = 150).toDomain()?.score)
        assertEquals(0, sessionDto(overallScore = -5).toDomain()?.score)
        assertEquals(72, sessionDto(overallScore = 72).toDomain()?.score)
        assertNull(sessionDto(overallScore = null).toDomain()?.score)
    }

    @Test
    fun `status parsing is case insensitive and falls back to unknown`() {
        assertEquals(SessionStatus.COMPLETED, sessionDto(status = "completed").toDomain()?.status)
        assertEquals(SessionStatus.IN_PROGRESS, sessionDto(status = "IN_PROGRESS").toDomain()?.status)
        assertEquals(SessionStatus.UNKNOWN, sessionDto(status = "SOMETHING_ELSE").toDomain()?.status)
        assertEquals(SessionStatus.UNKNOWN, sessionDto(status = null).toDomain()?.status)
    }

    @Test
    fun `resumable and completed reflect the status`() {
        val inProgress = sessionDto(status = "IN_PROGRESS").toDomain()
        val completed = sessionDto(status = "COMPLETED").toDomain()
        val abandoned = sessionDto(status = "ABANDONED").toDomain()

        assertTrue(inProgress?.isResumable == true)
        assertTrue(completed?.isCompleted == true)
        assertTrue(abandoned?.isResumable == false && abandoned.isCompleted == false)
    }

    @Test
    fun `id falls back to sessionId and a session without any id is dropped`() {
        val fromSessionId = sessionDto(id = null, sessionId = 42L).toDomain()

        assertEquals(42L, fromSessionId?.id)
        assertNull(sessionDto(id = null, sessionId = null).toDomain())
    }

    @Test
    fun `page mapping skips unmappable rows and coerces negative metadata`() {
        val page = InterviewSessionPageDto(
            content = listOf(
                sessionDto(id = 1L),
                sessionDto(id = null, sessionId = null),
                sessionDto(id = 3L),
            ),
            number = -1,
            totalPages = -4,
            totalElements = -7L,
        ).toDomain()

        assertEquals(listOf(1L, 3L), page.sessions.map { it.id })
        assertEquals(0, page.pageNumber)
        assertEquals(0, page.totalPages)
        assertEquals(0L, page.totalElements)
    }

    private fun sessionDto(
        id: Long? = 1L,
        sessionId: Long? = null,
        status: String? = "COMPLETED",
        overallScore: Int? = 80,
        durationSeconds: Int? = 600,
        maxQuestions: Int? = 3,
        startedAt: String? = "2026-08-11T08:40:00",
        completedAt: String? = "2026-08-11T09:00:00",
        createdAt: String? = "2026-08-11T08:40:00",
    ) = InterviewSessionDto(
        id = id,
        sessionId = sessionId,
        trackId = 1L,
        trackName = "Android Development",
        status = status,
        overallScore = overallScore,
        durationSeconds = durationSeconds,
        targetDurationMinutes = 20,
        maxQuestions = maxQuestions,
        startedAt = startedAt,
        completedAt = completedAt,
        createdAt = createdAt,
    )
}
