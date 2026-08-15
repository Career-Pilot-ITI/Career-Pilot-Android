package com.iti.careerpilot.reports.presentation.screen.history.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.core.designsystem.common.ObserveEvent
import com.iti.careerpilot.core.designsystem.components.ButtonVariant
import com.iti.careerpilot.core.designsystem.components.CareerPilotButton
import com.iti.careerpilot.reports.R
import com.iti.careerpilot.reports.presentation.screen.components.ReportsAnimatedContent
import com.iti.careerpilot.reports.presentation.screen.components.ReportsContentPhase
import com.iti.careerpilot.reports.presentation.screen.components.ReportsEmptyContent
import com.iti.careerpilot.reports.presentation.screen.components.ReportsErrorContent
import com.iti.careerpilot.reports.presentation.screen.components.SessionHistoryShimmerContent
import com.iti.careerpilot.reports.presentation.screen.history.contract.SessionHistoryAction
import com.iti.careerpilot.reports.presentation.screen.history.contract.SessionHistoryEvent
import com.iti.careerpilot.reports.presentation.screen.history.contract.SessionHistoryState
import com.iti.careerpilot.reports.presentation.screen.history.paging.SessionHistoryPagingException
import com.iti.careerpilot.reports.presentation.screen.history.uimodels.SessionSummaryUiModel
import com.iti.careerpilot.reports.presentation.screen.history.view.components.SessionHistoryCard
import com.iti.careerpilot.reports.presentation.screen.history.view.components.SessionHistorySummary
import com.iti.careerpilot.reports.presentation.screen.history.viewmodel.SessionHistoryViewModel
import com.iti.common.error.NetworkError
import com.iti.common.util.UIText
import com.iti.common.util.toUIText

@Composable
fun SessionHistoryRoot(
    openSessionDetails: (Long) -> Unit,
    viewModel: SessionHistoryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val sessions = viewModel.sessions.collectAsLazyPagingItems()
    ObserveEvent(viewModel.events) { event ->
        when (event) {
            is SessionHistoryEvent.NavigateToSessionDetails -> openSessionDetails(event.sessionId)
        }
    }
    SessionHistoryScreen(
        state = state,
        sessions = sessions,
        onAction = viewModel::onAction,
    )
}

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class,
)
@Composable
fun SessionHistoryScreen(
    state: SessionHistoryState,
    sessions: LazyPagingItems<SessionSummaryUiModel>,
    onAction: (SessionHistoryAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pullToRefreshState = rememberPullToRefreshState()
    val refreshState = sessions.loadState.refresh
    val hasContent = sessions.itemCount > 0
    val phase = when {
        refreshState is LoadState.Loading && !hasContent -> ReportsContentPhase.LOADING
        refreshState is LoadState.Error && !hasContent -> ReportsContentPhase.ERROR
        refreshState is LoadState.NotLoading && !hasContent -> ReportsContentPhase.EMPTY
        else -> ReportsContentPhase.CONTENT
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.reports_session_history),
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                ),
                windowInsets = TopAppBarDefaults.windowInsets.exclude(WindowInsets.statusBars),
            )
        },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.exclude(WindowInsets.navigationBars),
    ) { innerPadding ->
        PullToRefreshBox(
            state = pullToRefreshState,
            isRefreshing = refreshState is LoadState.Loading && hasContent,
            onRefresh = sessions::refresh,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            indicator = {
                PullToRefreshDefaults.LoadingIndicator(
                    state = pullToRefreshState,
                    isRefreshing = refreshState is LoadState.Loading && hasContent,
                    modifier = Modifier.align(Alignment.TopCenter),
                    color = MaterialTheme.colorScheme.primary,
                    containerColor = MaterialTheme.colorScheme.surface,
                )
            },
        ) {
            ReportsAnimatedContent(
                targetState = phase,
                contentKey = { it },
                modifier = Modifier.fillMaxSize(),
            ) { contentPhase ->
                when (contentPhase) {
                    ReportsContentPhase.LOADING -> SessionHistoryShimmerContent()

                    ReportsContentPhase.ERROR -> {
                        val error = (refreshState as? LoadState.Error)?.error
                        ReportsErrorContent(
                            error = error.toReportUIText(),
                            isOnline = state.isOnline,
                            onRetry = sessions::retry,
                        )
                    }

                    ReportsContentPhase.EMPTY -> ReportsEmptyContent(
                        titleRes = R.string.reports_empty_history_title,
                        messageRes = R.string.reports_empty_history_message,
                    )

                    ReportsContentPhase.CONTENT -> SessionHistoryContent(
                        sessions = sessions,
                        onAction = onAction,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SessionHistoryContent(
    sessions: LazyPagingItems<SessionSummaryUiModel>,
    onAction: (SessionHistoryAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val loadedSessions = sessions.itemSnapshotList.items
    val averageScore = remember {
        if (loadedSessions.isEmpty()) {
            0
        } else {
            loadedSessions.sumOf(SessionSummaryUiModel::score) / loadedSessions.size
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = Dimens.SpaceXL,
            vertical = Dimens.SpaceS,
        ),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
    ) {
        item(key = "history-summary", contentType = "summary") {
            SessionHistorySummary(
                sessionCount = loadedSessions.size,
                averageScore = averageScore,
            )
        }
        items(
            count = sessions.itemCount,
            key = sessions.itemKey(SessionSummaryUiModel::id),
            contentType = sessions.itemContentType { "session" },
        ) { index ->
            val session = sessions[index] ?: return@items
            SessionHistoryCard(
                session = session,
                onClick = {
                    onAction(SessionHistoryAction.SessionClicked(session.id))
                },
            )
        }
        when (sessions.loadState.append) {
            is LoadState.Loading -> item(
                key = "history-append-progress",
                contentType = "append-progress",
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.SpaceM),
                    contentAlignment = Alignment.Center,
                ) {
                    LoadingIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            is LoadState.Error -> item(
                key = "history-append-retry",
                contentType = "append-retry",
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    CareerPilotButton(
                        text = stringResource(R.string.reports_retry),
                        onClick = sessions::retry,
                        variant = ButtonVariant.OUTLINE,
                    )
                }
            }

            is LoadState.NotLoading -> Unit
        }
    }
}

private fun Throwable?.toReportUIText(): UIText {
    val networkError = (this as? SessionHistoryPagingException)?.error ?: NetworkError.UNKNOWN
    return networkError.toUIText()
}
