package com.iti.careerpilot.ats.presentation.optimizedcv.state

import androidx.compose.runtime.Immutable
import com.iti.careerpilot.ats.domain.model.CvOptimizationSection
import com.iti.common.util.UIText
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class OptimizedCvUiState(
    val sections: ImmutableList<CvOptimizationSection> = persistentListOf(),
    val recommendedTracks: ImmutableList<String> = persistentListOf(),
    val coinCost: Int? = null,
    val isLoading: Boolean = true,
    val error: UIText? = null,
)
