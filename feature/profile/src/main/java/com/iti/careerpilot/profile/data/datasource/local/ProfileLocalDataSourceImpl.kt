package com.iti.careerpilot.profile.data.datasource.local

import android.content.Context
import com.iti.careerpilot.profile.domain.datasource.local.ProfileLocalDataSource
import com.iti.core.datastore.models.UserProfile
import com.iti.core.datastore.repo.UserProfileRepo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.util.UUID
import javax.inject.Inject


class ProfileLocalDataSourceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userProfileRepo: UserProfileRepo,
) : ProfileLocalDataSource {

    override val userProfile: StateFlow<UserProfile> = userProfileRepo.userProfile

    override suspend fun updateUserProfile(updateBlock: (UserProfile) -> UserProfile) {
        userProfileRepo.updateUserProfile(updateBlock)
    }

    override suspend fun clearUserProfile() {
        userProfileRepo.clearUserProfile()
    }

    override suspend fun saveAvatarBytes(bytes: ByteArray): String {
        val dir = File(context.filesDir, "avatars").also { it.mkdirs() }
        val file = File(dir, "${UUID.randomUUID()}.jpg")
        file.writeBytes(bytes)
        return "file://${file.absolutePath}"
    }

    override suspend fun saveCVBytes(bytes: ByteArray, fileName: String): String {
        val dir = File(context.filesDir, "cvs").also { it.mkdirs() }
        val safeFileName = fileName.ifBlank { "cv_${UUID.randomUUID()}.pdf" }
        val file = File(dir, safeFileName)
        file.writeBytes(bytes)
        return "file://${file.absolutePath}"
    }
}