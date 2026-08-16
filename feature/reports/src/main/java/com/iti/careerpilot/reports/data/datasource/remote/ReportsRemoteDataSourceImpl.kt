package com.iti.careerpilot.reports.data.datasource.remote

import com.iti.careerpilot.core.network.Endpoints
import com.iti.careerpilot.core.network.util.safeCall
import com.iti.careerpilot.reports.data.datasource.remote.dto.FeedbackReportDto
import com.iti.careerpilot.reports.data.datasource.remote.dto.ReportsApiResponseDto
import com.iti.careerpilot.reports.data.datasource.remote.dto.SessionQuestionDto
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import javax.inject.Inject

class ReportsRemoteDataSourceImpl @Inject constructor(
    private val client: HttpClient,
) : ReportsRemoteDataSource {
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
        is CareerPilotResult.Success -> {
            val payload = result.data.data
            if (payload != null) {
                CareerPilotResult.Success(payload)
            } else {
                CareerPilotResult.Error(NetworkError.EMPTY_RESULT)
            }
        }
        is CareerPilotResult.Error -> CareerPilotResult.Error(result.error)
    }
}
