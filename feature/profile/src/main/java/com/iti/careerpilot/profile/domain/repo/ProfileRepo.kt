package com.iti.careerpilot.profile.domain.repo

import com.iti.core.datastore.models.UserProfile
import kotlinx.coroutines.flow.Flow

interface ProfileRepo {

    val userProfile: Flow<UserProfile>
}