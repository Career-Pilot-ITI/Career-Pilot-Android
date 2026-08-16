package com.iti.careerpilot.ats.presentation.optimizedcv.state

sealed interface OptimizedCvIntent {
    data class Initial(val jobId: Long) : OptimizedCvIntent
    data object Retry : OptimizedCvIntent
    data object DismissGateSheet : OptimizedCvIntent
    data object DismissCoinTopUpSheet : OptimizedCvIntent
}
