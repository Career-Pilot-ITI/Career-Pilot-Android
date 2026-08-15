package com.iti.careerpilot.home.presentation.home.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.core.designsystem.common.ObserveEvent
import com.iti.careerpilot.home.R
import com.iti.careerpilot.home.presentation.home.HomeAction
import com.iti.careerpilot.home.presentation.home.HomeEvent
import com.iti.careerpilot.home.presentation.home.HomeState
import com.iti.careerpilot.home.presentation.home.HomeViewModel
import com.iti.careerpilot.home.presentation.home.screen.components.EmptySessionsCard
import com.iti.careerpilot.home.presentation.home.screen.components.HomeHeader
import com.iti.careerpilot.home.presentation.home.screen.components.HomeShimmerLoading
import com.iti.careerpilot.home.presentation.home.screen.components.OverallScoreCard
import com.iti.careerpilot.home.presentation.home.screen.components.PracticeInterviewCard
import com.iti.careerpilot.home.presentation.home.screen.components.SectionHeader
import com.iti.careerpilot.home.presentation.home.screen.components.SessionRow
import com.iti.careerpilot.home.presentation.home.screen.components.SubscriptionCard
import com.iti.careerpilot.home.presentation.home.screen.components.rememberGreeting

@Composable
fun HomeRoot(
    openReadyToPractice: (trackId: Long, trackName: String) -> Unit,
    openQuiz: (trackId: Long, trackName: String) -> Unit,
    openSessionDetails: (Long) -> Unit,
    openPracticeSession: (trackId: Long, sessionId: Long) -> Unit,
    openInterviews: () -> Unit,
    openPlansPaywall: (showMySubscription: Boolean) -> Unit,
    openCoinsPaywall: () -> Unit,
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

            is HomeEvent.NavigateToQuiz ->
                openQuiz(event.trackId, event.trackName)

            is HomeEvent.NavigateToSessionDetails ->
                openSessionDetails(event.sessionId)

            is HomeEvent.NavigateToPracticeSession ->
                openPracticeSession(event.trackId, event.sessionId)

            HomeEvent.NavigateToInterviews -> openInterviews()
            is HomeEvent.NavigateToPlansPaywall -> openPlansPaywall(event.showMySubscription)
            HomeEvent.NavigateToCoinsPaywall -> openCoinsPaywall()
            HomeEvent.NavigateToReports -> openReports()
        }
    }

    HomeScreen(
        state = state,
        onAction = viewModel::onAction,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(
    state: HomeState,
    onAction: (HomeAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val pullToRefreshState = rememberPullToRefreshState()
    Scaffold(
        modifier = modifier,
        topBar = {
            HomeHeader(
                greeting = rememberGreeting(),
                userName = state.userName,
                coins = state.coins,
                onCoinsClick = {
                    onAction(HomeAction.CoinsClicked)
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.exclude(WindowInsets.navigationBars),
    ) { innerPadding ->
        if (state.isLoading) {
            HomeShimmerLoading(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else {
            PullToRefreshBox(
                state = pullToRefreshState,
                isRefreshing = state.isRefreshing,
                onRefresh = { onAction(HomeAction.Refresh) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                indicator = {
                    PullToRefreshDefaults.LoadingIndicator(
                        state = pullToRefreshState,
                        isRefreshing = state.isRefreshing,
                        modifier = Modifier.align(Alignment.TopCenter),
                        color = colors.primary,
                        containerColor = colors.surface,
                    )
                },
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        vertical = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXL),
                ) {

                    item {
                        SubscriptionCard(
                            planLabel = state.planLabel,
                            subscriptionTier = state.subscriptionTier,
                            onCardClick = { onAction(HomeAction.UpgradeClicked) },
                            modifier = Modifier
                                .padding(
                                    horizontal = 20.dp
                                )
                        )
                    }

                    item {
                        OverallScoreCard(
                            summary = state.scoreSummary,
                            onClick = { onAction(HomeAction.ScoreCardClicked) },
                            modifier = Modifier
                                .padding(
                                    horizontal = 20.dp
                                )
                        )
                    }
                    item {
                        SectionHeader(
                            title = stringResource(R.string.home_available_interviews),
                            actionLabel = stringResource(R.string.home_see_all),
                            onActionClick = { onAction(HomeAction.SeeAllInterviewsClicked) },
                            modifier = Modifier
                                .padding(
                                    horizontal = 20.dp
                                )
                        )
                    }
                    item {
                        PracticeInterviewCard(
                            trackName = state.practiceTrackName,
                            onInterviewClick = { onAction(HomeAction.PracticeInterviewClicked) },
                            onLessonClick = { onAction(HomeAction.LessonClicked) },
                            modifier = Modifier
                                .padding(
                                    horizontal = 20.dp
                                )
                        )
                    }

                    item {
                        SectionHeader(
                            title = stringResource(R.string.home_recent_sessions),
                            actionLabel = stringResource(R.string.home_see_all)
                                .takeIf { state.recentSessions.isNotEmpty() },
                            onActionClick = { onAction(HomeAction.SeeAllSessionsClicked) }
                                .takeIf { state.recentSessions.isNotEmpty() },
                            modifier = Modifier
                                .padding(
                                    horizontal = 20.dp
                                )
                        )
                    }

                    if (state.recentSessions.isEmpty()) {
                        item {
                            EmptySessionsCard(
                                onStartInterviewClick = { onAction(HomeAction.PracticeInterviewClicked) },
                                modifier = Modifier
                                    .padding(
                                        horizontal = 20.dp
                                    )
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
                                modifier = Modifier
                                    .padding(
                                        horizontal = 20.dp
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}
