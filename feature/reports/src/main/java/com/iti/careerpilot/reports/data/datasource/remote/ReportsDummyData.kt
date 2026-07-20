package com.iti.careerpilot.reports.data.datasource.remote

import com.iti.careerpilot.reports.data.datasource.remote.dto.CoachingSuggestionDto
import com.iti.careerpilot.reports.data.datasource.remote.dto.PerformanceMetricsDto
import com.iti.careerpilot.reports.data.datasource.remote.dto.QuestionBreakdownDto
import com.iti.careerpilot.reports.data.datasource.remote.dto.QuestionReportDto
import com.iti.careerpilot.reports.data.datasource.remote.dto.ReportDetailsDto
import com.iti.careerpilot.reports.data.datasource.remote.dto.SessionSummaryDto

internal object ReportsDummyData {
    val sessions = listOf(
        SessionSummaryDto("session-se-001", 82, "Software Engineering", "2026-07-19T11:14:00Z", 18, 8),
        SessionSummaryDto("session-se-002", 74, "Software Engineering", "2026-07-18T09:30:00Z", 22, 8),
        SessionSummaryDto("session-sd-001", 68, "System Design", "2026-07-08T16:45:00Z", 15, 6),
        SessionSummaryDto("session-se-003", 79, "Software Engineering", "2026-07-06T13:20:00Z", 20, 8),
        SessionSummaryDto("session-be-001", 85, "Behavioural", "2026-07-04T10:10:00Z", 25, 10),
        SessionSummaryDto("session-se-004", 71, "Software Engineering", "2026-07-01T15:00:00Z", 18, 8),
    )

    private val coachingSuggestions = listOf(
        CoachingSuggestionDto(
            id = "reduce-filler-words",
            type = "filler_words",
            title = "Reduce filler words",
            description = "You used 'um' and 'uh' 14 times. Try pausing silently instead.",
            impact = "high",
        ),
        CoachingSuggestionDto(
            id = "deepen-technical-answers",
            type = "technical_depth",
            title = "Deepen technical answers",
            description = "Your system design answer lacked mention of caching strategies.",
            impact = "medium",
        ),
        CoachingSuggestionDto(
            id = "stronger-story-structure",
            type = "story_structure",
            title = "Stronger story structure",
            description = "Use STAR format for behavioural questions to score higher.",
            impact = "medium",
        ),
    )

    private val questionBank = listOf(
        QuestionReportDto(
            id = "question-1",
            index = 1,
            question = "Tell me about yourself and your background.",
            score = 88,
            fillerWordCount = 3,
            durationSeconds = 102,
            coachFeedback = "Great structure. Consider leading with impact earlier.",
            transcript = "So, uh, I think the main challenge was... um... basically we had to figure out how the architecture would, you know, handle the load...",
            fillerWords = listOf("uh", "um", "you know", "basically"),
        ),
        QuestionReportDto(
            id = "question-2",
            index = 2,
            question = "How would you design a scalable notification system?",
            score = 82,
            fillerWordCount = 2,
            durationSeconds = 134,
            coachFeedback = "Clear component boundaries. Add caching and retry trade-offs.",
            transcript = "I would start with an event queue and, um, separate workers by delivery channel so failures remain isolated.",
            fillerWords = listOf("um", "so"),
        ),
        QuestionReportDto(
            id = "question-3",
            index = 3,
            question = "Describe a time you resolved a team conflict.",
            score = 77,
            fillerWordCount = 4,
            durationSeconds = 126,
            coachFeedback = "Good outcome. Make the situation and your personal actions more explicit.",
            transcript = "Basically, two approaches were competing. I gathered the evidence, you know, and helped the team agree on measurable criteria.",
            fillerWords = listOf("basically", "you know"),
        ),
        QuestionReportDto("question-4", 4, "How do you investigate a production incident?", 80, 1, 118, "Strong sequence. Mention stakeholder communication.", "I confirm impact, inspect recent changes, and use metrics and traces to narrow the failure.", listOf("well")),
        QuestionReportDto("question-5", 5, "Explain a difficult technical trade-off you made.", 76, 2, 111, "State the alternatives before explaining the decision.", "We traded immediate consistency for availability because the workflow tolerated a short delay.", listOf("so")),
        QuestionReportDto("question-6", 6, "How do you keep an Android application maintainable?", 86, 1, 129, "Excellent layering. Include an example of enforcing boundaries in tests.", "I keep dependencies directional, isolate platform concerns, and make UI state explicit and immutable.", listOf("um")),
        QuestionReportDto("question-7", 7, "How would you improve application performance?", 84, 2, 120, "Good measurement-first approach.", "I profile before optimizing, then focus on frame timing, allocations, startup, and network payloads.", listOf("basically")),
        QuestionReportDto("question-8", 8, "Tell me about a project you are proud of.", 89, 1, 145, "Compelling impact. Tighten the opening sentence.", "I led an application redesign that reduced task completion time and improved reliability.", listOf("so")),
        QuestionReportDto("question-9", 9, "How do you respond to critical feedback?", 83, 1, 108, "Balanced and specific answer.", "I ask for examples, confirm the expected change, and follow up after applying the feedback.", listOf("well")),
        QuestionReportDto("question-10", 10, "What would you improve about your last project?", 78, 2, 116, "Connect the lesson to a concrete future action.", "I would establish observability earlier and validate the data model with realistic load tests.", listOf("um")),
    )

    val detailsBySessionId = sessions.associate { session ->
        session.id to ReportDetailsDto(
            sessionId = session.id,
            completedAt = session.completedAt,
            overallScore = session.score,
            performanceTier = when {
                session.score >= 80 -> "strong"
                session.score >= 70 -> "good"
                else -> "needs_improvement"
            },
            topPercent = if (session.id == "session-se-001") 28 else (100 - session.score).coerceAtLeast(10),
            metrics = if (session.id == "session-se-001") {
                PerformanceMetricsDto(78, 85, 72, 65, 90)
            } else {
                PerformanceMetricsDto(
                    clarity = session.score,
                    confidence = (session.score + 3).coerceAtMost(100),
                    pacing = (session.score - 4).coerceAtLeast(0),
                    fillerWords = (session.score - 8).coerceAtLeast(0),
                    content = (session.score + 6).coerceAtMost(100),
                )
            },
            coachingSuggestions = coachingSuggestions,
        )
    }

    val breakdownsBySessionId = sessions.associate { session ->
        session.id to QuestionBreakdownDto(
            sessionId = session.id,
            questions = questionBank.take(session.questionCount).mapIndexed { index, question ->
                question.copy(
                    id = "${session.id}-q${index + 1}",
                    index = index + 1,
                )
            },
        )
    }
}
