package com.iti.careerpilot.editprofile.data.repo

import android.net.Uri
import android.util.Log
import com.iti.careerpilot.editprofile.data.datasource.remote.mapper.toDomain
import com.iti.careerpilot.editprofile.data.datasource.remote.mapper.toDto
import com.iti.careerpilot.editprofile.data.datasource.remote.models.FileUploadResponse
import com.iti.careerpilot.editprofile.domain.datasource.local.EditProfileLocalDataSource
import com.iti.careerpilot.editprofile.domain.datasource.local.ImageCompressor
import com.iti.careerpilot.editprofile.domain.datasource.remote.EditProfileRemoteDataSource
import com.iti.careerpilot.editprofile.domain.models.RequestProfileUpdate
import com.iti.careerpilot.editprofile.domain.repo.EditProfileRepo
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import com.iti.common.result.mapToEmptyResult
import com.iti.common.result.onError
import com.iti.common.result.onSuccess
import com.iti.core.datastore.models.UserProfile
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class EditProfileRepoImpl @Inject constructor(
    private val localDataSource: EditProfileLocalDataSource,
    private val remoteDataSource: EditProfileRemoteDataSource,
    private val imageCompressor: ImageCompressor
) : EditProfileRepo {

    override val userProfile: StateFlow<UserProfile> = localDataSource.userProfile

    override suspend fun updateProfile(
        request: RequestProfileUpdate
    ): CareerPilotResult<Unit, NetworkError> {
        return remoteDataSource.updateProfile(request.toDto())
            .onSuccess { dto ->
                val newProfile = dto.toDomain()
                localDataSource.updateUserProfile { current ->
                    newProfile.copy(
                        avatar = newProfile.avatar.copy(
                            avatarUrl = newProfile.avatar.avatarUrl.ifBlank { current.avatar.avatarUrl },
                            avatarLocalUri = current.avatar.avatarLocalUri,
                            avatarSizeBytes = current.avatar.avatarSizeBytes,
                        ),
                        cv = newProfile.cv.copy(
                            cvUrl = newProfile.cv.cvUrl.ifBlank { current.cv.cvUrl },
                            cvLocalUri = current.cv.cvLocalUri,
                            cvFileName = current.cv.cvFileName,
                            cvSizeBytes = current.cv.cvSizeBytes,
                        ),
                    )
                }
            }
            .mapToEmptyResult()
            .onError {
                Log.e("CareerPilot: updateProfile", "updateProfile: ${it.name}")
            }
    }

    override suspend fun uploadImage(
        uri: Uri,
        onProgress: (Int) -> Unit
    ): CareerPilotResult<FileUploadResponse, NetworkError> {
        val file = imageCompressor.compressToFile(uri)
        return file?.let {
            remoteDataSource.uploadImage(file, onProgress)
                .onSuccess { response ->
                    val localImageUri = localDataSource.moveImageToInternalStorage(file)
                    localDataSource.updateUserProfile {
                        it.copy(
                            avatar = it.avatar.copy(
                                avatarUrl = response.url,
                                avatarLocalUri = localImageUri,
                                avatarSizeBytes = response.sizeBytes
                            )
                        )
                    }
                }
        } ?: CareerPilotResult.Error(NetworkError.UNKNOWN)
    }

    override suspend fun uploadCV(
        uri: Uri,
        onProgress: (Int) -> Unit
    ): CareerPilotResult<FileUploadResponse, NetworkError> {
        val file = localDataSource.uriToCacheFile(uri)
        return file?.let {
            remoteDataSource.uploadCV(file, onProgress)
                .onSuccess { response ->
                    val localCvUri = localDataSource.moveCVToInternalStorage(file)
                    localDataSource.updateUserProfile {
                        it.copy(
                            cv = it.cv.copy(
                                cvUrl = response.url,
                                cvLocalUri = localCvUri,
                                cvFileName = response.originalName,
                                cvSizeBytes = response.sizeBytes
                            )
                        )
                    }
                }
        } ?: CareerPilotResult.Error(NetworkError.UNKNOWN)
    }

}
