package com.iti.careerpilot.home.presentation.ready

sealed interface ReadyToPracticeEvent {
    data class NavigateToInterview(val sessionId: Long) : ReadyToPracticeEvent
    data object NavigateBack : ReadyToPracticeEvent
}
