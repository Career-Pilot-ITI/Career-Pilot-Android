package com.iti.onboarding.presentation.screen.track.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.common.result.CareerPilotResult
import com.iti.common.util.toUIText
import com.iti.onboarding.domain.usecase.CompleteOnboardingUseCase
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
    private val completeOnboardingUseCase: CompleteOnboardingUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(ChoosingTracksUiState())
    val state = _state.asStateFlow()

    private val _effects = MutableSharedFlow<ChoosingTracksEffects>()
    val effects = _effects.asSharedFlow()

    fun onIntent(intent: ChoosingTracksIntent) {
        when (intent) {
            is ChoosingTracksIntent.ToggleTrackSelection -> {
                _state.update {
                    it.copy(selectedTrack = intent.track)
                }
            }

            ChoosingTracksIntent.FetchTracks -> {
                loadTracks(isRefresh = false)
            }

            ChoosingTracksIntent.OnRefresh -> {
                loadTracks(isRefresh = true)
            }

            ChoosingTracksIntent.OnNavigateNext -> {
                if (_state.value.isSubmitting) return
                viewModelScope.launch {
                    _state.update { it.copy(isSubmitting = true) }
                    try {
                        val trackId = _state.value.selectedTrack?.id ?: 0L
                        val trackResult = updateProfileTrackUseCase(trackId)
                        if (trackResult is CareerPilotResult.Success) {
                            when (val completeResult = completeOnboardingUseCase(cvFileId = null)) {
                                is CareerPilotResult.Success -> {
                                    _effects.emit(ChoosingTracksEffects.NavigateNext)
                                }
                                is CareerPilotResult.Error -> {
                                    _effects.emit(
                                        ChoosingTracksEffects.ShowError(completeResult.error.toUIText())
                                    )
                                }
                            }
                        } else if (trackResult is CareerPilotResult.Error) {
                            _effects.emit(
                                ChoosingTracksEffects.ShowError(trackResult.error.toUIText())
                            )
                        }
                    } finally {
                        _state.update { it.copy(isSubmitting = false) }
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
                            isLoading = false,
                            selectedTrack = null,
                            isRefreshing = false,
                        )
                    }
                }
            }
        }
    }
}
