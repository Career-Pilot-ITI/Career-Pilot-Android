package com.iti.careerpilot.core.interviews.data.remote

import com.iti.careerpilot.core.interviews.data.remote.dto.InterviewSessionDto
import com.iti.careerpilot.core.interviews.data.remote.dto.InterviewSessionPageDto
import com.iti.careerpilot.core.interviews.data.remote.dto.InterviewsEnvelopeDto
import com.iti.careerpilot.core.network.Endpoints
import com.iti.careerpilot.core.network.util.safeCall
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import javax.inject.Inject

class InterviewSessionRemoteDataSourceImpl @Inject constructor(
    private val httpClient: HttpClient,
) : InterviewSessionRemoteDataSource {

    override suspend fun getSessions(
        page: Int,
        size: Int,
    ): CareerPilotResult<InterviewSessionPageDto, NetworkError> =
        safeCall<InterviewsEnvelopeDto<InterviewSessionPageDto>> {
            httpClient.get(Endpoints.INTERVIEW_SESSIONS) {
                parameter("page", page)
                parameter("size", size)
            }
        }.unwrap()

    override suspend fun getSession(
        sessionId: Long,
    ): CareerPilotResult<InterviewSessionDto, NetworkError> =
        safeCall<InterviewsEnvelopeDto<InterviewSessionDto>> {
            httpClient.get(Endpoints.interviewSession(sessionId))
        }.unwrap()

    private fun <T> CareerPilotResult<InterviewsEnvelopeDto<T>, NetworkError>.unwrap():
        CareerPilotResult<T, NetworkError> = when (this) {
        is CareerPilotResult.Error -> this
        is CareerPilotResult.Success -> data.data
            ?.let { CareerPilotResult.Success(it) }
            ?: CareerPilotResult.Error(NetworkError.EMPTY_RESULT)
    }
}
