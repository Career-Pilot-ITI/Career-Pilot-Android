package com.iti.careerpilot.challenges.presentation.event

import com.iti.core.model.Plan

sealed interface ChallengesEvent {
    data object NavigateToCreateChallenge : ChallengesEvent
    data object NavigateToChallengeDashboard : ChallengesEvent
    data class NavigateToChallengeDetails(val challengeId: String) : ChallengesEvent
    data class ShowFeatureGate(
        val featureName: String,
        val requiredPlan: Plan,
        val planFeatures: List<String>
    ) : ChallengesEvent
    data object NavigateToPlansPaywall : ChallengesEvent
}
