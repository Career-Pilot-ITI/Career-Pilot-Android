package com.iti.careerpilot.challengedashboard.presentation.event

sealed interface ChallengeDashboardEvent {
    data object NavigateBack : ChallengeDashboardEvent
}
