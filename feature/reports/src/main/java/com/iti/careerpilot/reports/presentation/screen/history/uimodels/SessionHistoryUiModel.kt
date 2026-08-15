package com.iti.careerpilot.reports.presentation.screen.history.uimodels

import androidx.compose.runtime.Immutable
import com.iti.careerpilot.core.interviews.domain.model.InterviewSession
import java.time.Instant

@Immutable
data class SessionSummaryUiModel(
    val id: Long,
    val trackId: Long?,
    val score: Int?,
    val category: String,
    val occurredAt: Instant?,
    val durationMinutes: Int,
    val questionCount: Int,
    val isCompleted: Boolean,
    val isResumable: Boolean,
)

fun InterviewSession.toUiModel(): SessionSummaryUiModel =
    SessionSummaryUiModel(
        id = id,
        trackId = trackId,
        score = score,
        category = trackName,
        occurredAt = occurredAt,
        durationMinutes = durationMinutes,
        questionCount = questionCount,
        isCompleted = isCompleted,
        isResumable = isResumable,
    )
