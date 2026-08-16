package com.iti.careerpilot.challengedashboard.presentation.action

import com.iti.careerpilot.challengefirestore.ChallengeVisibility


sealed interface ChallengeDashboardAction {
    data object Initial : ChallengeDashboardAction
    data object Refresh : ChallengeDashboardAction
    data class OnTabSelected(val index: Int) : ChallengeDashboardAction
    data class OnDeleteChallenge(val challengeId: String, val visibility: ChallengeVisibility) : ChallengeDashboardAction
    data class OnEditChallenge(val challengeId: String) : ChallengeDashboardAction
    data class OnViewParticipantReports(val challengeId: String) : ChallengeDashboardAction
    data class OnTakenChallengeClicked(val sessionId: String) : ChallengeDashboardAction
    data object OnDismissParticipantReports : ChallengeDashboardAction
    data object OnBackClicked : ChallengeDashboardAction
}
