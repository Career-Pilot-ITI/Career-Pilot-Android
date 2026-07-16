package com.iti.onboarding.presentation.screen.track.state

import androidx.compose.runtime.Immutable
import com.iti.onboarding.presentation.screen.track.uimodel.TrackUiModel
import com.iti.onboarding.util.dummyTracks

@Immutable
data class ChoosingTracksUiState(
    val tracks: List<TrackUiModel> = dummyTracks,
    val filteredTracks: List<TrackUiModel> = dummyTracks,
    val searchQuery: String = "",
)
