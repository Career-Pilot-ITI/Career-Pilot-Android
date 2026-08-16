package com.iti.careerpilot.challengedashboard.presentation.action

import com.iti.careerpilot.challengedashboard.presentation.state.DashboardTab
import com.iti.careerpilot.challengefirestore.Challenge

sealed interface ChallengeDashboardAction {
    data object Initial : ChallengeDashboardAction
    data object Refresh : ChallengeDashboardAction
    data class OnTabSelected(val tab: DashboardTab) : ChallengeDashboardAction
    data class OnDeleteChallenge(val challenge: Challenge) : ChallengeDashboardAction
    data object OnConfirmDelete : ChallengeDashboardAction
    data object OnDismissDeleteConfirmation : ChallengeDashboardAction
    data class OnEditChallenge(val challengeId: String) : ChallengeDashboardAction
    data class OnViewParticipantReports(val challengeId: String) : ChallengeDashboardAction
    data class OnTakenChallengeClicked(val sessionId: String) : ChallengeDashboardAction
    data object OnDismissParticipantReports : ChallengeDashboardAction
    data object OnBackClicked : ChallengeDashboardAction
}
