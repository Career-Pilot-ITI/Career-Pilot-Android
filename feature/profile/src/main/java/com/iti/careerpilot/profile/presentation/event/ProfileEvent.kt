package com.iti.careerpilot.profile.presentation.event



sealed interface ProfileEvent {
    data object NavigateToEditProfile : ProfileEvent
    data object NavigateToSettings : ProfileEvent
    data object NavigateToLogout : ProfileEvent
}