package com.iti.careerpilot.reports.domain.model

data class PerformanceMetrics(
    val clarity: Int,
    val confidence: Int,
    val pacing: Int,
    val fillerWords: Int,
    val content: Int,
)