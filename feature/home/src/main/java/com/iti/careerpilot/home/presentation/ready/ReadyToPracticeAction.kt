package com.iti.careerpilot.home.presentation.ready

sealed interface ReadyToPracticeAction {
    data class Initial(
        val trackId: Long,
        val trackName: String,
        val workspaceId: Long? = null,
    ) : ReadyToPracticeAction
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
    data object StartPracticeClicked : ReadyToPracticeAction
    data object CancelClicked : ReadyToPracticeAction
    data object DismissVideoGateSheet : ReadyToPracticeAction
    data object DismissCoinTopUpSheet : ReadyToPracticeAction
    data object UpgradeFromVideoGate : ReadyToPracticeAction
    data object BuyCoinsClicked : ReadyToPracticeAction
}
