package com.iti.careerpilot.challengedetails.presentation.state

import com.iti.core.model.Challenge
import com.iti.core.model.ChallengeType

data class ChallengeDetailsState(
    val isLoading: Boolean = false,
    val challenge: Challenge? = null,
    val error: String? = null,
    val isMicrophoneGranted: Boolean = false,
    val isCameraGranted: Boolean = false,
    val isPermissionDialogVisible: Boolean = false,
    val showCameraPermissionDialog: Boolean = false
) {
    val canBegin: Boolean
        get() = challenge != null && 
                isMicrophoneGranted && 
                (challenge.type == ChallengeType.AUDIO_ONLY || isCameraGranted)
}
