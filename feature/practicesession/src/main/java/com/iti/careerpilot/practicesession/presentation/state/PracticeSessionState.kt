package com.iti.careerpilot.practicesession.presentation.state

import androidx.compose.runtime.Immutable
import com.iti.careerpilot.practicesession.domain.models.Session

@Immutable
data class PracticeSessionState(
    val isLoading: Boolean = false,
    val sessionId: String = "",
    val showPermissionDialog: Boolean = false,
    val currentSession: Session? = null,
)