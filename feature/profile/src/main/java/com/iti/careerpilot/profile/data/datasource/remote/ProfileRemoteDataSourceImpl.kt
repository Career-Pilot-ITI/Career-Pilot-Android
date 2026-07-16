package com.iti.careerpilot.profile.data.datasource.remote

import com.iti.careerpilot.core.network.BuildConfig
import com.iti.careerpilot.core.safecall.safeApiCall
import com.iti.careerpilot.profile.data.datasource.remote.models.RequestProfileUpdateDto
import com.iti.careerpilot.profile.data.datasource.remote.models.UpdateProfileResponseDto
import com.iti.careerpilot.profile.domain.datasource.remote.ProfileRemoteDataSource
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import io.ktor.client.HttpClient
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import javax.inject.Inject

class ProfileRemoteDataSourceImpl @Inject constructor(
    private val httpClient: HttpClient
): ProfileRemoteDataSource {

    override suspend fun updateProfile(
        request: RequestProfileUpdateDto
    ): CareerPilotResult<UpdateProfileResponseDto, NetworkError> {
        return safeApiCall<UpdateProfileResponseDto> {
            httpClient.patch("${BuildConfig.BASE_URL}api/v1/auth/profile") {
                setBody(request)
            }
        }
    }

}