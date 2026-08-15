package com.iti.careerpilot.profile.domain.datasource.local

import com.iti.core.datastore.models.UserProfile
import kotlinx.coroutines.flow.StateFlow

interface ProfileLocalDataSource {

    val userProfile: StateFlow<UserProfile>
    suspend fun updateUserProfile(updateBlock: (UserProfile) -> UserProfile)
    suspend fun clearUserProfile()
    suspend fun saveAvatarBytes(bytes: ByteArray): String
    suspend fun saveCVBytes(bytes: ByteArray, fileName: String): String

}
