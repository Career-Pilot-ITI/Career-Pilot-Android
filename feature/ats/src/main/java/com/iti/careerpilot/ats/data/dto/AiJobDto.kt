package com.iti.careerpilot.ats.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AiJobDto(
    @SerialName("id") val id: Long,
    @SerialName("workspaceId") val workspaceId: Long,
    @SerialName("type") val type: String? = null,
    @SerialName("status") val status: String? = null,
    @SerialName("progressPercentage") val progressPercentage: Int = 0,
    @SerialName("currentStep") val currentStep: String? = null,
    @SerialName("result") val result: CvOptimizationDto? = null,
    @SerialName("errorMessage") val errorMessage: String? = null,
    @SerialName("createdAt") val createdAt: String? = null,
    @SerialName("startedAt") val startedAt: String? = null,
    @SerialName("completedAt") val completedAt: String? = null,
)
