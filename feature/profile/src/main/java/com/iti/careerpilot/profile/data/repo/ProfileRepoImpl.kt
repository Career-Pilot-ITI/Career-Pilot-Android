package com.iti.careerpilot.profile.data.repo

import com.iti.careerpilot.profile.domain.datasource.local.ProfileLocalDataSource
import com.iti.careerpilot.profile.domain.datasource.remote.ProfileRemoteDataSource
import com.iti.careerpilot.profile.domain.repo.ProfileRepo
import javax.inject.Inject

class ProfileRepoImpl @Inject constructor(
    private val localDataSource: ProfileLocalDataSource,
    private val remoteDataSource: ProfileRemoteDataSource,
): ProfileRepo {
}