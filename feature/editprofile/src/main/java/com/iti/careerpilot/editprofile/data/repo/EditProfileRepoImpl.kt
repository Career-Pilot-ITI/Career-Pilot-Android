package com.iti.careerpilot.editprofile.data.repo

import android.net.Uri
import android.util.Log
import com.iti.careerpilot.core.network.Endpoints
import com.iti.careerpilot.editprofile.data.datasource.remote.mapper.toDomain
import com.iti.careerpilot.editprofile.data.datasource.remote.mapper.toDto
import com.iti.careerpilot.editprofile.data.datasource.remote.models.FileUploadResponse
import com.iti.careerpilot.editprofile.domain.datasource.local.EditProfileLocalDataSource
import com.iti.careerpilot.editprofile.domain.datasource.local.ImageCompressor
import com.iti.careerpilot.editprofile.domain.datasource.remote.EditProfileRemoteDataSource
import com.iti.careerpilot.editprofile.domain.models.RequestProfileUpdate
import com.iti.careerpilot.editprofile.domain.repo.EditProfileRepo
import com.iti.core.model.Track
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import com.iti.common.result.map
import com.iti.common.result.mapToEmptyResult
import com.iti.common.result.onError
import com.iti.common.result.onSuccess
import com.iti.core.datastore.models.UserProfile
import kotlinx.coroutines.flow.StateFlow
import java.io.File
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

    override suspend fun uploadAndAnalyzeCV(
        uri: Uri,
        onUploadProgress: (Int) -> Unit,
        onAnalysisStarted: () -> Unit,
    ): CareerPilotResult<FileUploadResponse, NetworkError> {
        val file = localDataSource.uriToCacheFile(uri)
            ?: return CareerPilotResult.Error(NetworkError.UNKNOWN)

        return when (val uploadResult = remoteDataSource.uploadCV(file, onUploadProgress)) {
            is CareerPilotResult.Error -> uploadResult
            is CareerPilotResult.Success -> {
                onAnalysisStarted()
                when (val analysisResult = remoteDataSource.analyzeCV(file)) {
                    is CareerPilotResult.Error -> analysisResult
                    is CareerPilotResult.Success -> {
                        try {
                            val localCvUri = localDataSource.moveCVToInternalStorage(file)
                            updateProfileFromCvAnalysis(
                                analyzed = analysisResult.data,
                                uploaded = uploadResult.data,
                                localCvUri = localCvUri,
                            )
                            CareerPilotResult.Success(uploadResult.data)
                        } catch (exception: Exception) {
                            Log.e(
                                "CareerPilot: EditProfile",
                                "Failed to persist analyzed CV locally",
                                exception,
                            )
                            CareerPilotResult.Error(NetworkError.UNKNOWN)
                        }
                    }
                }
            }
        }
    }

    override suspend fun ensureCvAvailableLocally() {
        runCatching {
            var profile = localDataSource.userProfile.value
            if (profile.cv.cvUrl.isBlank() && profile.id != 0L) {
                remoteDataSource.getProfile().onSuccess { response ->
                    val refreshedCvUrl = response.cvUrl.orEmpty()
                    localDataSource.updateUserProfile { current ->
                        current.copy(
                            cv = current.cv.copy(
                                cvUrl = refreshedCvUrl.ifBlank { current.cv.cvUrl },
                            ),
                        )
                    }
                }
                profile = localDataSource.userProfile.value
            }
            val cvUrl = profile.cv.cvUrl
            if (cvUrl.isBlank() || !isLocalFileMissing(profile.cv.cvLocalUri)) return

            val bytes = remoteDataSource.downloadBytes(cvUrl.toFullUrl()) ?: return
            val fileName = profile.cv.cvFileName.ifBlank {
                cvUrl.substringBefore('?').substringAfterLast('/').ifBlank { "cv.pdf" }
            }
            val localUri = localDataSource.saveCVBytes(bytes, fileName)
            localDataSource.updateUserProfile { current ->
                current.copy(
                    cv = current.cv.copy(
                        cvLocalUri = localUri,
                        cvFileName = current.cv.cvFileName.ifBlank { fileName },
                        cvSizeBytes = bytes.size.toLong(),
                    )
                )
            }
        }.onFailure {
            Log.w("CareerPilot: EditProfile", "Failed to restore CV locally: ${it.localizedMessage}")
        }
    }

    override suspend fun getTracks(): CareerPilotResult<List<Track>, NetworkError> {
        return remoteDataSource.getTracks().map { list ->
            list.map { it.toDomain() }
        }
    }

    private suspend fun updateProfileFromCvAnalysis(
        analyzed: com.iti.careerpilot.editprofile.data.datasource.remote.models.UserProfileDto,
        uploaded: FileUploadResponse,
        localCvUri: String,
    ) {
        localDataSource.updateUserProfile { current ->
            val parsed = analyzed.toDomain(current)
            current.copy(
                account = current.account.copy(
                    username = parsed.account.username.ifBlank { current.account.username },
                    email = parsed.account.email.ifBlank { current.account.email },
                    timezone = parsed.account.timezone.ifBlank { current.account.timezone },
                    termsAccepted = parsed.account.termsAccepted,
                    subscriptionTier = parsed.account.subscriptionTier.ifBlank { current.account.subscriptionTier },
                    coinBalance = if (parsed.account.coinBalance > 0) parsed.account.coinBalance else current.account.coinBalance,
                ),
                personal = current.personal.copy(
                    phoneNumber = parsed.personal.phoneNumber.ifBlank { current.personal.phoneNumber },
                    displayName = parsed.personal.displayName.ifBlank { current.personal.displayName },
                    gender = parsed.personal.gender.ifBlank { current.personal.gender },
                    dateOfBirth = parsed.personal.dateOfBirth.ifBlank { current.personal.dateOfBirth },
                ),
                career = current.career.copy(
                    targetRole = parsed.career.targetRole.ifBlank { current.career.targetRole },
                    industry = parsed.career.industry.ifBlank { current.career.industry },
                    experienceLevel = parsed.career.experienceLevel.ifBlank { current.career.experienceLevel },
                    currentJobTitle = parsed.career.currentJobTitle.ifBlank { current.career.currentJobTitle },
                    yearsOfExperience = if (parsed.career.yearsOfExperience > 0) parsed.career.yearsOfExperience else current.career.yearsOfExperience,
                    skills = if (parsed.career.skills.isNotEmpty()) {
                        (current.career.skills + parsed.career.skills).distinct()
                    } else {
                        current.career.skills
                    },
                    targetCompanies = if (parsed.career.targetCompanies.isNotEmpty()) parsed.career.targetCompanies else current.career.targetCompanies,
                    educationLevel = parsed.career.educationLevel.ifBlank { current.career.educationLevel },
                    trackName = parsed.career.trackName.ifBlank { current.career.trackName },
                    trackId = parsed.career.trackId ?: current.career.trackId,
                ),
                avatar = current.avatar.copy(
                    avatarUrl = parsed.avatar.avatarUrl.ifBlank { current.avatar.avatarUrl },
                ),
                cv = current.cv.copy(
                    cvUrl = uploaded.url.ifBlank { parsed.cv.cvUrl.ifBlank { current.cv.cvUrl } },
                    cvLocalUri = localCvUri,
                    cvFileName = uploaded.originalName.ifBlank { current.cv.cvFileName },
                    cvSizeBytes = if (uploaded.sizeBytes > 0) uploaded.sizeBytes else current.cv.cvSizeBytes,
                ),
                onboardingCompleted = parsed.onboardingCompleted ?: current.onboardingCompleted,
            )
        }
    }

    private fun isLocalFileMissing(localUri: String): Boolean {
        if (localUri.isBlank()) return true
        if (!localUri.startsWith("file://")) return true
        return !File(localUri.removePrefix("file://")).exists()
    }

    private fun String.toFullUrl(): String =
        if (startsWith("http://") || startsWith("https://")) this
        else "${Endpoints.BASE_URL.trimEnd('/')}/${trimStart('/')}"

}
