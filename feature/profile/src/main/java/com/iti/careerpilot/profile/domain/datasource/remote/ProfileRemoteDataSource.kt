package com.iti.careerpilot.profile.domain.datasource.remote

import com.iti.careerpilot.profile.data.datasource.remote.models.RequestProfileUpdateDto
import com.iti.careerpilot.profile.data.datasource.remote.models.UpdateProfileResponseDto
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult

interface ProfileRemoteDataSource {

    suspend fun updateProfile(
        request: RequestProfileUpdateDto
    ): CareerPilotResult<UpdateProfileResponseDto, NetworkError>

}