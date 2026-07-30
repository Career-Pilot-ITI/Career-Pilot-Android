package com.iti.careerpilot.reports.data.datasource.remote

import com.iti.careerpilot.core.network.Endpoints
import com.iti.careerpilot.core.network.util.safeCall
import com.iti.careerpilot.reports.data.datasource.remote.dto.FeedbackReportDto
import com.iti.careerpilot.reports.data.datasource.remote.dto.InterviewSessionDto
import com.iti.careerpilot.reports.data.datasource.remote.dto.InterviewSessionsPageDto
import com.iti.careerpilot.reports.data.datasource.remote.dto.ReportsApiResponseDto
import com.iti.careerpilot.reports.data.datasource.remote.dto.SessionQuestionDto
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import javax.inject.Inject

class ReportsRemoteDataSourceImpl @Inject constructor(
    private val client: HttpClient,
) : ReportsRemoteDataSource {
    override suspend fun getSessions(
        page: Int,
        size: Int,
    ): CareerPilotResult<InterviewSessionsPageDto, NetworkError> = when (
        val result = safeCall<ReportsApiResponseDto<InterviewSessionsPageDto>> {
            client.get(Endpoints.INTERVIEW_SESSIONS) {
                parameter("page", page)
                parameter("size", size)
            }
        }
    ) {
        is CareerPilotResult.Success -> CareerPilotResult.Success(result.data.data)
        is CareerPilotResult.Error -> CareerPilotResult.Error(result.error)
    }

    override suspend fun getSession(
        sessionId: Long,
    ): CareerPilotResult<InterviewSessionDto, NetworkError> =
        getPayload(Endpoints.interviewSession(sessionId))

    override suspend fun getFeedback(
        sessionId: Long,
    ): CareerPilotResult<FeedbackReportDto, NetworkError> =
        getPayload(Endpoints.interviewSessionFeedback(sessionId))

    override suspend fun getQuestions(
        sessionId: Long,
    ): CareerPilotResult<List<SessionQuestionDto>, NetworkError> =
        getPayload(Endpoints.interviewSessionQuestions(sessionId))

    private suspend inline fun <reified T> getPayload(
        endpoint: String,
    ): CareerPilotResult<T, NetworkError> = when (
        val result = safeCall<ReportsApiResponseDto<T>> { client.get(endpoint) }
    ) {
        is CareerPilotResult.Success -> CareerPilotResult.Success(result.data.data)
        is CareerPilotResult.Error -> CareerPilotResult.Error(result.error)
    }
}
