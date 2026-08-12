package com.iti.careerpilot.ats.presentation.optimizedcv.state

import com.iti.common.util.UIText

data class OptimizedCvUiState(
    val optimizedText: String = "",
    val recommendedTracks: List<String> = emptyList(),
    val coinCost: Int? = null,
    val isLoading: Boolean = true,
    val isConfirmationVisible: Boolean = false,
    val wasInterrupted: Boolean = false,
    val hasInsufficientCoins: Boolean = false,
    val error: UIText? = null,
)
