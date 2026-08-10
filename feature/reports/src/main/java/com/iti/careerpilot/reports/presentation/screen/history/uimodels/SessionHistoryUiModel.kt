package com.iti.careerpilot.reports.presentation.screen.history.uimodels

import androidx.compose.runtime.Immutable
import com.iti.careerpilot.reports.domain.model.InterviewSessionSummary
import java.time.Instant

@Immutable
data class SessionSummaryUiModel(
    val id: Long,
    val score: Int,
    val category: String,
    val completedAt: Instant,
    val durationMinutes: Int,
    val questionCount: Int,
)

fun InterviewSessionSummary.toUiModel(): SessionSummaryUiModel =
    SessionSummaryUiModel(
        id = id,
        score = score,
        category = category,
        completedAt = completedAt,
        durationMinutes = durationMinutes,
        questionCount = questionCount,
    )
