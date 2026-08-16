package com.iti.careerpilot.challengedetails.presentation.state

import com.iti.careerpilot.challengefirestore.Challenge
import com.iti.careerpilot.challengefirestore.ChallengeType

data class ChallengeDetailsState(
    val isLoading: Boolean = false,
    val challenge: Challenge? = null,
    val error: String? = null,
    val isMicrophoneGranted: Boolean = false,
    val isCameraGranted: Boolean = false,
    val showMicPermissionDialog: Boolean = false,
    val showCameraPermissionDialog: Boolean = false
) {
    val canBegin: Boolean
        get() = challenge != null && 
                isMicrophoneGranted && 
                (challenge.type == ChallengeType.AUDIO_ONLY || isCameraGranted)
}
