package com.iti.careerpilot.ats.presentation.optimizedcv.state

import com.iti.careerpilot.ats.domain.model.CvOptimizationSection
import com.iti.common.util.UIText

data class OptimizedCvUiState(
    val sections: List<CvOptimizationSection> = emptyList(),
    val recommendedTracks: List<String> = emptyList(),
    val coinCost: Int? = null,
    val isLoading: Boolean = true,
    val error: UIText? = null,
)
