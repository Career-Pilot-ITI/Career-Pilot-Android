package com.iti.careerpilot.profile.presentation.action
import com.iti.common.model.ProfileEditSection



sealed interface ProfileAction {
    data object Initial : ProfileAction
    data class OnEditProfileClick(val section: ProfileEditSection) : ProfileAction
    data object OnSettingsClick : ProfileAction
    data object OnLogoutClick : ProfileAction
    data object OnLogoutConfirm : ProfileAction
    data object OnLogoutDismiss : ProfileAction
    data class OnCVClick(val cvLocalUriOrUrl: String) : ProfileAction
}
