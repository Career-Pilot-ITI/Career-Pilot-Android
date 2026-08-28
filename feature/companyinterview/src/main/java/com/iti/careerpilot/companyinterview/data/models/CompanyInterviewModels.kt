package com.iti.careerpilot.companyinterview.data.models

import kotlinx.serialization.Serializable

@Serializable
data class ApplicantInterviewMetadataDto(
    val token: String,
    val interviewId: Long,
    val title: String,
    val description: String? = null,
    val deadline: String,
    val companyName: String,
    val companyLogoUrl: String? = null,
    val companyBrandColor: String? = "#2563EB",
    val totalQuestions: Int,
    val applicantEmail: String,
    val applicantName: String? = null,
    val emailVerified: Boolean = false,
    val sessionStarted: Boolean = false,
    val sessionCompleted: Boolean = false,
    val proctoringRules: List<String> = emptyList()
)

@Serializable
data class ApplicantVerifyEmailRequestDto(
    val email: String
)

@Serializable
data class ApplicantQuestionDto(
    val questionId: Long,
    val questionOrder: Int,
    val questionText: String,
    val timeLimitSeconds: Int,
    val totalQuestions: Int,
    val isLastQuestion: Boolean = false
)

@Serializable
data class StartApplicantSessionResponseDto(
    val sessionId: Long,
    val totalQuestions: Int,
    val currentQuestion: ApplicantQuestionDto
)

@Serializable
data class SubmitApplicantAnswerRequestDto(
    val questionId: Long,
    val transcript: String? = null,
    val durationSeconds: Int = 0,
    val speechRateWpm: Double? = null,
    val avgPauseMs: Double? = null,
    val silenceRatio: Double? = null,
    val fillerWordCount: Int = 0,
    val bodyLanguageMetrics: Map<String, String>? = null,
    val proctoringFlags: Map<String, String>? = null
)

@Serializable
data class SubmitApplicantAnswerResponseDto(
    val nextQuestion: ApplicantQuestionDto? = null,
    val isLastQuestion: Boolean = false,
    val sessionCompleted: Boolean = false,
    val message: String? = null
)

@Serializable
data class ApplicantSessionRecoveryResponseDto(
    val sessionId: Long,
    val status: String,
    val currentQuestionIndex: Int,
    val answeredQuestionsCount: Int,
    val totalQuestions: Int,
    val currentQuestion: ApplicantQuestionDto? = null,
    val isCompleted: Boolean = false
)
