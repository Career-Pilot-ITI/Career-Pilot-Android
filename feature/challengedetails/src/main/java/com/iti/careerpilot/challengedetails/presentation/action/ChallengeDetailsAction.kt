package com.iti.careerpilot.challengedetails.presentation.action

sealed interface ChallengeDetailsAction {
    data class Initial(val challengeId: String) : ChallengeDetailsAction
    data object MicrophoneRowClicked : ChallengeDetailsAction
    data object CameraRowClicked : ChallengeDetailsAction
    data object PermissionDialogDismissed : ChallengeDetailsAction
    data object CameraPermissionDialogDismissed : ChallengeDetailsAction
    data class MicrophonePermissionChanged(val isGranted: Boolean) : ChallengeDetailsAction
    data class CameraPermissionChanged(val isGranted: Boolean) : ChallengeDetailsAction
    data object BeginChallengeClicked : ChallengeDetailsAction
    data object OnBackClicked : ChallengeDetailsAction
}
