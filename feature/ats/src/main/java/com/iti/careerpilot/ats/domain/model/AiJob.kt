package com.iti.careerpilot.ats.domain.model

data class AiJob(
    val id: Long,
    val workspaceId: Long,
    val type: AiJobType,
    val status: AiJobStatus,
    val progressPercentage: Int,
    val currentStep: String,
    val result: CvOptimization?,
    val errorMessage: String?,
    val createdAt: String?,
    val startedAt: String?,
    val completedAt: String?,
)

enum class AiJobType {
    CV_OPTIMIZE,
    UNKNOWN,
}

enum class AiJobStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    UNKNOWN,
}
