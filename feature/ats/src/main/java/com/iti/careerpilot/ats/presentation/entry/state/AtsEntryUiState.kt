package com.iti.careerpilot.ats.presentation.entry.state

import androidx.compose.runtime.Immutable
import com.iti.core.model.Plan

@Immutable
data class AtsEntryUiState(
    val jobUrl: String = "",
    val isUrlValid: Boolean = false,
    val cvFileName: String = "",
    val cvSizeBytes: Long = 0L,
    val hasSynchronizedCv: Boolean = false,
    val isImporting: Boolean = false,
    val showGateSheet: Boolean = false,
    val gateRequiredPlan: Plan? = null,
    val gateFeatureName: String = "",
    val gatePlanFeatures: List<String> = emptyList(),
    val showCoinTopUpSheet: Boolean = false,
    val coinTopUpRequiredCost: Int = 0,
    val hasInsufficientCoins: Boolean = false,
) {
    val canCompare: Boolean get() = isUrlValid && hasSynchronizedCv && !isImporting
}
