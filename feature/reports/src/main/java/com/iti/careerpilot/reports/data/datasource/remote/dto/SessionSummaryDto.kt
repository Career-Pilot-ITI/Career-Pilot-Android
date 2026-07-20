package com.iti.careerpilot.reports.data.datasource.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SessionSummaryDto(
    val id: String,
    val score: Int,
    val category: String,
    val completedAt: String,
    val durationMinutes: Int,
    val questionCount: Int,
)
