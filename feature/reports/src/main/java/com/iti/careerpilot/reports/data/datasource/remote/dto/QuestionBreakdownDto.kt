package com.iti.careerpilot.reports.data.datasource.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class QuestionBreakdownDto(
    val sessionId: String,
    val questions: List<QuestionReportDto>,
)

@Serializable
data class QuestionReportDto(
    val id: String,
    val index: Int,
    val question: String,
    val score: Int,
    val fillerWordCount: Int,
    val durationSeconds: Int,
    val coachFeedback: String,
    val transcript: String,
    val fillerWords: List<String>,
)
