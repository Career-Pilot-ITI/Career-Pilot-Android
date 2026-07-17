package com.iti.careerpilot.editprofile.data.repo

import android.util.Log
import com.iti.careerpilot.editprofile.data.datasource.remote.mapper.toDomain
import com.iti.careerpilot.editprofile.data.datasource.remote.mapper.toDto
import com.iti.careerpilot.editprofile.domain.datasource.local.EditProfileLocalDataSource
import com.iti.careerpilot.editprofile.domain.datasource.remote.EditProfileRemoteDataSource
import com.iti.careerpilot.editprofile.domain.models.RequestProfileUpdate
import com.iti.careerpilot.editprofile.domain.repo.EditProfileRepo
import com.iti.common.result.onError
import com.iti.common.result.onSuccess
import com.iti.core.datastore.models.UserProfile
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class EditProfileRepoImpl @Inject constructor(
    private val localDataSource: EditProfileLocalDataSource,
    private val remoteDataSource: EditProfileRemoteDataSource,
): EditProfileRepo {

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