package com.iti.careerpilot.profile.data.repo

import android.util.Log
import com.iti.careerpilot.core.network.Endpoints
import com.iti.careerpilot.profile.data.mapper.toDomain
import com.iti.careerpilot.profile.domain.datasource.local.ProfileLocalDataSource
import com.iti.careerpilot.profile.domain.datasource.remote.ProfileRemoteDataSource
import com.iti.careerpilot.profile.domain.repo.ProfileRepo
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import com.iti.common.result.mapToEmptyResult
import com.iti.common.result.onSuccess
import com.iti.core.datastore.models.UserProfile
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import javax.inject.Inject

private const val TAG = "CareerPilot: ProfileRepo"

class ProfileRepoImpl @Inject constructor(
    private val localDataSource: ProfileLocalDataSource,
    private val remoteDataSource: ProfileRemoteDataSource,
) : ProfileRepo {

    override val userProfile: StateFlow<UserProfile> = localDataSource.userProfile

    override suspend fun refreshProfile(): CareerPilotResult<Unit, NetworkError> =
        remoteDataSource.getProfile()
            .onSuccess { response ->
                // Capture the current local state BEFORE the update so we know
                // which files are already on-device.
                val current = localDataSource.userProfile.value

                localDataSource.updateUserProfile { current ->
                    response.toDomain(current)
                }

                // Use URLs from the DTO directly — avoids reading the StateFlow
                // whose .value may not have propagated the write yet.
                downloadMissingFiles(
                    avatarUrl = response.avatarUrl.orEmpty(),
                    currentAvatarLocalUri = current.avatar.avatarLocalUri,
                    cvUrl = response.cvUrl.orEmpty(),
                    currentCvLocalUri = current.cv.cvLocalUri,
                    currentCvFileName = current.cv.cvFileName,
                )
            }
            .mapToEmptyResult()

    override suspend fun clearUserProfile() {
        localDataSource.clearUserProfile()
    }

    override suspend fun downloadMissingFiles() {
        val profile = localDataSource.userProfile.value
        downloadMissingFiles(
            avatarUrl = profile.avatar.avatarUrl,
            currentAvatarLocalUri = profile.avatar.avatarLocalUri,
            cvUrl = profile.cv.cvUrl,
            currentCvLocalUri = profile.cv.cvLocalUri,
            currentCvFileName = profile.cv.cvFileName,
        )
    }

    // ---------------------------------------------------------------------------
    // Private helpers
    // ---------------------------------------------------------------------------

    /** Resolves a potentially relative server path to a full URL. */
    private fun String.toFullUrl(): String =
        if (startsWith("http://") || startsWith("https://")) this
        else "${Endpoints.BASE_URL}$this"

    /** Returns true when the local URI is blank or the file it points to is gone. */
    private fun isLocalFileMissing(localUri: String): Boolean {
        if (localUri.isBlank()) return true
        val path = localUri.removePrefix("file://")
        return !File(path).exists()
    }

    /**
     * Downloads avatar and CV from the server if they are not already cached
     * on-device. URLs come from the freshly-fetched DTO; local URIs come from
     * the DataStore snapshot captured before the update to avoid any StateFlow
     * propagation race condition.
     */
    private suspend fun downloadMissingFiles(
        avatarUrl: String,
        currentAvatarLocalUri: String,
        cvUrl: String,
        currentCvLocalUri: String,
        currentCvFileName: String,
    ) {
        // --- Avatar ---
        if (avatarUrl.isNotBlank() && isLocalFileMissing(currentAvatarLocalUri)) {
            try {
                val bytes = remoteDataSource.downloadBytes(avatarUrl.toFullUrl())
                if (bytes != null) {
                    val localUri = localDataSource.saveAvatarBytes(bytes)
                    localDataSource.updateUserProfile {
                        it.copy(
                            avatar = it.avatar.copy(
                                avatarLocalUri = localUri,
                                avatarSizeBytes = bytes.size.toLong()
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to download avatar: ${e.localizedMessage}")
            }
        }

        // --- CV ---
        if (cvUrl.isNotBlank() && isLocalFileMissing(currentCvLocalUri)) {
            try {
                val bytes = remoteDataSource.downloadBytes(cvUrl.toFullUrl())
                if (bytes != null) {
                    val fileName = currentCvFileName.ifBlank {
                        cvUrl.substringAfterLast('/').ifBlank { "cv.pdf" }
                    }
                    val localUri = localDataSource.saveCVBytes(bytes, fileName)
                    localDataSource.updateUserProfile {
                        it.copy(
                            cv = it.cv.copy(
                                cvLocalUri = localUri,
                                cvFileName = it.cv.cvFileName.ifBlank { fileName },
                                cvSizeBytes = bytes.size.toLong()
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to download CV: ${e.localizedMessage}")
            }
        }
    }
}
