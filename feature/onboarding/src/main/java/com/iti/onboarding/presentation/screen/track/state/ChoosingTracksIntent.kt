package com.iti.onboarding.presentation.screen.track.state

import com.iti.onboarding.presentation.screen.track.uimodel.TrackUiModel

sealed interface ChoosingTracksIntent {
    data class ToggleTrackSelection(val track: TrackUiModel) : ChoosingTracksIntent
    data class OnChangeSearchQuery(val searchQuery: String) : ChoosingTracksIntent
    data object OnNavigateNext : ChoosingTracksIntent
}