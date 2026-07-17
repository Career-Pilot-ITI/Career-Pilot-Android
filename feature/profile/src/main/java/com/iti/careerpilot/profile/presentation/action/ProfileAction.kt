package com.iti.careerpilot.profile.presentation.action



sealed interface ProfileAction {
    data object OnEditProfileClick : ProfileAction
    data object OnSettingsClick : ProfileAction
    data object OnLogoutClick : ProfileAction
    data object OnLogoutConfirm : ProfileAction
    data object OnLogoutDismiss : ProfileAction
}
