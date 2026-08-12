package com.iti.careerpilot.ats.presentation.scoring.state

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
