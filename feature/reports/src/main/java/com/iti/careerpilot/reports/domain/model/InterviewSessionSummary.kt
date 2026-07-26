package com.iti.careerpilot.reports.domain.model

import java.time.Instant

data class InterviewSessionSummary(
    val id: Long,
    val score: Int,
    val category: String,
    val completedAt: Instant,
    val durationMinutes: Int,
    val questionCount: Int,
)
