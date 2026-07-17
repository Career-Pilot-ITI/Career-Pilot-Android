package com.iti.careerpilot.editprofile.domain.repo

import com.iti.careerpilot.editprofile.domain.models.RequestProfileUpdate
import com.iti.core.datastore.models.UserProfile
import kotlinx.coroutines.flow.Flow

interface EditProfileRepo {

    val userProfile: Flow<UserProfile>

    suspend fun updateProfile(
        request: RequestProfileUpdate
    )
}