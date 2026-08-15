package com.iti.careerpilot.challenges.presentation.event

sealed interface ChallengesEvent {
    data object NavigateToCreateChallenge : ChallengesEvent
}
