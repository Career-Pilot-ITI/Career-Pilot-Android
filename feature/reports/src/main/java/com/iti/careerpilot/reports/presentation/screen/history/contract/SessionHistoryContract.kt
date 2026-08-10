package com.iti.careerpilot.reports.presentation.screen.history.contract

import androidx.compose.runtime.Immutable

@Immutable
data class SessionHistoryState(
    val isOnline: Boolean = true,
)

sealed interface SessionHistoryAction {
    data class SessionClicked(val sessionId: Long) : SessionHistoryAction
}

sealed interface SessionHistoryEvent {
    data class NavigateToSessionDetails(val sessionId: Long) : SessionHistoryEvent
}
