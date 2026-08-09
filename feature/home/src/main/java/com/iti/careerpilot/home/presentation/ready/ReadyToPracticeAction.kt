package com.iti.careerpilot.home.presentation.ready

sealed interface ReadyToPracticeAction {
    data class MicrophonePermissionChanged(val isGranted: Boolean) : ReadyToPracticeAction
    data object SelectAudioMode : ReadyToPracticeAction
    data object SelectVideoMode : ReadyToPracticeAction
    data object MicrophoneRowClicked : ReadyToPracticeAction
    data object PermissionDialogDismissed : ReadyToPracticeAction
    data object BeginInterviewClicked : ReadyToPracticeAction
    data object CancelClicked : ReadyToPracticeAction
}
