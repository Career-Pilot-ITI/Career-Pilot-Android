package com.iti.careerpilot.challengedetails.presentation.event

import com.iti.careerpilot.challengefirestore.Challenge

sealed interface ChallengeDetailsEvent {
    data object NavigateBack : ChallengeDetailsEvent
    data class NavigateToPractice(
        val challenge: Challenge
    ) : ChallengeDetailsEvent
}
