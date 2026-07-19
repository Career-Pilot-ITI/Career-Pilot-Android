package com.iti.onboarding.data.repository

import android.util.Log
import com.iti.careerpilot.core.network.model.UpdateProfileRequestDto
import com.iti.careerpilot.core.network.model.UserResponseDto
import com.iti.common.dispatcher.CareerPilotDispatchers.IO
import com.iti.common.dispatcher.Dispatcher
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import com.iti.core.model.PdfFile
import com.iti.onboarding.data.local.datasource.OnboardingLocalDataSource
import com.iti.onboarding.data.mapper.toDomain
import com.iti.onboarding.data.remote.datasource.OnboardingRemoteDataSource
import com.iti.onboarding.domain.model.FileUploadData
import com.iti.onboarding.domain.model.Track
import com.iti.onboarding.domain.model.UploadedFile
import com.iti.onboarding.domain.repository.OnboardingRepository
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import java.nio.channels.UnresolvedAddressException
import javax.inject.Inject

class OnboardingRepositoryImpl @Inject constructor(
    private val remoteDataSource: OnboardingRemoteDataSource,
    private val localDataSource: OnboardingLocalDataSource,
    @param:Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
) : OnboardingRepository {

    override suspend fun uploadFile(
        fileData: FileUploadData,
    ): CareerPilotResult<UploadedFile, NetworkError> =
        safeNetworkCall {
            remoteDataSource
                .uploadFile(fileData)
                .toDomain()
        }

    override suspend fun saveAvatarUrl(url: String) {
        withContext(ioDispatcher) {
            localDataSource.saveAvatarUrl(url)
        }
    }

    override suspend fun updateProfile(
        request: UpdateProfileRequestDto,
    ): CareerPilotResult<UserResponseDto, NetworkError> =
        safeNetworkCall {
            remoteDataSource.updateProfile(request)
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
        document: PdfFile,
    ): CareerPilotResult<Unit, NetworkError> =
        safeNetworkCall {
            val response = remoteDataSource.uploadCv(
                document = document,
            )

            localDataSource.savePdfUrl(response.url)
        }

    override suspend fun updateProfileTrack(
        trackId: Int,
    ): CareerPilotResult<Unit, NetworkError> =
        safeNetworkCall {
            remoteDataSource.updateProfile(
                request = UpdateProfileRequestDto(
                    trackId = trackId,
                ),
            )
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

                CareerPilotResult.Error(
                    error = NetworkError.BAD_REQUEST,
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