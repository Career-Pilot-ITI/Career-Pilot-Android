package com.iti.careerpilot.reports.domain.model

data class QuestionBreakdown(
    val sessionId: String,
    val questions: List<QuestionReport>,
)