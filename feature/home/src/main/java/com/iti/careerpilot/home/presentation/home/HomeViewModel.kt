package com.iti.careerpilot.home.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.core.access.domain.AccessRepository
import com.iti.careerpilot.core.access.domain.usecase.RefreshAccessUseCase
import com.iti.careerpilot.home.domain.model.InterviewSession
import com.iti.careerpilot.home.domain.usecase.GetInterviewSessionsUseCase
import com.iti.careerpilot.home.domain.usecase.GetScoreSummaryUseCase
import com.iti.careerpilot.home.domain.usecase.GetTracksUseCase
import com.iti.careerpilot.home.domain.usecase.GetUserProfileUseCase
import com.iti.common.result.onError
import com.iti.common.result.onSuccess
import com.iti.common.snackbar.CareerPilotSnackbarController
import com.iti.common.util.toUIText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
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
    private val refreshAccessUseCase: RefreshAccessUseCase,
    private val accessRepository: AccessRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    private val _events = Channel<HomeEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var hasInitialized = false
    private var profileObservationJob: Job? = null

    private fun initialize() {
        if (hasInitialized) return
        hasInitialized = true
        observeProfile()
        observeAccessState()
        load(isRefresh = false)
    }

    fun onAction(action: HomeAction) {
        when (action) {
            HomeAction.Initial -> initialize()
            HomeAction.Refresh -> load(isRefresh = true)

            HomeAction.PracticeInterviewClicked -> {
                val current = _state.value
                val trackId = current.practiceTrackId ?: 1L
                sendEvent(
                    HomeEvent.NavigateToReadyToPractice(
                        trackId = trackId,
                        trackName = current.practiceTrackName.ifBlank { "Android Developer" },
                    )
                )
            }

            HomeAction.LessonClicked -> {
                val current = _state.value
                val trackId = current.practiceTrackId ?: 1L
                sendEvent(
                    HomeEvent.NavigateToQuiz(
                        trackId = trackId,
                        trackName = current.practiceTrackName.ifBlank { "Android Developer" },
                    )
                )
            }

            HomeAction.UpgradeClicked -> {
                val currentTier = _state.value.subscriptionTier.uppercase()
                val isMax = currentTier in setOf("PRO", "MAX")
                sendEvent(HomeEvent.NavigateToPlansPaywall(showMySubscription = isMax))
            }

            HomeAction.CoinsClicked -> sendEvent(HomeEvent.NavigateToCoinsPaywall)
            HomeAction.ScoreCardClicked -> sendEvent(HomeEvent.NavigateToReports)
            HomeAction.AtsJobMatchClicked -> sendEvent(HomeEvent.NavigateToAts)
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

            is HomeAction.ResumeSessionClicked -> {
                val session = _state.value.recentSessions
                    .firstOrNull { it.id == action.sessionId }
                sendEvent(
                    HomeEvent.NavigateToPracticeSession(
                        trackId = session?.trackId ?: 0L,
                        sessionId = action.sessionId,
                    )
                )
            }
        }
    }

    private fun observeProfile() {
        if (profileObservationJob != null) return
        profileObservationJob = viewModelScope.launch {
            getUserProfile().collect { profile ->
                _state.update {
                    it.copy(
                        userName = profile.personal.displayName,
                        practiceTrackName = profile.career.trackName,
                        practiceTrackId = profile.career.trackId?.takeIf { id -> id != 0L },
                    )
                }
            }
        }
    }

    private fun observeAccessState() {
        viewModelScope.launch {
            accessRepository.accessState.collect { access ->
                _state.update {
                    it.copy(
                        coins = access.coinBalance,
                        subscriptionTier = access.plan.name,
                    )
                }
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
                )
            }
            coroutineScope {
                launch {
                    refreshAccessUseCase()
                }
                launch {
                    loadInterviewTracks()
                }
                launch {
                    getInterviewSessions()
                        .onSuccess { data ->
                            applySessions(data)
                        }
                        .onError { error ->
                            CareerPilotSnackbarController.show(error.toUIText())
                        }
                }
            }
            _state.update {
                it.copy(
                    isLoading = false,
                    isRefreshing = false,
                )
            }
        }
    }

    private suspend fun loadInterviewTracks() {
        getInterviewTracks().onSuccess { tracks ->
            _state.update { it.copy(availableInterviews = tracks.toImmutableList()) }
        }
    }

    private fun applySessions(sessions: List<InterviewSession>) {
        _state.update {
            it.copy(
                recentSessions = sessions.take(RECENT_SESSIONS_COUNT).toImmutableList(),
                scoreSummary = getScoreSummary(sessions),
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
