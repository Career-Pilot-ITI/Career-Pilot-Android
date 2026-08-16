package com.iti.careerpilot.ats.presentation.jobdetails.state

sealed interface JobDetailsEffect {
    data class OpenScore(val workspaceId: Long) : JobDetailsEffect
    data class NavigateToPaywall(val showGetCoins: Boolean = false) : JobDetailsEffect
}
