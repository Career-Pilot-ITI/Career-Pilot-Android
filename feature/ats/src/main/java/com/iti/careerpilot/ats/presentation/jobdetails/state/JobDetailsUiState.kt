package com.iti.careerpilot.ats.presentation.jobdetails.state

import androidx.compose.runtime.Immutable
import com.iti.careerpilot.ats.domain.model.JobWorkspace
import com.iti.common.util.UIText

@Immutable
data class JobDetailsUiState(
    val workspace: JobWorkspace? = null,
    val isLoading: Boolean = true,
    val error: UIText? = null,
)
