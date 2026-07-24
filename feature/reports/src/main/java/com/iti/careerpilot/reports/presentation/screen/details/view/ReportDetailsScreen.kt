package com.iti.careerpilot.reports.presentation.screen.details.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.core.designsystem.common.ObserveEvent
import com.iti.careerpilot.reports.R
import com.iti.careerpilot.reports.presentation.screen.components.ReportsAnimatedContent
import com.iti.careerpilot.reports.presentation.screen.components.ReportsContentPhase
import com.iti.careerpilot.reports.presentation.screen.components.ReportsErrorContent
import com.iti.careerpilot.reports.presentation.screen.components.ReportsLoadingContent
import com.iti.careerpilot.reports.presentation.screen.details.contract.ReportDetailsAction
import com.iti.careerpilot.reports.presentation.screen.details.contract.ReportDetailsEvent
import com.iti.careerpilot.reports.presentation.screen.details.contract.ReportDetailsState
import com.iti.careerpilot.reports.presentation.screen.details.view.components.CoachingSuggestionCard
import com.iti.careerpilot.reports.presentation.screen.details.view.components.PerformanceBreakdownCard
import com.iti.careerpilot.reports.presentation.screen.details.view.components.PerformanceSummaryCard
import com.iti.careerpilot.reports.presentation.screen.details.view.components.ReportBreakdownNavigationCard
import com.iti.careerpilot.reports.presentation.screen.details.viewmodel.ReportDetailsViewModel

@Composable
fun ReportDetailsRoot(
    sessionId: Long,
    navigateBack: () -> Unit,
    openQuestionBreakdown: (Long) -> Unit,
    viewModel: ReportDetailsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(sessionId) {
        viewModel.onAction(ReportDetailsAction.Load(sessionId))
    }
    ObserveEvent(viewModel.events) { event ->
        when (event) {
            ReportDetailsEvent.NavigateBack -> navigateBack()
            is ReportDetailsEvent.NavigateToQuestionBreakdown -> {
                openQuestionBreakdown(event.sessionId)
            }
        }
    }
    ReportDetailsScreen(state = state, onAction = viewModel::onAction)
}

@Composable
fun ReportDetailsScreen(
    state: ReportDetailsState,
    onAction: (ReportDetailsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(bottom = 0.dp),
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { onAction(ReportDetailsAction.BackClicked) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = stringResource(R.string.reports_back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { innerPadding ->
        ReportsAnimatedContent(
            targetState = state,
            contentKey = ReportDetailsState::phase,
            modifier = Modifier.fillMaxSize(),
        ) { animatedState ->
            when (animatedState.phase) {
                ReportsContentPhase.LOADING -> ReportsLoadingContent(
                    messageRes = R.string.reports_loading_details,
                    modifier = Modifier.padding(innerPadding),
                )

                ReportsContentPhase.ERROR -> {
                    val error = animatedState.error
                    if (error != null) {
                        ReportsErrorContent(
                            error = error,
                            isOnline = animatedState.isOnline,
                            onRetry = { onAction(ReportDetailsAction.Retry) },
                            modifier = Modifier.padding(innerPadding),
                        )
                    } else {
                        ReportsLoadingContent(
                            messageRes = R.string.reports_loading_details,
                            modifier = Modifier.padding(innerPadding),
                        )
                    }
                }

                ReportsContentPhase.CONTENT -> {
                    val report = animatedState.content
                    if (report != null) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            contentPadding = PaddingValues(
                                bottom = Dimens.SpaceXL,
                                top = 0.dp,
                                end = Dimens.SpaceXL,
                                start = Dimens.SpaceXL,
                            ),
                            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceL),
                        ) {
                            item(key = "performance-summary", contentType = "summary") {
                                PerformanceSummaryCard(report)
                            }
                            item(key = "performance-breakdown", contentType = "breakdown") {
                                PerformanceBreakdownCard(report.metrics)
                            }
                            item(key = "question-breakdown-navigation", contentType = "navigation") {
                                ReportBreakdownNavigationCard(
                                    onClick = {
                                        onAction(ReportDetailsAction.QuestionBreakdownClicked)
                                    },
                                )
                            }
                            item(key = "coaching-heading", contentType = "heading") {
                                Text(
                                    text = stringResource(R.string.reports_coaching_suggestions),
                                    style = MaterialTheme.typography.titleMedium,
                                )
                            }
                            items(
                                items = report.coachingSuggestions,
                                key = { it.id },
                                contentType = { "coaching-suggestion" },
                            ) { suggestion ->
                                CoachingSuggestionCard(suggestion)
                            }
                            item(key = "bottom-space", contentType = "bottom space") {
                                Spacer(Modifier.height(40.dp))
                            }
                        }
                    }
                }

                ReportsContentPhase.EMPTY -> Unit
            }
        }
    }
}
