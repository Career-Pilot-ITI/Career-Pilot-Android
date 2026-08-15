package com.iti.careerpilot.ats.presentation.jobdetails.state

sealed interface JobDetailsAction {
    data class Initial(val workspaceId: Long) : JobDetailsAction
    data object Retry : JobDetailsAction
    data object StartScoring : JobDetailsAction
    data object DismissGateSheet : JobDetailsAction
    data object DismissCoinTopUpSheet : JobDetailsAction
    data object UpgradeFromGate : JobDetailsAction
    data object BuyCoinsClicked : JobDetailsAction
}
