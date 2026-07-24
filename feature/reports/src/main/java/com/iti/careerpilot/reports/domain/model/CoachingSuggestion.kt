package com.iti.careerpilot.reports.domain.model

data class CoachingSuggestion(
    val id: String,
    val ordinal: Int,
    val description: String,
    val impact: CoachingImpact,
)
