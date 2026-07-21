package com.iti.careerpilot.reports.data

import com.iti.careerpilot.reports.data.datasource.remote.ReportsRemoteDataSource
import com.iti.careerpilot.reports.data.datasource.remote.dto.FeedbackReportDto
import com.iti.careerpilot.reports.data.datasource.remote.dto.InterviewSessionDto
import com.iti.careerpilot.reports.data.datasource.remote.dto.QuestionScoreDto
import com.iti.careerpilot.reports.data.datasource.remote.dto.SessionQuestionDto
import com.iti.careerpilot.reports.data.repository.ReportsRepositoryImpl
import com.iti.careerpilot.reports.domain.model.CoachingImpact
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportsRepositoryImplTest {
    @Test
    fun `history maps completed backend sessions and filters incomplete sessions`() = runTest {
        val remote = FakeReportsRemoteDataSource(
            sessionsResult = CareerPilotResult.Success(
                listOf(
                    completedSession,
                    completedSession.copy(id = 2L, status = "IN_PROGRESS"),
                ),
            ),
        )
        val repository = repository(remote)

        val result = repository.getSessionHistory()

        assertTrue(result is CareerPilotResult.Success)
        val sessions = (result as CareerPilotResult.Success).data
        assertEquals(1, sessions.size)
        assertEquals("1", sessions.single().id)
        assertEquals(2, sessions.single().durationMinutes)
    }

    @Test
    fun `details maps backend metrics and distributes suggestion impacts by order`() = runTest {
        val remote = FakeReportsRemoteDataSource(
            sessionResult = CareerPilotResult.Success(completedSession),
            feedbackResult = CareerPilotResult.Success(feedback),
        )
        val repository = repository(remote)

        val result = repository.getReportDetails("1")

        assertTrue(result is CareerPilotResult.Success)
        val details = (result as CareerPilotResult.Success).data
        assertEquals(82, details.overallScore)
        assertEquals(null, details.topPercent)
        assertEquals(
            listOf(CoachingImpact.HIGH, CoachingImpact.MEDIUM, CoachingImpact.LOW),
            details.coachingSuggestions.map { it.impact },
        )
    }

    @Test
    fun `question mapping derives filler word count and distinct chips from transcript`() = runTest {
        val remote = FakeReportsRemoteDataSource(
            questionsResult = CareerPilotResult.Success(listOf(question)),
        )
        val repository = repository(remote)

        val result = repository.getQuestionBreakdown("1")

        assertTrue(result is CareerPilotResult.Success)
        val mapped = (result as CareerPilotResult.Success).data.questions.single()
        assertEquals(4, mapped.fillerWordCount)
        assertEquals(listOf("uh", "um", "you know"), mapped.fillerWords)
        assertEquals(88, mapped.score)
    }

    @Test
    fun `invalid route session id is rejected without a network request`() = runTest {
        val remote = FakeReportsRemoteDataSource()
        val repository = repository(remote)

        val result = repository.getReportDetails("not-a-number")

        assertEquals(CareerPilotResult.Error(NetworkError.BAD_REQUEST), result)
        assertEquals(0, remote.sessionCalls)
    }

    @Test(expected = CancellationException::class)
    fun `repository rethrows cancellation`() = runTest {
        val repository = repository(CancellingReportsRemoteDataSource)
        repository.getSessionHistory()
    }

    private fun TestScope.repository(remote: ReportsRemoteDataSource) = ReportsRepositoryImpl(
        remoteDataSource = remote,
        ioDispatcher = StandardTestDispatcher(testScheduler),
    )
}

private class FakeReportsRemoteDataSource(
    private val sessionsResult: CareerPilotResult<List<InterviewSessionDto>, NetworkError> =
        CareerPilotResult.Success(emptyList()),
    private val sessionResult: CareerPilotResult<InterviewSessionDto, NetworkError> =
        CareerPilotResult.Success(completedSession),
    private val feedbackResult: CareerPilotResult<FeedbackReportDto, NetworkError> =
        CareerPilotResult.Success(feedback),
    private val questionsResult: CareerPilotResult<List<SessionQuestionDto>, NetworkError> =
        CareerPilotResult.Success(emptyList()),
) : ReportsRemoteDataSource {
    var sessionCalls = 0

    override suspend fun getSessions() = sessionsResult

    override suspend fun getSession(sessionId: Long) = sessionResult.also { sessionCalls++ }

    override suspend fun getFeedback(sessionId: Long) = feedbackResult

    override suspend fun getQuestions(sessionId: Long) = questionsResult
}

private object CancellingReportsRemoteDataSource : ReportsRemoteDataSource {
    override suspend fun getSessions(): CareerPilotResult<List<InterviewSessionDto>, NetworkError> =
        throw CancellationException()

    override suspend fun getSession(
        sessionId: Long,
    ): CareerPilotResult<InterviewSessionDto, NetworkError> = throw CancellationException()

    override suspend fun getFeedback(
        sessionId: Long,
    ): CareerPilotResult<FeedbackReportDto, NetworkError> = throw CancellationException()

    override suspend fun getQuestions(
        sessionId: Long,
    ): CareerPilotResult<List<SessionQuestionDto>, NetworkError> = throw CancellationException()
}

private val completedSession = InterviewSessionDto(
    id = 1L,
    trackId = 4L,
    trackName = "Software Engineering",
    status = "COMPLETED",
    overallScore = 82,
    durationSeconds = 102,
    targetDurationMinutes = 20,
    maxQuestions = 3,
    startedAt = "2026-07-19T11:12:18",
    completedAt = "2026-07-19T11:14:00",
    createdAt = "2026-07-19T11:12:18",
)

private val feedback = FeedbackReportDto(
    id = 7L,
    sessionId = 1L,
    overallScore = 82,
    clarityScore = 78,
    confidenceScore = 85,
    pacingScore = 72,
    fillerWordsScore = 65,
    contentRelevanceScore = 90,
    coachingTips = listOf("Tip one", "Tip two", "Tip three"),
    generatedAt = "2026-07-19T11:14:01",
    createdAt = "2026-07-19T11:14:01",
)

private val question = SessionQuestionDto(
    id = 11L,
    sessionId = 1L,
    questionText = "Tell me about yourself.",
    questionOrder = 1,
    userTranscript = "Uh, um, you know, uh, I build Android applications.",
    durationMs = 102_000L,
    createdAt = "2026-07-19T11:12:18",
    completedAt = "2026-07-19T11:14:00",
    score = QuestionScoreDto(
        id = 21L,
        sessionQuestionId = 11L,
        contentRelevance = 90,
        clarity = 86,
        confidence = 85,
        pacing = 82,
        fillerWords = 65,
        overallScore = 88,
        coachingTip = "Lead with impact earlier.",
        createdAt = "2026-07-19T11:14:00",
    ),
)
