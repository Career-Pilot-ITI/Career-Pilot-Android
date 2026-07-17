package com.iti.onboarding.presentation.screen.track.state

import androidx.compose.runtime.Immutable
import com.iti.onboarding.domain.model.Track
import com.iti.onboarding.util.dummyTracks

@Immutable
data class ChoosingTracksUiState(
    val tracks: List<Track> = dummyTracks,
    val selectedTrack: Track? = null,
    val customTrack: String = "",
)
