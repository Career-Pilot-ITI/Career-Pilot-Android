package com.iti.careerpilot.profile.data.repo

import com.iti.careerpilot.profile.domain.datasource.local.ProfileLocalDataSource
import com.iti.careerpilot.profile.domain.repo.ProfileRepo
import com.iti.core.datastore.models.UserProfile
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class ProfileRepoImpl @Inject constructor(
    private val localDataSource: ProfileLocalDataSource,
): ProfileRepo {

    override val userProfile: StateFlow<UserProfile> = localDataSource.userProfile
    override suspend fun clearUserProfile() {
        localDataSource.clearUserProfile()
    }

}