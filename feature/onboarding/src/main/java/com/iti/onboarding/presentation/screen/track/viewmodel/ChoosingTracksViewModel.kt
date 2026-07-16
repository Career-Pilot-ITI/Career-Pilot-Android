package com.iti.onboarding.presentation.screen.track.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.onboarding.presentation.screen.track.state.ChoosingTracksEffects
import com.iti.onboarding.presentation.screen.track.state.ChoosingTracksIntent
import com.iti.onboarding.presentation.screen.track.state.ChoosingTracksUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class ChoosingTracksViewModel : ViewModel() {
    private val _state = MutableStateFlow(ChoosingTracksUiState())
    val state = _state.asStateFlow()

    private val _effects = MutableSharedFlow<ChoosingTracksEffects>()
    val effects = _effects.asSharedFlow()

    fun onIntent(intent: ChoosingTracksIntent) {
        when (intent) {
            is ChoosingTracksIntent.ToggleTrackSelection -> {
                _state.update {
                    it.copy(
                        tracks = it.tracks.filter { track -> track == intent.track }
                            .map { track -> track.copy(selected = !intent.track.selected) },
                    )
                }
            }

            ChoosingTracksIntent.OnNavigateNext -> {
                viewModelScope.launch {
                    _effects.emit(ChoosingTracksEffects.NavigateNext)
                }
            }

            is ChoosingTracksIntent.OnChangeSearchQuery -> {
                _state.update {
                    it.copy(
                        filteredTracks = it.tracks.filter { track -> track.name == intent.searchQuery }
                    )
                }
            }
        }
    }
}