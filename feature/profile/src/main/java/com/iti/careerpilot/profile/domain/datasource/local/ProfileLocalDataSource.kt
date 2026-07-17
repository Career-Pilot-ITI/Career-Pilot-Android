package com.iti.careerpilot.profile.domain.datasource.local

import com.iti.core.datastore.models.UserProfile
import kotlinx.coroutines.flow.Flow

interface ProfileLocalDataSource {

    val userProfile: Flow<UserProfile>

}