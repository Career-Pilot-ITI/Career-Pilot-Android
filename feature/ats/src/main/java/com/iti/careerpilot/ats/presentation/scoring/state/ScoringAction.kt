package com.iti.careerpilot.ats.presentation.scoring.state

sealed interface ScoringAction {
    data class Initial(val workspaceId: Long) : ScoringAction
    data object StartScore : ScoringAction
    data object RetryWorkspace : ScoringAction
    data object OpenCoins : ScoringAction
    data object GenerateCoverLetter : ScoringAction
    data object OptimizeCv : ScoringAction
    data object StartPractice : ScoringAction
}
