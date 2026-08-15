package com.iti.careerpilot.core.interviews.domain.model

data class InterviewSessionPage(
    val sessions: List<InterviewSession>,
    val pageNumber: Int,
    val totalPages: Int,
    val totalElements: Long,
    val isFirst: Boolean,
    val isLast: Boolean,
)
