package com.iti.careerpilot.challenges.presentation.action

sealed interface ChallengesAction {
    data object CreateChallengeClicked : ChallengesAction
    data object ChallengeDashboardClicked : ChallengesAction
}
