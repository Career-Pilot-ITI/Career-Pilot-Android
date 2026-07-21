package com.iti.careerpilot.reports.testutil

import com.iti.careerpilot.reports.domain.model.CoachingImpact
import com.iti.careerpilot.reports.domain.model.CoachingSuggestion
import com.iti.careerpilot.reports.domain.model.InterviewSessionSummary
import com.iti.careerpilot.reports.domain.model.PerformanceMetrics
import com.iti.careerpilot.reports.domain.model.PerformanceTier
import com.iti.careerpilot.reports.domain.model.QuestionBreakdown
import com.iti.careerpilot.reports.domain.model.QuestionReport
import com.iti.careerpilot.reports.domain.model.ReportDetails
import com.iti.careerpilot.reports.domain.repository.ReportsRepository
import com.iti.common.error.NetworkError
import com.iti.common.network.NetworkMonitor
import com.iti.common.result.CareerPilotResult
import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeNetworkMonitor(initialOnline: Boolean = true) : NetworkMonitor {
    private val online = MutableStateFlow(initialOnline)
    override val isOnline: StateFlow<Boolean> = online
    fun setOnline(value: Boolean) {
        online.value = value
    }
}

class FakeReportsRepository : ReportsRepository {
    var historyResult: CareerPilotResult<List<InterviewSessionSummary>, NetworkError> =
        CareerPilotResult.Success(listOf(sampleSession))
    var detailsResult: CareerPilotResult<ReportDetails, NetworkError> =
        CareerPilotResult.Success(sampleDetails)
    var breakdownResult: CareerPilotResult<QuestionBreakdown, NetworkError> =
        CareerPilotResult.Success(sampleBreakdown)
    var historyCalls = 0
    var detailsCalls = 0
    var breakdownCalls = 0

    override suspend fun getSessionHistory() = historyResult.also { historyCalls++ }

    override suspend fun getReportDetails(sessionId: String) = detailsResult.also { detailsCalls++ }

    override suspend fun getQuestionBreakdown(sessionId: String) =
        breakdownResult.also { breakdownCalls++ }
}

val sampleSession = InterviewSessionSummary(
    id = "session-1",
    score = 82,
    category = "Software Engineering",
    completedAt = Instant.parse("2026-07-19T11:14:00Z"),
    durationMinutes = 18,
    questionCount = 3,
)

val sampleDetails = ReportDetails(
    sessionId = sampleSession.id,
    completedAt = sampleSession.completedAt,
    overallScore = 82,
    performanceTier = PerformanceTier.STRONG,
    topPercent = 28,
    metrics = PerformanceMetrics(78, 85, 72, 65, 90),
    coachingSuggestions = listOf(
        CoachingSuggestion(
            id = "suggestion-1",
            ordinal = 1,
            description = "Pause silently instead.",
            impact = CoachingImpact.HIGH,
        ),
    ),
)

val sampleQuestions = listOf(
    QuestionReport(
        id = "question-1",
        index = 1,
        question = "Tell me about yourself.",
        score = 88,
        fillerWordCount = 1,
        durationSeconds = 90,
        coachFeedback = "Good structure.",
        transcript = "Uh, I build Android applications.",
        fillerWords = listOf("uh"),
    ),
    QuestionReport(
        id = "question-2",
        index = 2,
        question = "Describe a technical trade-off.",
        score = 80,
        fillerWordCount = 0,
        durationSeconds = 100,
        coachFeedback = "Clear answer.",
        transcript = "I compared consistency and availability.",
        fillerWords = emptyList(),
    ),
)

val sampleBreakdown = QuestionBreakdown(
    sessionId = sampleSession.id,
    questions = sampleQuestions,
)
