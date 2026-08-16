package com.iti.careerpilot.ats.presentation.jobdetails.state

import androidx.compose.runtime.Immutable
import com.iti.careerpilot.ats.domain.model.JobWorkspace
import com.iti.common.util.UIText
import com.iti.core.model.FeatureAccess
import com.iti.core.model.Plan

@Immutable
data class JobDetailsUiState(
    val workspace: JobWorkspace? = null,
    val isLoading: Boolean = true,
    val error: UIText? = null,
    val atsScoreAccess: FeatureAccess = FeatureAccess.Unknown,
    val cvOptimizeAccess: FeatureAccess = FeatureAccess.Unknown,
    val coverLetterAccess: FeatureAccess = FeatureAccess.Unknown,
    val coinBalance: Int = 0,
    val planDisplayName: String = "Free",
    val showGateSheet: Boolean = false,
    val gateRequiredPlan: Plan = Plan.PLUS,
    val gatePlanFeatures: List<String> = emptyList(),
    val gateFeatureName: String = "",
    val showCoinTopUpSheet: Boolean = false,
    val coinTopUpRequiredCost: Int = 0,
)
