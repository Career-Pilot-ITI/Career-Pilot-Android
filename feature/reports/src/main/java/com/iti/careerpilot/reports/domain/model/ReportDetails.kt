package com.iti.careerpilot.reports.domain.model

import java.time.Instant

data class ReportDetails(
    val sessionId: Long,
    val completedAt: Instant,
    val overallScore: Int,
    val performanceTier: PerformanceTier,
    val topPercent: Int?,
    val metrics: PerformanceMetrics,
    val coachingSuggestions: List<CoachingSuggestion>,
)
