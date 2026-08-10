package com.iti.careerpilot.ats.presentation.jobdetails

import com.iti.careerpilot.ats.domain.model.JobWorkspace
import com.iti.common.util.UIText

data class JobDetailsUiState(
    val isLoading: Boolean = true,
    val workspace: JobWorkspace? = null,
    val error: UIText? = null,
)

sealed interface JobDetailsAction {
    data object RetryClicked : JobDetailsAction
    data object ScoreClicked : JobDetailsAction
    data object ExternalLinkClicked : JobDetailsAction
}

sealed interface JobDetailsEffect {
    data class NavigateToScore(val workspaceId: Long) : JobDetailsEffect
    data class OpenExternalUrl(val url: String) : JobDetailsEffect
}
