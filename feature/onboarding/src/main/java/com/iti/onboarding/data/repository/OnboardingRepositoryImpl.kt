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
        fileData: FileUploadData
    ): CareerPilotResult<UploadedFile, NetworkError> = withContext(ioDispatcher) {
        try {
            val dto = remoteDataSource.uploadFile(fileData)
            CareerPilotResult.Success(dto.toDomain())
        } catch (e: ClientRequestException) {
            CareerPilotResult.Error(NetworkError.BAD_REQUEST)
        } catch (e: ServerResponseException) {
            CareerPilotResult.Error(NetworkError.SERVER)
        } catch (e: SerializationException) {
            CareerPilotResult.Error(NetworkError.SERIALIZATION)
        } catch (e: UnresolvedAddressException) {
            CareerPilotResult.Error(NetworkError.NO_INTERNET)
        } catch (e: Exception) {
            CareerPilotResult.Error(NetworkError.UNKNOWN)
        }
    }

    override suspend fun saveAvatarUrl(url: String) {
        localDataSource.saveAvatarUrl(url)
    }

    override suspend fun updateProfile(request: UpdateProfileRequestDto): CareerPilotResult<UserResponseDto, NetworkError> =
        withContext(ioDispatcher) {
            try {
                val response = remoteDataSource.updateProfile(request)
                CareerPilotResult.Success(response)
            } catch (e: ClientRequestException) {
                Log.e(
                    "ProfileError",
                    "HTTP Status: ${e.response.status}, Body: ${e.response.bodyAsText()}"
                )
                CareerPilotResult.Error(NetworkError.BAD_REQUEST)
            } catch (e: ServerResponseException) {
                CareerPilotResult.Error(NetworkError.SERVER)
            } catch (e: SerializationException) {
                CareerPilotResult.Error(NetworkError.SERIALIZATION)
            } catch (e: UnresolvedAddressException) {
                CareerPilotResult.Error(NetworkError.NO_INTERNET)
            } catch (e: Exception) {
                CareerPilotResult.Error(NetworkError.UNKNOWN)
            }
        }

    override suspend fun getTracks(): CareerPilotResult<List<Track>, NetworkError> {
        return try {
            val tracks = remoteDataSource.getTracks()
            CareerPilotResult.Success(tracks.map { it.toDomain() } )
        } catch (e: ClientRequestException) {
            CareerPilotResult.Error(NetworkError.BAD_REQUEST)
        } catch (e: ServerResponseException) {
            CareerPilotResult.Error(NetworkError.SERVER)
        } catch (e: SerializationException) {
            CareerPilotResult.Error(NetworkError.SERIALIZATION)
        } catch (e: UnresolvedAddressException) {
            CareerPilotResult.Error(NetworkError.NO_INTERNET)
        } catch (e: Exception) {
            CareerPilotResult.Error(NetworkError.UNKNOWN)
        }
    }

    override suspend fun uploadCv(document: PdfFile): CareerPilotResult<Unit, NetworkError> {
        return try {
            val response = remoteDataSource.uploadCv(document = document)

            localDataSource.savePdfUrl(response.url)
            CareerPilotResult.Success(Unit)
        } catch (e: ClientRequestException) {
            CareerPilotResult.Error(NetworkError.BAD_REQUEST)
        } catch (e: ServerResponseException) {
            CareerPilotResult.Error(NetworkError.SERVER)
        } catch (e: SerializationException) {
            CareerPilotResult.Error(NetworkError.SERIALIZATION)
        } catch (e: UnresolvedAddressException) {
            CareerPilotResult.Error(NetworkError.NO_INTERNET)
        } catch (e: Exception) {
            CareerPilotResult.Error(NetworkError.UNKNOWN)
        }
    }

    override suspend fun updateProfileTrack(trackId: Int): CareerPilotResult<Unit, NetworkError> {
        return try {
            updateProfile(UpdateProfileRequestDto(trackId = trackId))
            CareerPilotResult.Success(Unit)
        } catch (e: ClientRequestException) {
            CareerPilotResult.Error(NetworkError.BAD_REQUEST)
        } catch (e: ServerResponseException) {
            CareerPilotResult.Error(NetworkError.SERVER)
        } catch (e: SerializationException) {
            CareerPilotResult.Error(NetworkError.SERIALIZATION)
        } catch (e: UnresolvedAddressException) {
            CareerPilotResult.Error(NetworkError.NO_INTERNET)
        } catch (e: Exception) {
            CareerPilotResult.Error(NetworkError.UNKNOWN)
        }
    }
}