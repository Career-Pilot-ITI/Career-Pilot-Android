package com.iti.careerpilot.home.data.datasource.remote

import com.iti.careerpilot.core.network.Endpoints
import com.iti.careerpilot.core.network.util.safeCall
import com.iti.careerpilot.home.data.datasource.remote.dto.TrackDto
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import javax.inject.Inject

class InterviewRemoteDataSourceImpl @Inject constructor(
    private val httpClient: HttpClient,
) : InterviewRemoteDataSource {

    override suspend fun getTracks(): CareerPilotResult<List<TrackDto>, NetworkError> =
        safeCall {
            httpClient.get(Endpoints.GET_TRACKS)
        }
}
