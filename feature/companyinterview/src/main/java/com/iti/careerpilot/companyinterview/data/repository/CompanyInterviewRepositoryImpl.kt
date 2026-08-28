package com.iti.careerpilot.companyinterview.data.repository

import com.iti.careerpilot.companyinterview.data.datasource.CompanyInterviewRemoteDataSource
import com.iti.careerpilot.companyinterview.data.models.ApplicantQuestionDto
import com.iti.careerpilot.companyinterview.data.models.SubmitApplicantAnswerRequestDto
import com.iti.careerpilot.companyinterview.domain.models.ApplicantQuestion
import com.iti.careerpilot.companyinterview.domain.models.CompanyInterviewMetadata
import com.iti.careerpilot.companyinterview.domain.models.CompanyInterviewSessionState
import com.iti.careerpilot.companyinterview.domain.repository.CompanyInterviewRepository
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import com.iti.common.result.map
import javax.inject.Inject

class CompanyInterviewRepositoryImpl @Inject constructor(
    private val remoteDataSource: CompanyInterviewRemoteDataSource
) : CompanyInterviewRepository {

    override suspend fun getMetadata(token: String): CareerPilotResult<CompanyInterviewMetadata, NetworkError> {
        return remoteDataSource.getMetadata(token).map { dto ->
            CompanyInterviewMetadata(
                token = dto.token,
                interviewId = dto.interviewId,
                title = dto.title,
                description = dto.description,
                deadline = dto.deadline,
                companyName = dto.companyName,
                companyLogoUrl = dto.companyLogoUrl,
                companyBrandColor = dto.companyBrandColor ?: "#2563EB",
                totalQuestions = dto.totalQuestions,
                applicantEmail = dto.applicantEmail,
                applicantName = dto.applicantName,
                emailVerified = dto.emailVerified,
                sessionStarted = dto.sessionStarted,
                sessionCompleted = dto.sessionCompleted,
                proctoringRules = dto.proctoringRules
            )
        }
    }

    override suspend fun verifyEmail(
        token: String,
        email: String
    ): CareerPilotResult<CompanyInterviewMetadata, NetworkError> {
        return remoteDataSource.verifyEmail(token, email).map { dto ->
            CompanyInterviewMetadata(
                token = dto.token,
                interviewId = dto.interviewId,
                title = dto.title,
                description = dto.description,
                deadline = dto.deadline,
                companyName = dto.companyName,
                companyLogoUrl = dto.companyLogoUrl,
                companyBrandColor = dto.companyBrandColor ?: "#2563EB",
                totalQuestions = dto.totalQuestions,
                applicantEmail = dto.applicantEmail,
                applicantName = dto.applicantName,
                emailVerified = dto.emailVerified,
                sessionStarted = dto.sessionStarted,
                sessionCompleted = dto.sessionCompleted,
                proctoringRules = dto.proctoringRules
            )
        }
    }

    override suspend fun startSession(token: String): CareerPilotResult<CompanyInterviewSessionState, NetworkError> {
        return remoteDataSource.startSession(token).map { dto ->
            CompanyInterviewSessionState(
                sessionId = dto.sessionId,
                currentQuestion = dto.currentQuestion.toDomain(),
                totalQuestions = dto.totalQuestions,
                answeredCount = 0,
                isCompleted = false
            )
        }
    }

    override suspend fun submitAnswer(
        token: String,
        questionId: Long,
        transcript: String?,
        durationSeconds: Int,
        speechRateWpm: Double?,
        avgPauseMs: Double?,
        silenceRatio: Double?,
        fillerWordCount: Int,
        bodyLanguageMetrics: Map<String, String>?,
        proctoringFlags: Map<String, String>?
    ): CareerPilotResult<ApplicantQuestion?, NetworkError> {
        val request = SubmitApplicantAnswerRequestDto(
            questionId = questionId,
            transcript = transcript,
            durationSeconds = durationSeconds,
            speechRateWpm = speechRateWpm,
            avgPauseMs = avgPauseMs,
            silenceRatio = silenceRatio,
            fillerWordCount = fillerWordCount,
            bodyLanguageMetrics = bodyLanguageMetrics,
            proctoringFlags = proctoringFlags
        )

        return remoteDataSource.submitAnswer(token, request).map { dto ->
            dto.nextQuestion?.toDomain()
        }
    }

    override suspend fun completeSession(token: String): CareerPilotResult<Boolean, NetworkError> {
        return remoteDataSource.completeSession(token).map { dto ->
            dto.sessionCompleted
        }
    }

    override suspend fun recoverSession(token: String): CareerPilotResult<CompanyInterviewSessionState, NetworkError> {
        return remoteDataSource.getSessionState(token).map { dto ->
            CompanyInterviewSessionState(
                sessionId = dto.sessionId,
                currentQuestion = dto.currentQuestion?.toDomain(),
                totalQuestions = dto.totalQuestions,
                answeredCount = dto.answeredQuestionsCount,
                isCompleted = dto.isCompleted
            )
        }
    }

    private fun ApplicantQuestionDto.toDomain() = ApplicantQuestion(
        questionId = questionId,
        questionOrder = questionOrder,
        questionText = questionText,
        timeLimitSeconds = timeLimitSeconds,
        totalQuestions = totalQuestions,
        isLastQuestion = isLastQuestion
    )
}
