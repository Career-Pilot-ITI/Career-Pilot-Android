package com.iti.careerpilot.profile.domain.repo

import com.iti.core.datastore.models.UserProfile
import kotlinx.coroutines.flow.StateFlow

interface ProfileRepo {

    val userProfile: StateFlow<UserProfile>
    suspend fun clearUserProfile()
}