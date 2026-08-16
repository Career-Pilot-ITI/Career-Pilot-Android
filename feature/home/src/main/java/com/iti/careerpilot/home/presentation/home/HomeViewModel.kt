package com.iti.careerpilot.home.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.core.interviews.domain.model.InterviewSession
import com.iti.careerpilot.home.R
import com.iti.careerpilot.home.domain.model.InterviewTrack
import com.iti.careerpilot.core.access.domain.AccessRepository
import com.iti.careerpilot.core.access.domain.usecase.RefreshAccessUseCase
import com.iti.careerpilot.home.domain.usecase.GetInterviewSessionsUseCase
import com.iti.careerpilot.home.domain.usecase.GetScoreSummaryUseCase
import com.iti.careerpilot.home.domain.usecase.GetTracksUseCase
import com.iti.careerpilot.home.domain.usecase.GetUserProfileUseCase
import com.iti.common.result.onError
import com.iti.common.result.onSuccess
import com.iti.common.snackbar.CareerPilotSnackbarController
import com.iti.common.util.UIText
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

    private val _events = Channel<HomeEffect>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var hasInitialized = false
    private var profileObservationJob: Job? = null

    init {
        observeProfile()
        observeAccessState()
        load(isRefresh = false)
    }

    private fun initialize() {
        if (hasInitialized) return
        hasInitialized = true
        load(isRefresh = false)
    }

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            HomeIntent.Initial -> initialize()
            HomeIntent.Refresh -> load(isRefresh = true)

            HomeIntent.PracticeInterviewClicked -> {
                val current = _state.value
                val trackId = current.practiceTrackId ?: return promptForTrack()
                sendEvent(
                    HomeEffect.NavigateToReadyToPractice(
                        trackId = trackId,
                        trackName = current.practiceTrackName,
                    )
                )
            }

            HomeIntent.LessonClicked -> {
                val current = _state.value
                val trackId = current.practiceTrackId ?: return promptForTrack()
                sendEvent(
                    HomeEffect.NavigateToQuiz(
                        trackId = trackId,
                        trackName = current.practiceTrackName,
                    )
                )
            }

            HomeIntent.UpgradeClicked -> {
                val currentTier = _state.value.subscriptionTier.uppercase()
                val isMax = currentTier in setOf("PRO", "MAX")
                sendEvent(HomeEffect.NavigateToPlansPaywall(showMySubscription = isMax))
            }

            HomeIntent.CoinsClicked -> sendEvent(HomeEffect.NavigateToCoinsPaywall)
            HomeIntent.ScoreCardClicked -> sendEvent(HomeEffect.NavigateToReports)
            HomeIntent.AtsJobMatchClicked -> {
                if (_state.value.isSubscribed) {
                    sendEvent(HomeEffect.NavigateToAts)
                } else {
                    sendEvent(HomeEffect.NavigateToPlansPaywall(showMySubscription = false))
                }
            }
            HomeIntent.SeeAllSessionsClicked -> sendEvent(HomeEffect.NavigateToReports)
            HomeIntent.SeeAllInterviewsClicked -> sendEvent(HomeEffect.NavigateToInterviews)

            is HomeIntent.InterviewTrackClicked -> sendEvent(
                HomeEffect.NavigateToReadyToPractice(
                    trackId = intent.trackId,
                    trackName = intent.trackName,
                )
            )

            is HomeIntent.SessionClicked ->
                sendEvent(HomeEffect.NavigateToSessionDetails(intent.sessionId))

            is HomeIntent.ResumeSessionClicked -> {
                val session = _state.value.recentSessions
                    .firstOrNull { it.id == intent.sessionId }
                sendEvent(
                    HomeEffect.NavigateToPracticeSession(
                        trackId = session?.trackId ?: 0L,
                        sessionId = intent.sessionId,
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

    private fun sendEvent(event: HomeEffect) {
        viewModelScope.launch { _events.send(event) }
    }

    private fun promptForTrack() {
        viewModelScope.launch {
            CareerPilotSnackbarController.show(
                UIText.StringResource(R.string.home_no_track_selected),
            )
        }
    }

    private companion object {
        const val RECENT_SESSIONS_COUNT = 3
    }
}
