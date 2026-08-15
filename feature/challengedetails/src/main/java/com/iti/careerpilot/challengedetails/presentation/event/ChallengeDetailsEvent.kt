package com.iti.careerpilot.challengedetails.presentation.event

sealed interface ChallengeDetailsEvent {
    data object NavigateBack : ChallengeDetailsEvent
}
