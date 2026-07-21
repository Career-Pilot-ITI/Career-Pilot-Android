package com.iti.core.datastore.repo

import com.iti.core.datastore.models.UserProfile
import kotlinx.coroutines.flow.StateFlow

interface UserProfileRepo {
    val userProfile: StateFlow<UserProfile>
    suspend fun readUserProfile(): UserProfile
    suspend fun updateUserProfile(updateBlock: (UserProfile) -> UserProfile)
    suspend fun clearUserProfile()
}
