package com.iti.careerpilot.editprofile.data.datasource.remote

import com.iti.careerpilot.core.network.BuildConfig
import com.iti.careerpilot.core.safecall.safeApiCall
import com.iti.careerpilot.editprofile.data.datasource.remote.models.UpdateProfileRequestDto
import com.iti.careerpilot.editprofile.data.datasource.remote.models.UpdateProfileResponseDto
import com.iti.careerpilot.editprofile.domain.datasource.remote.EditProfileRemoteDataSource
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import io.ktor.client.HttpClient
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import javax.inject.Inject

class EditProfileRemoteDataSourceImpl @Inject constructor(
    private val httpClient: HttpClient
): EditProfileRemoteDataSource {

    override suspend fun updateProfile(
        request: UpdateProfileRequestDto
    ): CareerPilotResult<UpdateProfileResponseDto, NetworkError> {
        return safeApiCall<UpdateProfileResponseDto> {
            httpClient.patch("${BuildConfig.BASE_URL}api/v1/auth/profile") {
                setBody(request)
            }
        }
    }

}