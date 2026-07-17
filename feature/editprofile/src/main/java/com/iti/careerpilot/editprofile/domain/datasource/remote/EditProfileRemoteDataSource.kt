package com.iti.careerpilot.editprofile.domain.datasource.remote


import com.iti.careerpilot.editprofile.data.datasource.remote.models.UpdateProfileRequestDto
import com.iti.careerpilot.editprofile.data.datasource.remote.models.UpdateProfileResponseDto
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult

interface EditProfileRemoteDataSource {

    suspend fun updateProfile(
        request: UpdateProfileRequestDto
    ): CareerPilotResult<UpdateProfileResponseDto, NetworkError>

}