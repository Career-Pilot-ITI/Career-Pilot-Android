package com.iti.careerpilot.ats.presentation.scoring.state

sealed interface ScoringIntent {
    data class Initial(val workspaceId: Long) : ScoringIntent
    data object Retry : ScoringIntent
    data object OpenCoins : ScoringIntent
    data object GenerateCoverLetter : ScoringIntent
    data object OptimizeCv : ScoringIntent
    data object StartPractice : ScoringIntent
    data object DismissGateSheet : ScoringIntent
    data object DismissCoinTopUpSheet : ScoringIntent
    data object UpgradeFromGate : ScoringIntent
    data object BuyCoinsClicked : ScoringIntent
}
