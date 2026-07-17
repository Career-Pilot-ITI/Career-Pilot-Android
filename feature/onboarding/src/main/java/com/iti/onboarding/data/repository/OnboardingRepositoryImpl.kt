package com.iti.onboarding.data.repository

import com.iti.careerpilot.core.network.model.UpdateProfileRequestDto
import com.iti.careerpilot.core.network.model.UserResponseDto
import com.iti.common.dispatcher.CareerPilotDispatchers.IO
import com.iti.common.dispatcher.Dispatcher
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import com.iti.onboarding.data.mapper.toDomain
import com.iti.onboarding.domain.model.UploadedFile
import com.iti.onboarding.domain.model.FileUploadData
import com.iti.onboarding.data.local.datasource.OnboardingLocalDataSource
import com.iti.onboarding.data.remote.datasource.OnboardingRemoteDataSource
import com.iti.onboarding.domain.error.TracksScreenError
import com.iti.onboarding.domain.model.Track
import com.iti.onboarding.domain.repository.OnboardingRepository
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
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
        } catch (e: io.ktor.client.plugins.ClientRequestException) {
            CareerPilotResult.Error(NetworkError.BAD_REQUEST)
        } catch (e: io.ktor.client.plugins.ServerResponseException) {
            CareerPilotResult.Error(NetworkError.SERVER)
        } catch (e: kotlinx.serialization.SerializationException) {
            CareerPilotResult.Error(NetworkError.SERIALIZATION)
        } catch (e: java.nio.channels.UnresolvedAddressException) {
            CareerPilotResult.Error(NetworkError.NO_INTERNET)
        } catch (e: Exception) {
            CareerPilotResult.Error(NetworkError.UNKNOWN)
        }
    }

    override suspend fun saveAvatarUrl(url: String) {
        localDataSource.saveAvatarUrl(url)
    }

    override suspend fun updateProfile(request: UpdateProfileRequestDto): CareerPilotResult<UserResponseDto, NetworkError> = withContext(ioDispatcher) {
        try {
            val response = remoteDataSource.updateProfile(request)
            CareerPilotResult.Success(response)
        } catch (e: io.ktor.client.plugins.ClientRequestException) {
            android.util.Log.e("ProfileError", "HTTP Status: ${e.response.status}, Body: ${e.response.bodyAsText()}")
            CareerPilotResult.Error(NetworkError.BAD_REQUEST)
        } catch (e: io.ktor.client.plugins.ServerResponseException) {
            CareerPilotResult.Error(NetworkError.SERVER)
        } catch (e: kotlinx.serialization.SerializationException) {
            CareerPilotResult.Error(NetworkError.SERIALIZATION)
        } catch (e: java.nio.channels.UnresolvedAddressException) {
            CareerPilotResult.Error(NetworkError.NO_INTERNET)
        } catch (e: Exception) {
            CareerPilotResult.Error(NetworkError.UNKNOWN)
        }
    }

    override suspend fun getTracks(): CareerPilotResult<List<Track>, TracksScreenError> {
        return remoteDataSource.getTracks()
    }
}