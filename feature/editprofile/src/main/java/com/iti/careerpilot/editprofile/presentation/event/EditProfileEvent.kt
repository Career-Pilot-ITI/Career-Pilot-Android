package com.iti.careerpilot.editprofile.presentation.event

sealed interface EditProfileEvent {
    data object NavigateBack : EditProfileEvent
    data class ShowSnackbar(val message: String) : EditProfileEvent
}