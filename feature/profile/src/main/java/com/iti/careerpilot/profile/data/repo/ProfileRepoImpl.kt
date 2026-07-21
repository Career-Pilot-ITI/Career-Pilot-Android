package com.iti.careerpilot.profile.data.repo

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
import javax.inject.Inject

class ProfileRepoImpl @Inject constructor(
    private val localDataSource: ProfileLocalDataSource,
    private val remoteDataSource: ProfileRemoteDataSource,
): ProfileRepo {

    override val userProfile: StateFlow<UserProfile> = localDataSource.userProfile

    override suspend fun refreshProfile(): CareerPilotResult<Unit, NetworkError> =
        remoteDataSource.getProfile()
            .onSuccess { response ->
                localDataSource.updateUserProfile { current ->
                    response.toDomain(current)
                }
            }
            .mapToEmptyResult()

    override suspend fun clearUserProfile() {
        localDataSource.clearUserProfile()
    }

}
