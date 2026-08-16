package com.iti.careerpilot.home.presentation.ready

sealed interface ReadyToPracticeIntent {
    data class Initial(
        val trackId: Long,
        val trackName: String,
        val workspaceId: Long? = null,
    ) : ReadyToPracticeIntent
    data class MicrophonePermissionChanged(val isGranted: Boolean) : ReadyToPracticeIntent
    data class CameraPermissionChanged(val isGranted: Boolean) : ReadyToPracticeIntent
    data object SelectAudioMode : ReadyToPracticeIntent
    data object SelectVideoMode : ReadyToPracticeIntent
    data class TogglePostureTracking(val enabled: Boolean) : ReadyToPracticeIntent
    data class ToggleHandTracking(val enabled: Boolean) : ReadyToPracticeIntent
    data object MicrophoneRowClicked : ReadyToPracticeIntent
    data object CameraRowClicked : ReadyToPracticeIntent
    data object PermissionDialogDismissed : ReadyToPracticeIntent
    data object CameraPermissionDialogDismissed : ReadyToPracticeIntent
    data object BeginInterviewClicked : ReadyToPracticeIntent
    data object StartPracticeClicked : ReadyToPracticeIntent
    data object CancelClicked : ReadyToPracticeIntent
    data object DismissVideoGateSheet : ReadyToPracticeIntent
    data object DismissCoinTopUpSheet : ReadyToPracticeIntent
    data object UpgradeFromVideoGate : ReadyToPracticeIntent
    data object BuyCoinsClicked : ReadyToPracticeIntent
}
