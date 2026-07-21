package com.iti.careerpilot.profile.domain.repo

import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import com.iti.core.datastore.models.UserProfile
import kotlinx.coroutines.flow.StateFlow

interface ProfileRepo {

    val userProfile: StateFlow<UserProfile>
    suspend fun refreshProfile(): CareerPilotResult<Unit, NetworkError>
    suspend fun clearUserProfile()
}
