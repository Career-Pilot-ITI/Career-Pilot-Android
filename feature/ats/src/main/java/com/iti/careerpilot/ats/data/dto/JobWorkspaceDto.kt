package com.iti.careerpilot.ats.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JobWorkspaceDto(
    @SerialName("id") val id: Long,
    @SerialName("job") val job: JobDto,
    @SerialName("status") val status: String? = null,
    @SerialName("cvScore") val cvScore: Int? = null,
    @SerialName("cvScoreUpdatedAt") val cvScoreUpdatedAt: String? = null,
    @SerialName("cvOptimizedText") val cvOptimizedText: String? = null,
    @SerialName("coverLetterText") val coverLetterText: String? = null,
    @SerialName("lastInterviewSessionId") val lastInterviewSessionId: Long? = null,
    @SerialName("createdAt") val createdAt: String? = null,
    @SerialName("updatedAt") val updatedAt: String? = null,
)
