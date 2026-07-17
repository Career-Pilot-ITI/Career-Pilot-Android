package com.iti.careerpilot.profile.data.datasource.local

import androidx.datastore.core.DataStore
import com.iti.careerpilot.profile.domain.datasource.local.ProfileLocalDataSource
import com.iti.core.datastore.models.UserProfile
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class ProfileLocalDataSourceImpl @Inject constructor(
    dataStore: DataStore<UserProfile>
): ProfileLocalDataSource {

    override val userProfile: Flow<UserProfile> = dataStore.data

}