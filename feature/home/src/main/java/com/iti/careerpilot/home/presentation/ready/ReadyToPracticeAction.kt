package com.iti.careerpilot.home.presentation.ready

sealed interface ReadyToPracticeAction {
    data class Initial(
        val trackId: Long,
        val trackName: String,
        val workspaceId: Long? = null,
    ) : ReadyToPracticeAction
    data class MicrophonePermissionChanged(val isGranted: Boolean) : ReadyToPracticeAction
    data object MicrophoneRowClicked : ReadyToPracticeAction
    data object PermissionDialogDismissed : ReadyToPracticeAction
    data object BeginInterviewClicked : ReadyToPracticeAction
    data object CancelClicked : ReadyToPracticeAction
}
