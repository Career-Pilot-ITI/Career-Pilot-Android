package com.iti.careerpilot.companyinterview.data.datasource

import com.iti.careerpilot.companyinterview.data.models.*
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult

interface CompanyInterviewRemoteDataSource {
    suspend fun getMetadata(token: String): CareerPilotResult<ApplicantInterviewMetadataDto, NetworkError>
    suspend fun verifyEmail(token: String, email: String): CareerPilotResult<ApplicantInterviewMetadataDto, NetworkError>
    suspend fun startSession(token: String): CareerPilotResult<StartApplicantSessionResponseDto, NetworkError>
    suspend fun submitAnswer(token: String, request: SubmitApplicantAnswerRequestDto): CareerPilotResult<SubmitApplicantAnswerResponseDto, NetworkError>
    suspend fun completeSession(token: String): CareerPilotResult<SubmitApplicantAnswerResponseDto, NetworkError>
    suspend fun getSessionState(token: String): CareerPilotResult<ApplicantSessionRecoveryResponseDto, NetworkError>
}
