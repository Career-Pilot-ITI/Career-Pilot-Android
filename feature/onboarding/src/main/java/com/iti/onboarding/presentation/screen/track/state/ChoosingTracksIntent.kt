package com.iti.onboarding.presentation.screen.track.state

import com.iti.onboarding.domain.model.Track

sealed interface ChoosingTracksIntent {
    data class ToggleTrackSelection(val track: Track) : ChoosingTracksIntent
    data class OnChangeCustomTrackValue(val customTrackValue: String) : ChoosingTracksIntent
    data object OnNavigateNext : ChoosingTracksIntent
}