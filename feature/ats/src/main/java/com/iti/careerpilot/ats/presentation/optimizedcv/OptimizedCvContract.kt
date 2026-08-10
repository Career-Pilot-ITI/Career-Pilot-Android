package com.iti.careerpilot.ats.presentation.optimizedcv

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

sealed interface OptimizedCvAction {
    data object RequestOptimization : OptimizedCvAction
    data object ConfirmOptimization : OptimizedCvAction
    data object DismissConfirmation : OptimizedCvAction
    data object Copy : OptimizedCvAction
    data object OpenCoins : OptimizedCvAction
}

sealed interface OptimizedCvEffect {
    data class CopyText(val value: String) : OptimizedCvEffect
    data object OpenCoinsPaywall : OptimizedCvEffect
}
