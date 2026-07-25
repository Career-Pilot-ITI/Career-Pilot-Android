package com.iti.careerpilot.reports.data.datasource.remote

import com.iti.careerpilot.reports.data.datasource.remote.dto.FeedbackReportDto
import com.iti.careerpilot.reports.data.datasource.remote.dto.InterviewSessionDto
import com.iti.careerpilot.reports.data.datasource.remote.dto.QuestionScoreDto
import com.iti.careerpilot.reports.data.datasource.remote.dto.SessionQuestionDto
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import com.iti.common.util.fakeDelay
import javax.inject.Inject

class FakeReportsRemoteDataSource @Inject constructor() : ReportsRemoteDataSource {
    override suspend fun getSessions(): CareerPilotResult<List<InterviewSessionDto>, NetworkError> {
        fakeDelay()
        return CareerPilotResult.Success(fakeSessions)
    }

    override suspend fun getSession(
        sessionId: Long,
    ): CareerPilotResult<InterviewSessionDto, NetworkError> {
        fakeDelay()
        return fakeSessions.find { it.id == sessionId }.toResult()
    }

    override suspend fun getFeedback(
        sessionId: Long,
    ): CareerPilotResult<FeedbackReportDto, NetworkError> {
        fakeDelay()
        val session = fakeSessions.find { it.id == sessionId }
            ?: return CareerPilotResult.Error(NetworkError.BAD_REQUEST)
        return CareerPilotResult.Success(session.toFeedback())
    }

    override suspend fun getQuestions(
        sessionId: Long,
    ): CareerPilotResult<List<SessionQuestionDto>, NetworkError> {
        fakeDelay()
        val session = fakeSessions.find { it.id == sessionId }
            ?: return CareerPilotResult.Error(NetworkError.BAD_REQUEST)
        return CareerPilotResult.Success(session.toQuestions())
    }
}

private fun InterviewSessionDto?.toResult():
    CareerPilotResult<InterviewSessionDto, NetworkError> = this?.let {
    CareerPilotResult.Success(it)
} ?: CareerPilotResult.Error(NetworkError.BAD_REQUEST)

private fun InterviewSessionDto.toFeedback(): FeedbackReportDto {
    val score = overallScore ?: 0
    return FeedbackReportDto(
        id = id + FEEDBACK_ID_OFFSET,
        sessionId = id,
        overallScore = score,
        clarityScore = (score + 2).coerceAtMost(MAX_SCORE),
        confidenceScore = (score - 3).coerceAtLeast(MIN_SCORE),
        pacingScore = (score - 6).coerceAtLeast(MIN_SCORE),
        fillerWordsScore = (score - 9).coerceAtLeast(MIN_SCORE),
        contentRelevanceScore = (score + 5).coerceAtMost(MAX_SCORE),
        coachingTips = listOf(
            "Open with the result before explaining the implementation details.",
            "Use a short pause between ideas instead of filler words.",
            "Support each answer with one measurable example from your experience.",
        ),
        generatedAt = completedAt,
        createdAt = completedAt ?: "Today",
    )
}

private fun InterviewSessionDto.toQuestions(): List<SessionQuestionDto> {
    val score = overallScore ?: 0
    return listOf(
        createQuestion(
            order = 1,
            question = "Tell me about a challenging project and how you delivered it.",
            transcript = "Um, I led an Android project with a tight deadline. " +
                "I split the work into milestones, tracked risks, and delivered the core flow early.",
            score = (score + 3).coerceAtMost(MAX_SCORE),
            coachingTip = "Lead with the project outcome, then explain the actions you took.",
            durationMs = 86_000L,
        ),
        createQuestion(
            order = 2,
            question = "How do you investigate and resolve a production issue?",
            transcript = "I first reproduce the issue and inspect logs. Basically, I narrow down " +
                "the failing layer, add a regression test, and verify the fix before release.",
            score = score,
            coachingTip = "Mention how you communicate impact and progress to the team.",
            durationMs = 74_000L,
        ),
        createQuestion(
            order = 3,
            question = "Describe a time you disagreed with a technical decision.",
            transcript = "I gathered data for both options, you know, and discussed the tradeoffs " +
                "with the team. We agreed on a small experiment before committing.",
            score = (score - 5).coerceAtLeast(MIN_SCORE),
            coachingTip = "Clarify the final result and what you learned from the disagreement.",
            durationMs = 68_000L,
        ),
    )
}

private fun InterviewSessionDto.createQuestion(
    order: Int,
    question: String,
    transcript: String,
    score: Int,
    coachingTip: String,
    durationMs: Long,
): SessionQuestionDto {
    val questionId = id * QUESTION_ID_MULTIPLIER + order
    return SessionQuestionDto(
        id = questionId,
        sessionId = id,
        questionText = question,
        questionOrder = order,
        userTranscript = transcript,
        durationMs = durationMs,
        speechRateWpm = 128.0 + order,
        avgPauseMs = 510.0 + order * 20,
        silenceRatio = 0.12 + order * 0.01,
        createdAt = startedAt ?: createdAt,
        completedAt = completedAt,
        score = QuestionScoreDto(
            id = questionId + SCORE_ID_OFFSET,
            sessionQuestionId = questionId,
            contentRelevance = (score + 4).coerceAtMost(MAX_SCORE),
            clarity = (score + 2).coerceAtMost(MAX_SCORE),
            confidence = score,
            pacing = (score - 2).coerceAtLeast(MIN_SCORE),
            fillerWords = (score - 5).coerceAtLeast(MIN_SCORE),
            overallScore = score,
            coachingTip = coachingTip,
            createdAt = completedAt ?: "Today",
        ),
    )
}

private val fakeSessions = listOf(
    InterviewSessionDto(
        id = 101L,
        trackId = 1L,
        trackName = "Android Development",
        status = "COMPLETED",
        overallScore = 87,
        durationSeconds = 1_140,
        targetDurationMinutes = 20,
        maxQuestions = 3,
        startedAt = "2026-07-20T09:00:00Z",
        completedAt = "2026-07-20T09:19:00Z",
        createdAt = "2026-07-20T09:00:00Z",
    ),
    InterviewSessionDto(
        id = 102L,
        trackId = 2L,
        trackName = "Backend Development",
        status = "COMPLETED",
        overallScore = 76,
        durationSeconds = 1_020,
        targetDurationMinutes = 20,
        maxQuestions = 3,
        startedAt = "2026-07-17T14:30:00Z",
        completedAt = "2026-07-17T14:47:00Z",
        createdAt = "2026-07-17T14:30:00Z",
    ),
    InterviewSessionDto(
        id = 103L,
        trackId = 3L,
        trackName = "UI/UX Design",
        status = "COMPLETED",
        overallScore = 64,
        durationSeconds = 900,
        targetDurationMinutes = 20,
        maxQuestions = 3,
        startedAt = "2026-07-12T11:10:00Z",
        completedAt = "2026-07-12T11:25:00Z",
        createdAt = "2026-07-12T11:10:00Z",
    ),
)

private const val MIN_SCORE = 0
private const val MAX_SCORE = 100
private const val FEEDBACK_ID_OFFSET = 1_000L
private const val QUESTION_ID_MULTIPLIER = 10L
private const val SCORE_ID_OFFSET = 10_000L
