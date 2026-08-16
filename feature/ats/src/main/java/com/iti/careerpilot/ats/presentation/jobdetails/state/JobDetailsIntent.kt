package com.iti.careerpilot.ats.presentation.jobdetails.state

sealed interface JobDetailsIntent {
    data class Initial(val workspaceId: Long) : JobDetailsIntent
    data object Retry : JobDetailsIntent
    data object StartScoring : JobDetailsIntent
    data object DismissGateSheet : JobDetailsIntent
    data object DismissCoinTopUpSheet : JobDetailsIntent
    data object UpgradeFromGate : JobDetailsIntent
    data object BuyCoinsClicked : JobDetailsIntent
}
