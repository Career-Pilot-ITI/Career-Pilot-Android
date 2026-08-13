package com.iti.careerpilot.ats.presentation.jobdetails.state

import com.iti.careerpilot.ats.domain.model.JobWorkspace
import com.iti.common.util.UIText

data class JobDetailsUiState(
    val workspace: JobWorkspace? = null,
    val isLoading: Boolean = true,
    val error: UIText? = null,
)
