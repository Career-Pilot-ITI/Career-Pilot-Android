package com.iti.careerpilot.reports.presentation.screen.history.uimodels

import androidx.compose.runtime.Immutable
import com.iti.careerpilot.reports.domain.model.InterviewSessionSummary
import java.time.Instant

@Immutable
data class SessionHistoryUiModel(
    val sessions: List<SessionSummaryUiModel>,
    val averageScore: Int,
)

@Immutable
data class SessionSummaryUiModel(
    val id: Long,
    val score: Int,
    val category: String,
    val completedAt: Instant,
    val durationMinutes: Int,
    val questionCount: Int,
)

fun List<InterviewSessionSummary>.toUiModel(): SessionHistoryUiModel =
    SessionHistoryUiModel(
        sessions = map { session ->
            SessionSummaryUiModel(
                id = session.id,
                score = session.score,
                category = session.category,
                completedAt = session.completedAt,
                durationMinutes = session.durationMinutes,
                questionCount = session.questionCount,
            )
        },
        averageScore = if (isEmpty()) 0 else sumOf(InterviewSessionSummary::score) / size,
    )
