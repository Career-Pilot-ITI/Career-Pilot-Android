package com.iti.careerpilot.profile.presentation.state

import androidx.compose.runtime.Immutable
import com.iti.core.datastore.models.UserProfile

@Immutable
data class ProfileState(
    val showLogoutDialog: Boolean = false,
    val profile: UserProfile = UserProfile(),
)
