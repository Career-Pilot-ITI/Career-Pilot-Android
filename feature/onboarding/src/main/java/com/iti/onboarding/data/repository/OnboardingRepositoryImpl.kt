package com.iti.onboarding.data.repository

import android.net.Uri
import android.util.Log
import com.iti.careerpilot.core.network.model.UpdateProfileRequestDto
import com.iti.careerpilot.core.network.model.UserProfileDto
import com.iti.common.dispatcher.CareerPilotDispatchers.IO
import com.iti.common.dispatcher.Dispatcher
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import com.iti.common.result.onSuccess
import com.iti.onboarding.data.local.datasource.OnboardingLocalDataSource
import com.iti.onboarding.data.mapper.toDomain
import com.iti.onboarding.data.remote.datasource.OnboardingRemoteDataSource
import com.iti.core.datastore.models.UserProfile
import com.iti.onboarding.domain.model.Track
import com.iti.onboarding.domain.model.UploadedFile
import com.iti.onboarding.domain.repository.OnboardingRepository
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import java.nio.channels.UnresolvedAddressException
import javax.inject.Inject
import com.iti.common.media.ImageCompressor

class OnboardingRepositoryImpl @Inject constructor(
    private val remoteDataSource: OnboardingRemoteDataSource,
    private val localDataSource: OnboardingLocalDataSource,
    private val imageCompressor: ImageCompressor,
    @param:Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
) : OnboardingRepository {

    override val userProfile: StateFlow<UserProfile> = localDataSource.userProfile

    override suspend fun uploadFile(
        uri: Uri,
        onProgress: (Int) -> Unit
    ): CareerPilotResult<UploadedFile, NetworkError> {
        val file = imageCompressor.compressImage(uri)
        return safeNetworkCall {
            remoteDataSource.uploadFile(file, onProgress).toDomain()
        }.onSuccess { uploaded ->
            val localImageUri = localDataSource.moveImageToInternalStorage(file)
            localDataSource.updateUserProfile {
                it.copy(
                    avatar = it.avatar.copy(
                        avatarUrl = uploaded.url,
                        avatarLocalUri = localImageUri,
                        avatarSizeBytes = uploaded.sizeBytes
                    )
                )
            }
        }
    }

    override suspend fun saveAvatarUrl(file: UploadedFile) {
        withContext(ioDispatcher) {
            localDataSource.saveAvatarUrl(file)
        }
    }

    override suspend fun updateProfile(
        request: UpdateProfileRequestDto,
    ): CareerPilotResult<UserProfileDto, NetworkError> =
        safeNetworkCall {
            val response = remoteDataSource.updateProfile(request)
            updateLocalProfile(response)
            response
        }

    override suspend fun getTracks(): CareerPilotResult<List<Track>, NetworkError> =
        safeNetworkCall {
            remoteDataSource
                .getTracks()
                .map { trackDto ->
                    trackDto.toDomain()
                }
        }

    override suspend fun uploadCv(
        uri: Uri,
        onProgress: (Int) -> Unit
    ): CareerPilotResult<UploadedFile, NetworkError> {
        val file = localDataSource.uriToCacheFile(uri)
        return file?.let {
            safeNetworkCall {
                remoteDataSource.uploadCv(file, onProgress).toDomain()
            }.onSuccess { uploaded ->
                val localCvUri = localDataSource.moveCVToInternalStorage(file)
                localDataSource.updateUserProfile {
                    it.copy(
                        cv = it.cv.copy(
                            cvUrl = uploaded.url,
                            cvLocalUri = localCvUri,
                            cvFileName = uploaded.originalName,
                            cvSizeBytes = uploaded.sizeBytes
                        )
                    )
                }
            }
        } ?: CareerPilotResult.Error(NetworkError.UNKNOWN)
    }

    override suspend fun analyzeCv(
        uri: Uri,
        onProgress: (Int) -> Unit
    ): CareerPilotResult<UserProfile, NetworkError> {
        val file = localDataSource.uriToCacheFile(uri)
        return file?.let {
            safeNetworkCall {
                val response = remoteDataSource.analyzeCv(file, onProgress)
                val localCvUri = localDataSource.moveCVToInternalStorage(file)
                updateLocalProfile(response)
                localDataSource.updateUserProfile { current ->
                    current.copy(
                        cv = current.cv.copy(
                            cvLocalUri = localCvUri,
                            cvFileName = file.name,
                            cvSizeBytes = file.length()
                        )
                    )
                }
                response.toDomain()
            }
        } ?: CareerPilotResult.Error(NetworkError.UNKNOWN)
    }

    override suspend fun updateProfileTrack(
        trackId: Long,
    ): CareerPilotResult<Unit, NetworkError> =
        safeNetworkCall {
            val response = remoteDataSource.updateProfile(
                request = UpdateProfileRequestDto(
                    trackId = trackId,
                ),
            )
            updateLocalProfile(response)
        }

    override suspend fun completeOnboarding(
        cvFileId: Long?,
    ): CareerPilotResult<Unit, NetworkError> =
        safeNetworkCall {
            val response = remoteDataSource.updateProfile(
                request = UpdateProfileRequestDto(
                    cvFileId = cvFileId,
                    onboardingCompleted = true,
                ),
            )
            updateLocalProfile(response)
        }

    private suspend fun updateLocalProfile(response: UserProfileDto) {
        localDataSource.updateUserProfile { current ->
            val newProfile = response.toDomain()
            current.copy(
                account = current.account.copy(
                    username = newProfile.account.username.ifBlank { current.account.username },
                    email = newProfile.account.email.ifBlank { current.account.email },
                    timezone = newProfile.account.timezone.ifBlank { current.account.timezone },
                    termsAccepted = newProfile.account.termsAccepted,
                    subscriptionTier = newProfile.account.subscriptionTier.ifBlank { current.account.subscriptionTier },
                    coinBalance = if (newProfile.account.coinBalance > 0) newProfile.account.coinBalance else current.account.coinBalance
                ),
                personal = current.personal.copy(
                    phoneNumber = newProfile.personal.phoneNumber.ifBlank { current.personal.phoneNumber },
                    displayName = newProfile.personal.displayName.ifBlank { current.personal.displayName },
                    gender = newProfile.personal.gender.ifBlank { current.personal.gender },
                    dateOfBirth = newProfile.personal.dateOfBirth.ifBlank { current.personal.dateOfBirth }
                ),
                career = current.career.copy(
                    targetRole = newProfile.career.targetRole.ifBlank { current.career.targetRole },
                    industry = newProfile.career.industry.ifBlank { current.career.industry },
                    experienceLevel = newProfile.career.experienceLevel.ifBlank { current.career.experienceLevel },
                    currentJobTitle = newProfile.career.currentJobTitle.ifBlank { current.career.currentJobTitle },
                    yearsOfExperience = if (newProfile.career.yearsOfExperience > 0) newProfile.career.yearsOfExperience else current.career.yearsOfExperience,
                    skills = if (newProfile.career.skills.isNotEmpty()) (current.career.skills + newProfile.career.skills).distinct() else current.career.skills,
                    targetCompanies = if (newProfile.career.targetCompanies.isNotEmpty()) newProfile.career.targetCompanies else current.career.targetCompanies,
                    educationLevel = newProfile.career.educationLevel.ifBlank { current.career.educationLevel },
                    trackName = newProfile.career.trackName.ifBlank { current.career.trackName }
                ),
                avatar = current.avatar.copy(
                    avatarUrl = newProfile.avatar.avatarUrl.ifBlank { current.avatar.avatarUrl }
                ),
                cv = current.cv.copy(
                    cvUrl = newProfile.cv.cvUrl.ifBlank { current.cv.cvUrl }
                ),
                onboardingCompleted = newProfile.onboardingCompleted ?: current.onboardingCompleted
            )
        }
    }

    private suspend fun <T> safeNetworkCall(
        block: suspend () -> T,
    ): CareerPilotResult<T, NetworkError> =
        withContext(ioDispatcher) {
            try {
                CareerPilotResult.Success(
                    data = block(),
                )
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (exception: ClientRequestException) {
                logClientRequestException(exception)

                val error = when (exception.response.status) {
                    HttpStatusCode.Unauthorized -> NetworkError.UNAUTHORIZED
                    HttpStatusCode.Forbidden -> NetworkError.FORBIDDEN
                    else -> NetworkError.BAD_REQUEST
                }

                CareerPilotResult.Error(
                    error = error,
                )
            } catch (_: ServerResponseException) {
                CareerPilotResult.Error(
                    error = NetworkError.SERVER,
                )
            } catch (_: SerializationException) {
                CareerPilotResult.Error(
                    error = NetworkError.SERIALIZATION,
                )
            } catch (_: UnresolvedAddressException) {
                CareerPilotResult.Error(
                    error = NetworkError.NO_INTERNET,
                )
            } catch (exception: Exception) {
                Log.e(
                    TAG,
                    "Unexpected onboarding repository error",
                    exception,
                )

                CareerPilotResult.Error(
                    error = NetworkError.UNKNOWN,
                )
            }
        }

    private suspend fun logClientRequestException(
        exception: ClientRequestException,
    ) {
        val responseBody = runCatching {
            exception.response.bodyAsText()
        }.getOrNull()

        Log.e(
            TAG,
            buildString {
                append("Client request failed. ")
                append("Status: ${exception.response.status}")

                if (!responseBody.isNullOrBlank()) {
                    append(", Body: $responseBody")
                }
            },
            exception,
        )
    }

    private companion object {
        const val TAG = "OnboardingRepository"
    }
}
