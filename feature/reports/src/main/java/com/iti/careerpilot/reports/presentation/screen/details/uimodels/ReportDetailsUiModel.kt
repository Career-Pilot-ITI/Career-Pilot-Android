package com.iti.careerpilot.reports.presentation.screen.details.uimodels

import androidx.compose.runtime.Immutable
import com.iti.careerpilot.reports.domain.model.CoachingImpact
import com.iti.careerpilot.reports.domain.model.PerformanceTier
import com.iti.careerpilot.reports.domain.model.ReportDetails
import java.time.Instant
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

@Immutable
data class ReportDetailsUiModel(
    val sessionId: String,
    val completedAt: Instant,
    val overallScore: Int,
    val performanceTier: PerformanceTier,
    val topPercent: Int?,
    val metrics: PersistentList<PerformanceMetricUiModel>,
    val coachingSuggestions: PersistentList<CoachingSuggestionUiModel>,
)

@Immutable
data class PerformanceMetricUiModel(
    val type: PerformanceMetricType,
    val score: Int,
)

enum class PerformanceMetricType {
    CLARITY,
    CONFIDENCE,
    PACING,
    FILLER_WORDS,
    CONTENT,
}

@Immutable
data class CoachingSuggestionUiModel(
    val id: String,
    val ordinal: Int,
    val description: String,
    val impact: CoachingImpact,
)

fun ReportDetails.toUiModel(): ReportDetailsUiModel = ReportDetailsUiModel(
    sessionId = sessionId,
    completedAt = completedAt,
    overallScore = overallScore,
    performanceTier = performanceTier,
    topPercent = topPercent,
    metrics = persistentListOf(
        PerformanceMetricUiModel(PerformanceMetricType.CLARITY, metrics.clarity),
        PerformanceMetricUiModel(PerformanceMetricType.CONFIDENCE, metrics.confidence),
        PerformanceMetricUiModel(PerformanceMetricType.PACING, metrics.pacing),
        PerformanceMetricUiModel(PerformanceMetricType.FILLER_WORDS, metrics.fillerWords),
        PerformanceMetricUiModel(PerformanceMetricType.CONTENT, metrics.content),
    ),
    coachingSuggestions = coachingSuggestions.map { suggestion ->
        CoachingSuggestionUiModel(
            id = suggestion.id,
            ordinal = suggestion.ordinal,
            description = suggestion.description,
            impact = suggestion.impact,
        )
    }.toPersistentList(),
)
