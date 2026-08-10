package com.iti.careerpilot.ats.presentation.scoring

import com.iti.careerpilot.ats.domain.model.AtsScore
import com.iti.careerpilot.ats.domain.model.JobWorkspace
import com.iti.common.util.UIText

data class ScoringUiState(
    val workspace: JobWorkspace? = null,
    val score: AtsScore? = null,
    val isLoading: Boolean = true,
    val isScoreConfirmationVisible: Boolean = false,
    val wasInterrupted: Boolean = false,
    val error: UIText? = null,
    val hasInsufficientCoins: Boolean = false,
    val trackId: Long? = null,
)

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

sealed interface ScoringEffect {
    data object OpenCoinsPaywall : ScoringEffect
    data class OpenCoverLetter(val workspaceId: Long) : ScoringEffect
    data class OpenOptimizedCv(val workspaceId: Long) : ScoringEffect
    data class OpenPractice(val trackId: Long) : ScoringEffect
}
