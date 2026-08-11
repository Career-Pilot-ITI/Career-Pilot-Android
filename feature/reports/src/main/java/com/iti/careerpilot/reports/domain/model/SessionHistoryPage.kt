package com.iti.careerpilot.reports.domain.model

data class SessionHistoryPage(
    val sessions: List<InterviewSessionSummary>,
    val pageNumber: Int,
    val totalPages: Int,
    val totalElements: Long,
    val isFirst: Boolean,
    val isLast: Boolean,
)
