package com.iti.careerpilot.companyinterview.domain.models

data class CompanyInterviewMetadata(
    val token: String,
    val interviewId: Long,
    val title: String,
    val description: String?,
    val deadline: String,
    val companyName: String,
    val companyLogoUrl: String?,
    val companyBrandColor: String,
    val totalQuestions: Int,
    val applicantEmail: String,
    val applicantName: String?,
    val emailVerified: Boolean,
    val sessionStarted: Boolean,
    val sessionCompleted: Boolean,
    val proctoringRules: List<String>
)

data class ApplicantQuestion(
    val questionId: Long,
    val questionOrder: Int,
    val questionText: String,
    val timeLimitSeconds: Int,
    val totalQuestions: Int,
    val isLastQuestion: Boolean
)

data class CompanyInterviewSessionState(
    val sessionId: Long,
    val currentQuestion: ApplicantQuestion?,
    val totalQuestions: Int,
    val answeredCount: Int,
    val isCompleted: Boolean
)
