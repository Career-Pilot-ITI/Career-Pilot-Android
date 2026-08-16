package com.iti.careerpilot.profile.presentation.action

import com.iti.common.model.ProfileEditSection

sealed interface ProfileIntent {
    data object Initial : ProfileIntent
    data class OnEditProfileClick(val section: ProfileEditSection) : ProfileIntent
    data object OnSettingsClick : ProfileIntent
    data object OnSubscriptionClick : ProfileIntent
    data object OnLogoutClick : ProfileIntent
    data object OnLogoutConfirm : ProfileIntent
    data object OnLogoutDismiss : ProfileIntent
    data class OnCVClick(val cvLocalUriOrUrl: String) : ProfileIntent
}
