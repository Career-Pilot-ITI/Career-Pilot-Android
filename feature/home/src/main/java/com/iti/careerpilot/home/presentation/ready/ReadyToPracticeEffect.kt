package com.iti.careerpilot.home.presentation.ready

sealed interface ReadyToPracticeEffect {
    data class NavigateToPractice(
        val trackId: Long,
        val workspaceId: Long? = null,
        val isVideo: Boolean = false,
        val enablePosture: Boolean = false,
        val enableHands: Boolean = false,
    ) : ReadyToPracticeEffect
    data object NavigateToPaywall : ReadyToPracticeEffect
    data object NavigateBack : ReadyToPracticeEffect
}
