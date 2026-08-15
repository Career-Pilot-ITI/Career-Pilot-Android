package com.iti.careerpilot.challengedashboard.presentation.action

sealed interface ChallengeDashboardAction {
    data object OnBackClicked : ChallengeDashboardAction
}
