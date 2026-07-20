package com.iti.careerpilot.reports.domain.model

import java.time.Instant

data class ReportDetails(
    val sessionId: String,
    val completedAt: Instant,
    val overallScore: Int,
    val performanceTier: PerformanceTier,
    val topPercent: Int,
    val metrics: PerformanceMetrics,
    val coachingSuggestions: List<CoachingSuggestion>,
)

data class PerformanceMetrics(
    val clarity: Int,
    val confidence: Int,
    val pacing: Int,
    val fillerWords: Int,
    val content: Int,
)

enum class PerformanceTier {
    STRONG,
    GOOD,
    NEEDS_IMPROVEMENT,
}

data class CoachingSuggestion(
    val id: String,
    val type: CoachingSuggestionType,
    val title: String,
    val description: String,
    val impact: CoachingImpact,
)

enum class CoachingSuggestionType {
    FILLER_WORDS,
    TECHNICAL_DEPTH,
    STORY_STRUCTURE,
    OTHER,
}

enum class CoachingImpact {
    HIGH,
    MEDIUM,
    LOW,
}
