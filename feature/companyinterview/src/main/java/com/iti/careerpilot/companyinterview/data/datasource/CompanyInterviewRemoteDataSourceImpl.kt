package com.iti.careerpilot.companyinterview.data.datasource

import com.iti.careerpilot.companyinterview.data.models.*
import com.iti.careerpilot.core.network.Endpoints
import com.iti.careerpilot.core.network.util.safeCall
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import javax.inject.Inject

class CompanyInterviewRemoteDataSourceImpl @Inject constructor(
    private val httpClient: HttpClient
) : CompanyInterviewRemoteDataSource {

    override suspend fun getMetadata(token: String): CareerPilotResult<ApplicantInterviewMetadataDto, NetworkError> {
        return safeCall {
            httpClient.get(Endpoints.applicantInterviewMetadata(token))
        }
    }

    override suspend fun verifyEmail(
        token: String,
        email: String
    ): CareerPilotResult<ApplicantInterviewMetadataDto, NetworkError> {
        return safeCall {
            httpClient.post(Endpoints.applicantVerifyEmail(token)) {
                contentType(ContentType.Application.Json)
                setBody(ApplicantVerifyEmailRequestDto(email))
            }
        }
    }

    override suspend fun startSession(token: String): CareerPilotResult<StartApplicantSessionResponseDto, NetworkError> {
        return safeCall {
            httpClient.post(Endpoints.applicantStartSession(token))
        }
    }

    override suspend fun submitAnswer(
        token: String,
        request: SubmitApplicantAnswerRequestDto
    ): CareerPilotResult<SubmitApplicantAnswerResponseDto, NetworkError> {
        return safeCall {
            httpClient.post(Endpoints.applicantSubmitAnswer(token)) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }

    override suspend fun completeSession(token: String): CareerPilotResult<SubmitApplicantAnswerResponseDto, NetworkError> {
        return safeCall {
            httpClient.post(Endpoints.applicantCompleteSession(token))
        }
    }

    override suspend fun getSessionState(token: String): CareerPilotResult<ApplicantSessionRecoveryResponseDto, NetworkError> {
        return safeCall {
            httpClient.get(Endpoints.applicantSessionState(token))
        }
    }
}
