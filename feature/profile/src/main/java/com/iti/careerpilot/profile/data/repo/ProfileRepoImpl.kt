package com.iti.careerpilot.profile.data.repo

import android.util.Log
import com.iti.careerpilot.profile.data.datasource.remote.mapper.toDomain
import com.iti.careerpilot.profile.data.datasource.remote.mapper.toDto
import com.iti.careerpilot.profile.domain.datasource.local.ProfileLocalDataSource
import com.iti.careerpilot.profile.domain.datasource.remote.ProfileRemoteDataSource
import com.iti.careerpilot.profile.domain.models.RequestProfileUpdate
import com.iti.careerpilot.profile.domain.repo.ProfileRepo
import com.iti.common.result.onError
import com.iti.common.result.onSuccess
import com.iti.core.datastore.models.UserProfile
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ProfileRepoImpl @Inject constructor(
    private val localDataSource: ProfileLocalDataSource,
    private val remoteDataSource: ProfileRemoteDataSource,
): ProfileRepo {

    override val userProfile: Flow<UserProfile> = localDataSource.userProfile

    override suspend fun updateProfile(
        request: RequestProfileUpdate
    ) {
        remoteDataSource.updateProfile(request.toDto())
            .onSuccess { dto ->
                dto.profile?.toDomain(
                    dto.id,
                    dto.phoneNumber
                )?.let { newProfile ->
                    localDataSource.updateUserProfile {
                        newProfile
                    }
                }
            }
            .onError {
                Log.e("CareerPilot: updateProfile", "updateProfile: ${it.name}", )
            }
    }
}