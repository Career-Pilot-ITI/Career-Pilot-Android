package com.iti.careerpilot.home.presentation.interviews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.home.domain.model.InterviewTrack
import com.iti.careerpilot.home.domain.usecase.GetTracksUseCase
import com.iti.common.result.CareerPilotResult
import com.iti.common.snackbar.CareerPilotSnackbarController
import com.iti.common.util.toUIText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InterviewsViewModel @Inject constructor(
    private val getInterviewTracks: GetTracksUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(InterviewsState())
    val state = _state.asStateFlow()

    private val _events = Channel<InterviewsEffect>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var allTracks: List<InterviewTrack> = emptyList()

    fun onIntent(intent: InterviewsIntent) {
        when (intent) {
            InterviewsIntent.Initial -> load()
            is InterviewsIntent.QueryChanged -> {
                _state.update { it.copy(query = intent.query) }
                applyFilter()
            }

            is InterviewsIntent.PracticeTrackClicked -> viewModelScope.launch {
                _events.send(
                    InterviewsEffect.NavigateToReadyToPractice(
                        trackId = intent.trackId,
                        trackName = intent.trackName,
                    )
                )
            }

            is InterviewsIntent.LessonTrackClicked -> viewModelScope.launch {
                _events.send(
                    InterviewsEffect.NavigateToQuiz(
                        trackId = intent.trackId,
                        trackName = intent.trackName,
                    )
                )
            }

            InterviewsIntent.Retry -> load()
        }
    }

    private fun load() {
        if (_state.value.isLoading) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            when (val result = getInterviewTracks()) {
                is CareerPilotResult.Error -> {
                    _state.update {
                        it.copy(isLoading = false, error = result.error.toUIText())
                    }
                    CareerPilotSnackbarController.show(result.error.toUIText())
                }

                is CareerPilotResult.Success -> {
                    allTracks = result.data
                    _state.update { it.copy(isLoading = false, hasLoadedTracks = true) }
                    applyFilter()
                }
            }
        }
    }

    private fun applyFilter() {
        val query = _state.value.query.trim()
        val filtered = if (query.isEmpty()) {
            allTracks
        } else {
            allTracks.filter { track ->
                track.name.contains(query, ignoreCase = true) ||
                    track.description.contains(query, ignoreCase = true)
            }
        }

        _state.update { it.copy(tracks = filtered.toImmutableList()) }
    }
}
