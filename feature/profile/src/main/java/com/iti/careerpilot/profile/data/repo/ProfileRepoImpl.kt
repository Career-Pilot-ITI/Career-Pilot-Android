package com.iti.careerpilot.profile.data.repo

import com.iti.careerpilot.profile.domain.datasource.local.ProfileLocalDataSource
import com.iti.careerpilot.profile.domain.repo.ProfileRepo
import com.iti.core.datastore.models.UserProfile
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ProfileRepoImpl @Inject constructor(
    localDataSource: ProfileLocalDataSource,
): ProfileRepo {

    override val userProfile: Flow<UserProfile> = localDataSource.userProfile

}