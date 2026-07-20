package com.iti.onboarding.data.local.datasource

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.net.toUri
import com.iti.core.datastore.UserTokensRepo
import com.iti.core.datastore.models.UserProfile
import com.iti.core.datastore.repo.UserProfileRepo
import com.iti.onboarding.domain.model.UploadedFile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class OnboardingLocalDataSourceImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val preferencesDataSource: UserTokensRepo,
    private val userProfileRepo: UserProfileRepo
): OnboardingLocalDataSource {

    private companion object {
        const val AVATAR_DIR = "avatars"
        const val CV_DIR = "cvs"
    }

    override val userProfile: StateFlow<UserProfile> = userProfileRepo.userProfile

    override suspend fun updateUserProfile(updateBlock: (UserProfile) -> UserProfile) {
        userProfileRepo.updateUserProfile(updateBlock)
    }

    override suspend fun saveAvatarUrl(file: UploadedFile) {
        userProfileRepo.updateUserProfile {
            it.copy(
                avatar = it.avatar.copy(
                    avatarUrl = file.url,
                    avatarSizeBytes = file.sizeBytes
                )
            )
        }
    }

    override suspend fun savePdfUrl(file: UploadedFile) {
        userProfileRepo.updateUserProfile {
            it.copy(
                cv = it.cv.copy(
                    cvUrl = file.url,
                    cvFileName = file.originalName,
                    cvSizeBytes = file.sizeBytes
                )
            )
        }
    }

    override suspend fun moveImageToInternalStorage(sourceFile: File): String {
        return moveFileToInternalStorage(sourceFile, AVATAR_DIR)
    }

    override suspend fun moveCVToInternalStorage(sourceFile: File): String {
        return moveFileToInternalStorage(sourceFile, CV_DIR)
    }

    private suspend fun moveFileToInternalStorage(
        sourceFile: File,
        targetDir: String
    ): String {
        return withContext(Dispatchers.IO) {
            val directory = File(context.filesDir, targetDir).apply {
                if (!exists()) mkdirs()
            }
            directory.listFiles()?.forEach { it.delete() }
            val targetFile = File(directory, sourceFile.name)
            val moved = sourceFile.renameTo(targetFile)
            if (!moved) {
                sourceFile.copyTo(targetFile, overwrite = true)
            }
            targetFile.toUri().toString()
        }
    }

    override suspend fun uriToCacheFile(uri: Uri): File? {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
                val file: File = getFileName(uri)?.let { fileName ->
                    File(context.cacheDir, fileName)
                } ?: File.createTempFile("temp_file", ".tmp", context.cacheDir)
                file.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
                file
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun getFileName(uri: Uri): String? {
        var name: String? = null
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    name = it.getString(nameIndex)
                }
            }
        }
        return name
    }
}
