package com.iti.careerpilot.home.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.core.interviews.domain.model.InterviewSession
import com.iti.careerpilot.home.R
import com.iti.careerpilot.challengefirestore.ChallengeFirestoreDataSource
import com.iti.careerpilot.home.domain.model.InterviewTrack
import com.iti.careerpilot.home.domain.usecase.GetInterviewSessionsUseCase
import com.iti.careerpilot.home.domain.usecase.GetScoreSummaryUseCase
import com.iti.careerpilot.home.domain.usecase.GetTracksUseCase
import com.iti.careerpilot.home.domain.usecase.GetUserProfileUseCase
import com.iti.common.error.NetworkError
import com.iti.common.result.onError
import com.iti.common.result.onSuccess
import com.iti.common.snackbar.CareerPilotSnackbarController
import com.iti.common.util.UIText
import com.iti.common.util.toUIText
import com.iti.core.datastore.sync.UserProfileSync
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
    private val userProfileSync: UserProfileSync,
    private val challengeFirestoreDataSource: ChallengeFirestoreDataSource,
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
        load(isRefresh = false)
//        only to add fake firestore challenges
//        viewModelScope.launch {
//            challengeFirestoreDataSource.addFakeData()
//        }
    }

    private var hasEnteredScreen = false

    fun onScreenEntered() {
        if (hasEnteredScreen) {
            load(isRefresh = true)
        } else {
            hasEnteredScreen = true
        }
    }

    fun onAction(action: HomeAction) {
        when (action) {
            HomeAction.Initial -> initialize()
            HomeAction.Refresh -> load(isRefresh = true)

            HomeAction.PracticeInterviewClicked -> {
                val current = _state.value
                val trackId = current.practiceTrackId ?: return promptForTrack()
                sendEvent(
                    HomeEvent.NavigateToReadyToPractice(
                        trackId = trackId,
                        trackName = current.practiceTrackName,
                    )
                )
            }

            HomeAction.LessonClicked -> {
                val current = _state.value
                val trackId = current.practiceTrackId ?: return promptForTrack()
                sendEvent(
                    HomeEvent.NavigateToQuiz(
                        trackId = trackId,
                        trackName = current.practiceTrackName,
                    )
                )
            }

            HomeAction.UpgradeClicked -> sendEvent(HomeEvent.NavigateToPlansPaywall)
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
                        coins = profile.account.coinBalance,
                        subscriptionTier = profile.account.subscriptionTier,
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

            var tracksResult: List<InterviewTrack>? = null
            var sessionsResult: List<InterviewSession>? = null
            var sessionsError: NetworkError? = null

            coroutineScope {
                launch { userProfileSync.syncWalletBalance() }
                launch { userProfileSync.syncSubscriptionTier() }
                launch {
                    getInterviewTracks().onSuccess { tracks ->
                        tracksResult = tracks
                    }
                }
                launch {
                    getInterviewSessions()
                        .onSuccess { data ->
                            sessionsResult = data
                        }
                        .onError { error ->
                            sessionsError = error
                        }
                }
            }

            _state.update { state ->
                var updated = state
                tracksResult?.let { tracks ->
                    updated = updated.copy(availableInterviews = tracks.toImmutableList())
                }
                sessionsResult?.let { sessions ->
                    updated = updated.copy(
                        recentSessions = sessions.take(RECENT_SESSIONS_COUNT).toImmutableList(),
                        scoreSummary = getScoreSummary(sessions),
                    )
                }
                updated.copy(
                    isLoading = false,
                    isRefreshing = false,
                )
            }

            sessionsError?.let { error ->
                CareerPilotSnackbarController.show(error.toUIText())
            }
        }
    }

    private fun sendEvent(event: HomeEvent) {
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

