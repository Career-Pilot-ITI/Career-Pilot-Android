package com.iti.careerpilot.ats.domain.model

data class JobWorkspace(
    val id: Long,
    val job: JobListing,
    val status: String,
    val cvScore: Int?,
    val cvScoreUpdatedAt: String?,
    val cvOptimizedText: String?,
    val coverLetterText: String?,
    val lastInterviewSessionId: Long?,
    val createdAt: String?,
    val updatedAt: String?,
)
