package com.iti.careerpilot.profile.data.datasource.remote

import com.iti.careerpilot.core.network.Endpoints
import com.iti.careerpilot.core.network.model.UserProfileDto
import com.iti.careerpilot.core.safecall.safeApiCall
import com.iti.careerpilot.profile.domain.datasource.remote.ProfileRemoteDataSource
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import javax.inject.Inject

class ProfileRemoteDataSourceImpl @Inject constructor(
    private val httpClient: HttpClient,
) : ProfileRemoteDataSource {

    override suspend fun getProfile(): CareerPilotResult<UserProfileDto, NetworkError> =
        safeApiCall {
            httpClient.get(Endpoints.PROFILE)
        }
}
