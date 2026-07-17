package com.iti.onboarding.presentation.screen.track.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.common.result.onError
import com.iti.common.result.onSuccess
import com.iti.common.util.UIText
import com.iti.onboarding.domain.usecase.GetTracksUseCase
import com.iti.onboarding.presentation.screen.track.state.ChoosingTracksEffects
import com.iti.onboarding.presentation.screen.track.state.ChoosingTracksIntent
import com.iti.onboarding.presentation.screen.track.state.ChoosingTracksUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


@HiltViewModel
class ChoosingTracksViewModel @Inject constructor(
    private val getTracksUseCase: GetTracksUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(ChoosingTracksUiState())
    val state = _state.asStateFlow()

    private val _effects = MutableSharedFlow<ChoosingTracksEffects>()
    val effects = _effects.asSharedFlow()

    init {
        loadTracks()
    }

    private fun loadTracks() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            getTracksUseCase().onSuccess { tracks ->
                _state.update { it.copy(tracks = tracks, isLoading = false) }
            }.onError { error ->
                // TODO: Mapping error to UiText or show error screen
                _effects.emit(
                    ChoosingTracksEffects.ShowError(
                        UIText.StringResource(
                            com.iti.common.R.string.error_unknown
                        )
                    )
                )
            }
        }
    }

    fun onIntent(intent: ChoosingTracksIntent) {
        when (intent) {
            is ChoosingTracksIntent.ToggleTrackSelection -> {
                _state.update {
                    it.copy(
                        selectedTrack = intent.track
                    )
                }
            }

            ChoosingTracksIntent.OnNavigateNext -> {
                viewModelScope.launch {
                    _effects.emit(ChoosingTracksEffects.NavigateNext)
                }
            }

            is ChoosingTracksIntent.OnChangeCustomTrackValue -> {
                _state.update {
                    it.copy(
                        customTrack = intent.customTrackValue
                    )
                }
            }
        }
    }
}