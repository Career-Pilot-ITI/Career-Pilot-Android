package com.iti.careerpilot.profile.domain.repo

import com.iti.careerpilot.profile.domain.models.RequestProfileUpdate
import com.iti.core.datastore.models.UserProfile
import kotlinx.coroutines.flow.Flow

interface ProfileRepo {

    val userProfile: Flow<UserProfile>

    suspend fun updateProfile(
        request: RequestProfileUpdate
    )
}