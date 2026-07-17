package com.iti.careerpilot.editprofile.domain.datasource.local

import com.iti.core.datastore.models.UserProfile
import kotlinx.coroutines.flow.Flow
import java.io.File

interface EditProfileLocalDataSource {

    val userProfile: Flow<UserProfile>

    suspend fun updateUserProfile(updateBlock: (UserProfile) -> UserProfile)

    suspend fun moveImageToInternalStorage(
        sourceFile: File
    ): String

    suspend fun uriToCacheFile(uri: android.net.Uri): File?

    suspend fun moveCVToInternalStorage(
        sourceFile: File
    ): String
}