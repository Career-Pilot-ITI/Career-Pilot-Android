package com.iti.onboarding.presentation.screen.track.state

sealed interface ChoosingTracksEffects {
    data object NavigateNext: ChoosingTracksEffects
    data class ShowError(val message: String): ChoosingTracksEffects
}