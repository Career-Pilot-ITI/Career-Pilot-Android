package com.iti.careerpilot.home.presentation.ready

sealed interface ReadyToPracticeEvent {
    data class NavigateToPractice(
        val trackId: Long,
        val workspaceId: Long?,
    ) : ReadyToPracticeEvent
    data object NavigateBack : ReadyToPracticeEvent
}
