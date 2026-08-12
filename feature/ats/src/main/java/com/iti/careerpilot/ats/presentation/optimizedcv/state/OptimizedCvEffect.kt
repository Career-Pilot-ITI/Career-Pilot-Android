package com.iti.careerpilot.ats.presentation.optimizedcv.state

sealed interface OptimizedCvEffect {
    data class CopyText(val value: String) : OptimizedCvEffect
    data object OpenCoinsPaywall : OptimizedCvEffect
}
