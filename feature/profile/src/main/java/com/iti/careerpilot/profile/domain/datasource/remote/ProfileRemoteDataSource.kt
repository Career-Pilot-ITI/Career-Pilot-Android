package com.iti.careerpilot.profile.domain.datasource.remote

import com.iti.careerpilot.core.network.model.UserProfileDto
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult

interface ProfileRemoteDataSource {
    suspend fun getProfile(): CareerPilotResult<UserProfileDto, NetworkError>
    suspend fun downloadBytes(url: String): ByteArray?
}
