package com.iti.careerpilot.ats.presentation.scoring.state

sealed interface ScoringAction {
    data object RequestScore : ScoringAction
    data object ConfirmScore : ScoringAction
    data object DismissConfirmation : ScoringAction
    data object RetryWorkspace : ScoringAction
    data object OpenCoins : ScoringAction
    data object GenerateCoverLetter : ScoringAction
    data object OptimizeCv : ScoringAction
    data object StartPractice : ScoringAction
}
