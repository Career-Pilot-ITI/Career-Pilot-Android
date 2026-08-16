package com.iti.careerpilot.challengedashboard.presentation.event

sealed interface ChallengeDashboardEvent {
    data object NavigateBack : ChallengeDashboardEvent
    data class NavigateToEditChallenge(val challengeId: String) : ChallengeDashboardEvent
    data class NavigateToSessionDetails(val sessionId: String) : ChallengeDashboardEvent
    data class ContinueSession(val sessionId: String) : ChallengeDashboardEvent
}
