package com.iti.onboarding.data.local.datasource

import com.iti.core.datastore.models.UserProfile
import kotlinx.coroutines.flow.StateFlow
import com.iti.onboarding.domain.model.UploadedFile
import java.io.File
import android.net.Uri


interface OnboardingLocalDataSource {
    val userProfile: StateFlow<UserProfile>
    suspend fun updateUserProfile(updateBlock: (UserProfile) -> UserProfile)
    suspend fun saveAvatarUrl(file: UploadedFile)
    suspend fun savePdfUrl(file: UploadedFile)
    suspend fun moveImageToInternalStorage(sourceFile: File): String
    suspend fun moveCVToInternalStorage(sourceFile: File): String
    suspend fun uriToCacheFile(uri: Uri): File?
}