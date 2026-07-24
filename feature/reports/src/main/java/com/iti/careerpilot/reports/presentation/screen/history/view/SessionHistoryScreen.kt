package com.iti.careerpilot.reports.presentation.screen.history.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.core.designsystem.common.ObserveEvent
import com.iti.careerpilot.reports.R
import com.iti.careerpilot.reports.presentation.screen.components.ReportsEmptyContent
import com.iti.careerpilot.reports.presentation.screen.components.ReportsAnimatedContent
import com.iti.careerpilot.reports.presentation.screen.components.ReportsContentPhase
import com.iti.careerpilot.reports.presentation.screen.components.ReportsErrorContent
import com.iti.careerpilot.reports.presentation.screen.components.ReportsLoadingContent
import com.iti.careerpilot.reports.presentation.screen.history.contract.SessionHistoryAction
import com.iti.careerpilot.reports.presentation.screen.history.contract.SessionHistoryEvent
import com.iti.careerpilot.reports.presentation.screen.history.contract.SessionHistoryState
import com.iti.careerpilot.reports.presentation.screen.history.view.components.SessionHistoryCard
import com.iti.careerpilot.reports.presentation.screen.history.view.components.SessionHistorySummary
import com.iti.careerpilot.reports.presentation.screen.history.viewmodel.SessionHistoryViewModel

@Composable
fun SessionHistoryRoot(
    openSessionDetails: (String) -> Unit,
    viewModel: SessionHistoryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ObserveEvent(viewModel.events) { event ->
        when (event) {
            is SessionHistoryEvent.NavigateToSessionDetails -> openSessionDetails(event.sessionId)
        }
    }
    SessionHistoryScreen(
        state = state,
        onAction = viewModel::onAction,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SessionHistoryScreen(
    state: SessionHistoryState,
    onAction: (SessionHistoryAction) -> Unit,
    modifier: Modifier = Modifier,
) {
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
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { innerPadding ->
        val content = state.content
        val phase = when {
            state.error != null && content == null -> ReportsContentPhase.ERROR
            content == null -> ReportsContentPhase.LOADING
            content.sessions.isEmpty() -> ReportsContentPhase.EMPTY
            else -> ReportsContentPhase.CONTENT
        }
        ReportsAnimatedContent(
            phase = phase,
            modifier = Modifier.fillMaxSize(),
        ) { targetPhase ->
            when (targetPhase) {
                ReportsContentPhase.LOADING -> ReportsLoadingContent(
                    messageRes = R.string.reports_loading_history,
                    modifier = Modifier.padding(innerPadding),
                )

                ReportsContentPhase.ERROR -> ReportsErrorContent(
                    error = requireNotNull(state.error),
                    isOnline = state.isOnline,
                    onRetry = { onAction(SessionHistoryAction.Retry) },
                    modifier = Modifier.padding(innerPadding),
                )

                ReportsContentPhase.EMPTY -> ReportsEmptyContent(
                    titleRes = R.string.reports_empty_history_title,
                    messageRes = R.string.reports_empty_history_message,
                    modifier = Modifier.padding(innerPadding),
                )

                ReportsContentPhase.CONTENT -> {
                    val history = requireNotNull(content)
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentPadding = PaddingValues(
                            horizontal = Dimens.SpaceXL,
                            vertical = Dimens.SpaceS,
                        ),
                        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
                    ) {
                        item(key = "history-summary", contentType = "summary") {
                            SessionHistorySummary(
                                sessionCount = history.sessions.size,
                                averageScore = history.averageScore,
                            )
                        }
                        if (state.isLoading) {
                            item(key = "history-progress", contentType = "progress") {
                                Box(
                                    modifier = Modifier.fillParentMaxWidth(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    LoadingIndicator(color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                        items(
                            items = history.sessions,
                            key = { it.id },
                            contentType = { "session" },
                        ) { session ->
                            SessionHistoryCard(
                                session = session,
                                onClick = {
                                    onAction(SessionHistoryAction.SessionClicked(session.id))
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}