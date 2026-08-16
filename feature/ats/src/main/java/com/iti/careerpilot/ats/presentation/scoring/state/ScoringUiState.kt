package com.iti.careerpilot.ats.presentation.scoring.state

import androidx.compose.runtime.Immutable
import com.iti.careerpilot.ats.domain.model.AtsScore
import com.iti.careerpilot.ats.domain.model.JobWorkspace
import com.iti.common.util.UIText
import com.iti.core.model.FeatureAccess
import com.iti.core.model.Plan

@Immutable
data class ScoringUiState(
    val workspace: JobWorkspace? = null,
    val score: AtsScore? = null,
    val isLoading: Boolean = true,
    val wasInterrupted: Boolean = false,
    val error: UIText? = null,
    val hasInsufficientCoins: Boolean = false,
    val isStartingOptimization: Boolean = false,
    val optimizationError: UIText? = null,
    val trackId: Long? = null,
    val trackName: String = "",
    val atsScoreAccess: FeatureAccess = FeatureAccess.Unknown,
    val cvOptimizeAccess: FeatureAccess = FeatureAccess.Unknown,
    val coinBalance: Int = 0,
    val planDisplayName: String = "Free",
    val showGateSheet: Boolean = false,
    val gateRequiredPlan: Plan = Plan.PLUS,
    val gatePlanFeatures: List<String> = emptyList(),
    val gateFeatureName: String = "",
    val showCoinTopUpSheet: Boolean = false,
    val coinTopUpRequiredCost: Int = 0,
)
