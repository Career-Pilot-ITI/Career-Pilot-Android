package com.iti.onboarding.presentation.screen.track.state

import androidx.compose.runtime.Immutable
import com.iti.onboarding.domain.model.Track
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class ChoosingTracksUiState(
    val tracks: ImmutableList<Track> = persistentListOf(),
    val selectedTrack: Track? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
) {
    val isFormValid: Boolean
        get() = selectedTrack != null
}
