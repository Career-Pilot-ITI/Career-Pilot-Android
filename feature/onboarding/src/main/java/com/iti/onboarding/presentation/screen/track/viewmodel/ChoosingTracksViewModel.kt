package com.iti.onboarding.presentation.screen.track.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.common.result.CareerPilotResult
import com.iti.common.util.UIText
import com.iti.onboarding.domain.usecase.GetTracksUseCase
import com.iti.onboarding.presentation.screen.track.state.ChoosingTracksEffects
import com.iti.onboarding.presentation.screen.track.state.ChoosingTracksIntent
import com.iti.onboarding.presentation.screen.track.state.ChoosingTracksUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ChoosingTracksViewModel @Inject constructor(
    private val getTracksUseCase: GetTracksUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(ChoosingTracksUiState())
    val state = _state.asStateFlow()

    private val _effects = MutableSharedFlow<ChoosingTracksEffects>()
    val effects = _effects.asSharedFlow()

    init {
        loadTracks(isRefresh = false)
    }

    fun onIntent(intent: ChoosingTracksIntent) {
        when (intent) {
            is ChoosingTracksIntent.ToggleTrackSelection -> {
                _state.update {
                    it.copy(selectedTrack = intent.track)
                }
            }

            is ChoosingTracksIntent.OnChangeCustomTrackValue -> {
                _state.update {
                    it.copy(customTrack = intent.customTrackValue)
                }
            }

            ChoosingTracksIntent.OnRefresh -> {
                loadTracks(isRefresh = true)
            }

            ChoosingTracksIntent.OnNavigateNext -> {
                viewModelScope.launch {
                    _effects.emit(ChoosingTracksEffects.NavigateNext)
                }
            }
        }
    }

    private fun loadTracks(isRefresh: Boolean) {
        val currentState = _state.value
        if (currentState.isLoading || currentState.isRefreshing) return

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = !isRefresh,
                    isRefreshing = isRefresh,
                )
            }

            when (val result = getTracksUseCase()) {
                is CareerPilotResult.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                        )
                    }

                    _effects.emit(
                        ChoosingTracksEffects.ShowError(
                            UIText.StringResource(
                                com.iti.common.R.string.error_unknown,
                            )
                        )
                    )
                }

                is CareerPilotResult.Success -> {
                    _state.update { state ->
                        state.copy(
                            tracks = result.data,
                            selectedTrack = state.selectedTrack?.let { selected ->
                                result.data.firstOrNull { it.id == selected.id }
                            },
                            isLoading = false,
                            isRefreshing = false,
                        )
                    }
                }
            }
        }
    }
}
