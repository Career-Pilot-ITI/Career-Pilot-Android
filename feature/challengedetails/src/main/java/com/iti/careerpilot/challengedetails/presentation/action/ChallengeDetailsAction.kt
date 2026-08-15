package com.iti.careerpilot.challengedetails.presentation.action

sealed interface ChallengeDetailsAction {
    data object OnBackClicked : ChallengeDetailsAction
}
