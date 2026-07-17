package com.iti.careerpilot.editprofile.data.datasource.local

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import androidx.datastore.core.DataStore
import com.iti.careerpilot.editprofile.domain.datasource.local.EditProfileLocalDataSource
import com.iti.core.datastore.models.UserProfile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import javax.inject.Inject

private const val ERROR_TAG = "CareerPilot: EditProfileLocalDataSource"

class EditProfileLocalDataSourceImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val dataStore: DataStore<UserProfile>
): EditProfileLocalDataSource {

    companion object {
        private const val AVATAR_DIR = "avatars"
    }

    override val userProfile: Flow<UserProfile> = dataStore.data

    override suspend fun updateUserProfile(updateBlock: (UserProfile) -> UserProfile) {
        try {
            dataStore.updateData { currentData ->
                updateBlock(currentData)
            }
        } catch (e: IOException) {
            Log.e(ERROR_TAG, "Failed to update user profile: ${e.localizedMessage}", e)
        }
    }

    override suspend fun moveImageToInternalStorage(
        sourceFile: File
    ): String {
        return moveImageToInternalStorage(sourceFile, AVATAR_DIR)
    }

    suspend fun moveImageToInternalStorage(
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
}