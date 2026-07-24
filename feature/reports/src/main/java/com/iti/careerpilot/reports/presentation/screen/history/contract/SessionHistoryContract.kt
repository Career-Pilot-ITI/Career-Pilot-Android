package com.iti.careerpilot.reports.presentation.screen.history.contract

import androidx.compose.runtime.Immutable
import com.iti.careerpilot.reports.presentation.screen.history.uimodels.SessionHistoryUiModel
import com.iti.common.util.UIText

@Immutable
data class SessionHistoryState(
    val isLoading: Boolean = false,
    val content: SessionHistoryUiModel? = null,
    val isOnline: Boolean = true,
    val error: UIText? = null,
)

sealed interface SessionHistoryAction {
    data object Load : SessionHistoryAction
    data object Retry : SessionHistoryAction
    data class SessionClicked(val sessionId: String) : SessionHistoryAction
}

sealed interface SessionHistoryEvent {
    data class NavigateToSessionDetails(val sessionId: String) : SessionHistoryEvent
}
