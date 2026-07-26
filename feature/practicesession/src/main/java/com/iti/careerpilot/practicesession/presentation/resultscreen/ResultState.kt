package com.iti.careerpilot.practicesession.presentation.resultscreen

import androidx.compose.runtime.Immutable
import com.iti.careerpilot.practicesession.domain.models.SessionResult

@Immutable
data class ResultState(
    val isLoading: Boolean = false,
    val sessionId: Long? = null,
    val sessionResult: SessionResult? = null,
)