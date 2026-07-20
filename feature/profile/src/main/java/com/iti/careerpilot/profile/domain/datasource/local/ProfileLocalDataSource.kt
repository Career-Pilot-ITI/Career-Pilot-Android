package com.iti.careerpilot.profile.domain.datasource.local

import com.iti.core.datastore.models.UserProfile
import kotlinx.coroutines.flow.StateFlow

interface ProfileLocalDataSource {

    val userProfile: StateFlow<UserProfile>
    suspend fun clearUserProfile()

}