package com.iti.careerpilot.editprofile.domain.datasource.local

import com.iti.core.datastore.models.UserProfile
import kotlinx.coroutines.flow.Flow

interface EditProfileLocalDataSource {

    val userProfile: Flow<UserProfile>

    suspend fun updateUserProfile(updateBlock: (UserProfile) -> UserProfile)

}