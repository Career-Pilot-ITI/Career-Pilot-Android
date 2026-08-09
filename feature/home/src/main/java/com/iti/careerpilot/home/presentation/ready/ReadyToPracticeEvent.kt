package com.iti.careerpilot.home.presentation.ready

sealed interface ReadyToPracticeEvent {
    data class NavigateToPractice(val trackId: Long, val isVideo: Boolean = false) : ReadyToPracticeEvent
    data object NavigateToPaywall : ReadyToPracticeEvent
    data object NavigateBack : ReadyToPracticeEvent
}
