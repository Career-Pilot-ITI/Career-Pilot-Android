package com.iti.careerpilot.ats.presentation.optimizedcv.state

sealed interface OptimizedCvAction {
    data object RequestOptimization : OptimizedCvAction
    data object ConfirmOptimization : OptimizedCvAction
    data object DismissConfirmation : OptimizedCvAction
    data object Copy : OptimizedCvAction
    data object OpenCoins : OptimizedCvAction
}
