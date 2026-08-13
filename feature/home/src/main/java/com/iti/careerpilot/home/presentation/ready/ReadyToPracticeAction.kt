package com.iti.careerpilot.home.presentation.ready

sealed interface ReadyToPracticeAction {
    data class MicrophonePermissionChanged(val isGranted: Boolean) : ReadyToPracticeAction
    data class CameraPermissionChanged(val isGranted: Boolean) : ReadyToPracticeAction
    data object SelectAudioMode : ReadyToPracticeAction
    data object SelectVideoMode : ReadyToPracticeAction
    data class TogglePostureTracking(val enabled: Boolean) : ReadyToPracticeAction
    data class ToggleHandTracking(val enabled: Boolean) : ReadyToPracticeAction
    data object MicrophoneRowClicked : ReadyToPracticeAction
    data object CameraRowClicked : ReadyToPracticeAction
    data object PermissionDialogDismissed : ReadyToPracticeAction
    data object CameraPermissionDialogDismissed : ReadyToPracticeAction
    data object BeginInterviewClicked : ReadyToPracticeAction
    data object CancelClicked : ReadyToPracticeAction
}
