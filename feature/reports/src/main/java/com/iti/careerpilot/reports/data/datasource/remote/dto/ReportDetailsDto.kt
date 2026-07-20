package com.iti.careerpilot.reports.data.datasource.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ReportDetailsDto(
    val sessionId: String,
    val completedAt: String,
    val overallScore: Int,
    val performanceTier: String,
    val topPercent: Int,
    val metrics: PerformanceMetricsDto,
    val coachingSuggestions: List<CoachingSuggestionDto>,
)

@Serializable
data class PerformanceMetricsDto(
    val clarity: Int,
    val confidence: Int,
    val pacing: Int,
    val fillerWords: Int,
    val content: Int,
)

@Serializable
data class CoachingSuggestionDto(
    val id: String,
    val type: String,
    val title: String,
    val description: String,
    val impact: String,
)
