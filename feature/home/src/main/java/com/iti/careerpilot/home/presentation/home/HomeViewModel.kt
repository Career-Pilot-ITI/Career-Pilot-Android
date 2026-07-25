package com.iti.careerpilot.home.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.home.domain.model.InterviewSession
import com.iti.careerpilot.home.domain.model.InterviewTrack
import com.iti.careerpilot.home.domain.usecase.GetInterviewSessionsUseCase
import com.iti.careerpilot.home.domain.usecase.GetScoreSummaryUseCase
import com.iti.careerpilot.home.domain.usecase.GetTracksUseCase
import com.iti.careerpilot.home.domain.usecase.GetUserProfileUseCase
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
class HomeViewModel @Inject constructor(
    private val getUserProfile: GetUserProfileUseCase,
    private val getInterviewSessions: GetInterviewSessionsUseCase,
    private val getInterviewTracks: GetTracksUseCase,
    private val getScoreSummary: GetScoreSummaryUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    private val _events = Channel<HomeEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        observeProfile()
        load(isRefresh = false)
    }

    fun onAction(action: HomeAction) {
        when (action) {
            HomeAction.Refresh -> load(isRefresh = true)

            HomeAction.PracticeInterviewClicked -> {
                val current = _state.value
                val trackId = current.practiceTrackId ?: return
                sendEvent(
                    HomeEvent.NavigateToReadyToPractice(
                        trackId = trackId,
                        trackName = current.practiceTrackName,
                    )
                )
            }

            HomeAction.UpgradeClicked -> sendEvent(HomeEvent.NavigateToPaywall)
            HomeAction.ScoreCardClicked -> sendEvent(HomeEvent.NavigateToReports)
            HomeAction.SeeAllSessionsClicked -> sendEvent(HomeEvent.NavigateToReports)
            HomeAction.SeeAllInterviewsClicked -> sendEvent(HomeEvent.NavigateToInterviews)

            is HomeAction.InterviewTrackClicked -> sendEvent(
                HomeEvent.NavigateToReadyToPractice(
                    trackId = action.trackId,
                    trackName = action.trackName,
                )
            )

            is HomeAction.SessionClicked ->
                sendEvent(HomeEvent.NavigateToSessionDetails(action.sessionId))
        }
    }

    private fun observeProfile() {
        viewModelScope.launch {
            getUserProfile().collect { profile ->
                _state.update {
                    it.copy(
                        userName = profile.personal.displayName,
                        practiceTrackName = profile.career.trackName,
                    )
                }
                resolvePracticeTrackId(_state.value.availableInterviews)
            }
        }
    }

    private fun load(isRefresh: Boolean) {
        val current = _state.value
        if (current.isLoading || current.isRefreshing) return

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = !isRefresh,
                    isRefreshing = isRefresh,
                    error = null,
                )
            }

            when (val result = getInterviewSessions()) {
                is CareerPilotResult.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            error = result.error.toUIText(),
                        )
                    }
                    CareerPilotSnackbarController.show(result.error.toUIText())
                }

                is CareerPilotResult.Success -> applySessions(result.data)
            }

            loadInterviewTracks()
            applyPendingIntegrationPlaceholders()
        }
    }

    private suspend fun loadInterviewTracks() {
        val tracks = (getInterviewTracks() as? CareerPilotResult.Success)?.data ?: return

        _state.update { it.copy(availableInterviews = tracks.toImmutableList()) }
        resolvePracticeTrackId(tracks)
    }

    private fun applySessions(sessions: List<InterviewSession>) {
        _state.update {
            it.copy(
                isLoading = false,
                isRefreshing = false,
                recentSessions = sessions.take(RECENT_SESSIONS_COUNT).toImmutableList(),
                scoreSummary = getScoreSummary(sessions),
            )
        }
    }


    private fun resolvePracticeTrackId(tracks: List<InterviewTrack>) {
        val trackName = _state.value.practiceTrackName
        if (trackName.isBlank() || tracks.isEmpty() || _state.value.practiceTrackId != null) return

        val match = tracks.firstOrNull { track ->
            track.name.equals(trackName, ignoreCase = true)
        }

        _state.update { it.copy(practiceTrackId = match?.id) }
    }

    // TODO: provide coins and subscription state
    private fun applyPendingIntegrationPlaceholders() {
        _state.update {
            it.copy(
                coins = 0,
                trial = TrialUiState(
                    sessionsUsed = 0,
                    totalFreeSessions = 1,
                ),
            )
        }
    }

    private fun sendEvent(event: HomeEvent) {
        viewModelScope.launch { _events.send(event) }
    }

    private companion object {
        const val RECENT_SESSIONS_COUNT = 3
    }
}
