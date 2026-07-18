package com.iti.careerpilot.profile.data.datasource.local

import com.iti.careerpilot.profile.domain.datasource.local.ProfileLocalDataSource
import com.iti.core.datastore.models.UserProfile
import com.iti.core.datastore.repo.UserProfileRepo
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject


class ProfileLocalDataSourceImpl @Inject constructor(
    userProfileRepo: UserProfileRepo
): ProfileLocalDataSource {

    override val userProfile: StateFlow<UserProfile> = userProfileRepo.userProfile

}