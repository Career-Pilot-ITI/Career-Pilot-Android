package com.iti.careerpilot.reports.domain.model

data class QuestionBreakdown(
    val sessionId: Long,
    val questions: List<QuestionReport>,
)
