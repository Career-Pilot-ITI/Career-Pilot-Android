package com.iti.careerpilot.editprofile.data.datasource.local

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.core.net.toUri
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.iti.careerpilot.editprofile.domain.datasource.local.EditProfileLocalDataSource
import com.iti.core.datastore.models.UserProfile
import com.iti.core.datastore.repo.UserProfileRepo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import javax.inject.Inject

private const val ERROR_TAG = "CareerPilot: EditProfileLocalDataSource"

class EditProfileLocalDataSourceImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val userProfileRepo: UserProfileRepo,
): EditProfileLocalDataSource {

    companion object {
        private const val AVATAR_DIR = "avatars"
        private const val CV_DIR = "cvs"
    }

    override val userProfile: StateFlow<UserProfile> = userProfileRepo.userProfile

    override suspend fun updateUserProfile(updateBlock: (UserProfile) -> UserProfile) {
        userProfileRepo.updateUserProfile(updateBlock)
    }

    override suspend fun moveImageToInternalStorage(
        sourceFile: File
    ): String {
        return moveFileToInternalStorage(sourceFile, AVATAR_DIR)
    }

    override suspend fun moveCVToInternalStorage(sourceFile: File): String {
        return moveFileToInternalStorage(sourceFile, CV_DIR)
    }

    suspend fun moveFileToInternalStorage(
        sourceFile: File,
        targetDir: String
    ): String {
        return withContext(Dispatchers.IO) {
            val targetDir = File(context.filesDir, targetDir).apply {
                if (!exists()) mkdirs()
            }
            targetDir.listFiles()?.forEach { it.delete() }
            val targetFile = File(targetDir, sourceFile.name)
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
                val fileName = getFileName(uri) ?: "temp_file"
                val file = File(context.cacheDir, fileName)
                file.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
                file
            } catch (e: Exception) {
                Log.e(ERROR_TAG, "Failed to convert uri to file: ${e.localizedMessage}", e)
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