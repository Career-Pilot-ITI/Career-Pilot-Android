package com.iti.careerpilot.home.presentation.ready

sealed interface ReadyToPracticeEvent {
    data class NavigateToPractice(val trackId: Long) : ReadyToPracticeEvent
    data object NavigateBack : ReadyToPracticeEvent
}
