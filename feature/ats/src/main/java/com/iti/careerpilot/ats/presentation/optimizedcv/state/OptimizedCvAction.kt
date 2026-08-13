package com.iti.careerpilot.ats.presentation.optimizedcv.state

sealed interface OptimizedCvAction {
    data class Initial(val jobId: Long) : OptimizedCvAction
    data object Retry : OptimizedCvAction
}
