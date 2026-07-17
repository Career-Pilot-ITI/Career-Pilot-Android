package com.iti.onboarding.presentation.screen.track.state

import androidx.compose.runtime.Immutable
import com.iti.onboarding.domain.model.Track

@Immutable
data class ChoosingTracksUiState(
    val tracks: List<Track> = emptyList(),
    val selectedTrack: Track? = null,
    val customTrack: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)
