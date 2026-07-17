package com.iti.careerpilot.profile.presentation.event
import com.iti.common.model.ProfileEditSection



sealed interface ProfileEvent {
    data class NavigateToEditProfile(val section: ProfileEditSection) : ProfileEvent
    data object NavigateToSettings : ProfileEvent
    data object NavigateToLogout : ProfileEvent
}