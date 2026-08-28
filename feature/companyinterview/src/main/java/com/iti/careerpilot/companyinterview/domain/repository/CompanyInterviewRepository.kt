package com.iti.careerpilot.companyinterview.domain.repository

import com.iti.careerpilot.companyinterview.domain.models.ApplicantQuestion
import com.iti.careerpilot.companyinterview.domain.models.CompanyInterviewMetadata
import com.iti.careerpilot.companyinterview.domain.models.CompanyInterviewSessionState
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult

interface CompanyInterviewRepository {
    suspend fun getMetadata(token: String): CareerPilotResult<CompanyInterviewMetadata, NetworkError>
    suspend fun verifyEmail(token: String, email: String): CareerPilotResult<CompanyInterviewMetadata, NetworkError>
    suspend fun startSession(token: String): CareerPilotResult<CompanyInterviewSessionState, NetworkError>
    suspend fun submitAnswer(
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
    ): CareerPilotResult<ApplicantQuestion?, NetworkError>
    suspend fun completeSession(token: String): CareerPilotResult<Boolean, NetworkError>
    suspend fun recoverSession(token: String): CareerPilotResult<CompanyInterviewSessionState, NetworkError>
}
