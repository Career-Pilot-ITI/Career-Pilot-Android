package com.iti.onboarding.presentation.screen.track.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.common.result.CareerPilotResult
import com.iti.common.util.UIText
import com.iti.common.util.toUIText
import com.iti.onboarding.domain.usecase.GetTracksUseCase
import com.iti.onboarding.domain.usecase.UpdateProfileTrackUseCase
import com.iti.onboarding.presentation.screen.track.state.ChoosingTracksEffects
import com.iti.onboarding.presentation.screen.track.state.ChoosingTracksIntent
import com.iti.onboarding.presentation.screen.track.state.ChoosingTracksUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChoosingTracksViewModel @Inject constructor(
    private val getTracksUseCase: GetTracksUseCase,
    private val updateProfileTrackUseCase: UpdateProfileTrackUseCase,
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

            ChoosingTracksIntent.OnRefresh -> {
                loadTracks(isRefresh = true)
            }

            ChoosingTracksIntent.OnNavigateNext -> {
                viewModelScope.launch {
                    val result = updateProfileTrackUseCase(_state.value.selectedTrack?.id ?: 0)
                    when (result) {
                        is CareerPilotResult.Success -> {
                            _effects.emit(ChoosingTracksEffects.NavigateNext)
                        }
                        else -> {
                            _effects.emit(
                                ChoosingTracksEffects.ShowError(
                                    UIText.StringResource(com.iti.common.R.string.error_unknown)
                                )
                            )
                        }
                    }
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
                            result.error.toUIText()
                        )
                    )
                }

                is CareerPilotResult.Success -> {
                    _state.update { state ->
                        state.copy(
                            tracks = result.data.toImmutableList(),
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
