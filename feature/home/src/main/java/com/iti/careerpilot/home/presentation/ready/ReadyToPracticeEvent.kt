package com.iti.careerpilot.home.presentation.ready

sealed interface ReadyToPracticeEvent {
    data class NavigateToPractice(
        val trackId: Long,
        val workspaceId: Long? = null,
        val isVideo: Boolean = false,
        val enablePosture: Boolean = false,
        val enableHands: Boolean = false,
    ) : ReadyToPracticeEvent
    data object NavigateToPaywall : ReadyToPracticeEvent
    data object NavigateBack : ReadyToPracticeEvent
}
