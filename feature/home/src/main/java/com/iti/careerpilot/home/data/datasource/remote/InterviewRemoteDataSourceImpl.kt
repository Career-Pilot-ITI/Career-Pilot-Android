package com.iti.careerpilot.home.data.datasource.remote

import com.iti.careerpilot.core.network.Endpoints
import com.iti.careerpilot.core.network.util.safeCall
import com.iti.careerpilot.home.data.datasource.remote.dto.ApiEnvelopeDto
import com.iti.careerpilot.home.data.datasource.remote.dto.InterviewSessionDto
import com.iti.careerpilot.home.data.datasource.remote.dto.PageResponseDto
import com.iti.careerpilot.home.data.datasource.remote.dto.TrackDto
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import javax.inject.Inject

class InterviewRemoteDataSourceImpl @Inject constructor(
    private val httpClient: HttpClient,
) : InterviewRemoteDataSource {

    override suspend fun getInterviewSessions(): CareerPilotResult<List<InterviewSessionDto>, NetworkError> =
        safeCall<ApiEnvelopeDto<PageResponseDto<InterviewSessionDto>>> {
            httpClient.get(Endpoints.INTERVIEW_SESSIONS)
        }.unwrap { it?.content ?: emptyList() }

    override suspend fun getTracks(): CareerPilotResult<List<TrackDto>, NetworkError> =
        safeCall {
            httpClient.get(Endpoints.GET_TRACKS)
        }

    private inline fun <T, R> CareerPilotResult<ApiEnvelopeDto<T>, NetworkError>.unwrap(
        transform: (T?) -> R?,
    ): CareerPilotResult<R, NetworkError> = when (this) {
        is CareerPilotResult.Error -> this
        is CareerPilotResult.Success ->
            transform(data.data)
                ?.let { CareerPilotResult.Success(it) }
                ?: CareerPilotResult.Error(NetworkError.EMPTY_RESULT)
    }
}
