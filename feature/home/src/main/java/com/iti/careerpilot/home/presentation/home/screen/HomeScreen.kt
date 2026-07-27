package com.iti.careerpilot.home.presentation.home.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.core.designsystem.common.ObserveEvent
import com.iti.careerpilot.core.designsystem.components.LoadingWave
import com.iti.careerpilot.home.R
import com.iti.careerpilot.home.presentation.home.HomeAction
import com.iti.careerpilot.home.presentation.home.HomeEvent
import com.iti.careerpilot.home.presentation.home.HomeState
import com.iti.careerpilot.home.presentation.home.HomeViewModel
import com.iti.careerpilot.home.presentation.home.screen.components.EmptySessionsCard
import com.iti.careerpilot.home.presentation.home.screen.components.FreeTrialCard
import com.iti.careerpilot.home.presentation.home.screen.components.HomeHeader
import com.iti.careerpilot.home.presentation.home.screen.components.InterviewTrackCard
import com.iti.careerpilot.home.presentation.home.screen.components.OverallScoreCard
import com.iti.careerpilot.home.presentation.home.screen.components.PracticeInterviewCard
import com.iti.careerpilot.home.presentation.home.screen.components.SectionHeader
import com.iti.careerpilot.home.presentation.home.screen.components.SessionRow
import com.iti.careerpilot.home.presentation.home.screen.components.rememberGreeting

@Composable
fun HomeRoot(
    openReadyToPractice: (trackId: Long, trackName: String) -> Unit,
    openSessionDetails: (Long) -> Unit,
    openPracticeSession: (trackId: Long, sessionId: Long) -> Unit,
    openInterviews: () -> Unit,
    openPaywall: () -> Unit,
    openReports: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var isInitialResume by remember { mutableStateOf(true) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (isInitialResume) {
            isInitialResume = false
        } else {
            viewModel.onAction(HomeAction.Refresh)
        }
    }

    ObserveEvent(viewModel.events) { event ->
        when (event) {
            is HomeEvent.NavigateToReadyToPractice ->
                openReadyToPractice(event.trackId, event.trackName)

            is HomeEvent.NavigateToSessionDetails ->
                openSessionDetails(event.sessionId)

            is HomeEvent.NavigateToPracticeSession ->
                openPracticeSession(event.trackId, event.sessionId)

            HomeEvent.NavigateToInterviews -> openInterviews()
            HomeEvent.NavigateToPaywall -> openPaywall()
            HomeEvent.NavigateToReports -> openReports()
        }
    }

    HomeScreen(
        state = state,
        onAction = viewModel::onAction,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeState,
    onAction: (HomeAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets
            .exclude(WindowInsets.navigationBars),
    ) { innerPadding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                LoadingWave(color = MaterialTheme.colorScheme.primary)
            }
            return@Scaffold
        }

        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { onAction(HomeAction.Refresh) },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = Dimens.SpaceXXL,
                    end = Dimens.SpaceXXL,
                    top = Dimens.SpaceXXL,
                    bottom = Dimens.SpaceXXXXL,
                ),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXL),
            ) {
                item {
                    HomeHeader(
                        greeting = rememberGreeting(),
                        userName = state.userName,
                        coins = state.coins,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                state.trial?.let { trial ->
                    item {
                        FreeTrialCard(
                            trial = trial,
                            onUpgradeClick = { onAction(HomeAction.UpgradeClicked) },
                        )
                    }
                }

                item {
                    OverallScoreCard(
                        summary = state.scoreSummary,
                        onClick = { onAction(HomeAction.ScoreCardClicked) },
                    )
                }

                item {
                    PracticeInterviewCard(
                        trackName = state.practiceTrackName,
                        enabled = state.canStartPractice,
                        onClick = { onAction(HomeAction.PracticeInterviewClicked) },
                    )
                }

                if (state.availableInterviews.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = stringResource(R.string.home_available_interviews),
                            actionLabel = stringResource(R.string.home_see_all),
                            onActionClick = { onAction(HomeAction.SeeAllInterviewsClicked) },
                        )
                    }

                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            items(
                                items = state.availableInterviews,
                                key = { track -> track.id },
                            ) { track ->
                                InterviewTrackCard(
                                    track = track,
                                    onClick = {
                                        onAction(
                                            HomeAction.InterviewTrackClicked(
                                                trackId = track.id,
                                                trackName = track.name,
                                            )
                                        )
                                    },
                                )
                            }
                        }
                    }
                }

                item {
                    SectionHeader(
                        title = stringResource(R.string.home_recent_sessions),
                        actionLabel = stringResource(R.string.home_see_all)
                            .takeIf { state.recentSessions.isNotEmpty() },
                        onActionClick = { onAction(HomeAction.SeeAllSessionsClicked) }
                            .takeIf { state.recentSessions.isNotEmpty() },
                    )
                }

                if (state.recentSessions.isEmpty()) {
                    item {
                        EmptySessionsCard(
                            onStartInterviewClick = { onAction(HomeAction.PracticeInterviewClicked) },
                            isActionEnabled = state.canStartPractice,
                        )
                    }
                } else {
                    items(
                        items = state.recentSessions,
                        key = { session -> session.id },
                    ) { session ->
                        SessionRow(
                            session = session,
                            onClick = { onAction(HomeAction.SessionClicked(session.id)) },
                            onResume = { onAction(HomeAction.ResumeSessionClicked(session.id)) },
                        )
                    }
                }
            }
        }
    }
}
