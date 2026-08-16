package com.iti.careerpilot.profile.presentation.event

import com.iti.common.model.ProfileEditSection

sealed interface ProfileEffect {
    data class NavigateToEditProfile(val section: ProfileEditSection) : ProfileEffect
    data object NavigateToSettings : ProfileEffect
    data object NavigateToSubscription : ProfileEffect
    data object NavigateToLogout : ProfileEffect
    data class OpenCV(val url: String) : ProfileEffect
}
