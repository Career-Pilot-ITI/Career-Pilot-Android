package com.iti.careerpilot.reports.presentation.screen.breakdown.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.core.designsystem.common.ObserveEvent
import com.iti.careerpilot.reports.R
import com.iti.careerpilot.reports.presentation.screen.breakdown.view.component.CoachFeedbackCard
import com.iti.careerpilot.reports.presentation.screen.breakdown.viewmodel.QuestionBreakdownViewModel
import com.iti.careerpilot.reports.presentation.screen.breakdown.view.component.QuestionMetricsRow
import com.iti.careerpilot.reports.presentation.screen.breakdown.view.component.QuestionPromptCard
import com.iti.careerpilot.reports.presentation.screen.breakdown.view.component.QuestionSelector
import com.iti.careerpilot.reports.presentation.screen.breakdown.view.component.TranscriptCard
import com.iti.careerpilot.reports.presentation.screen.breakdown.contract.QuestionBreakdownAction
import com.iti.careerpilot.reports.presentation.screen.breakdown.contract.QuestionBreakdownEvent
import com.iti.careerpilot.reports.presentation.screen.breakdown.contract.QuestionBreakdownState
import com.iti.careerpilot.reports.presentation.screen.components.ReportsEmptyContent
import com.iti.careerpilot.reports.presentation.screen.components.ReportsAnimatedContent
import com.iti.careerpilot.reports.presentation.screen.components.ReportsContentPhase
import com.iti.careerpilot.reports.presentation.screen.components.ReportsErrorContent
import com.iti.careerpilot.reports.presentation.screen.components.ReportsLoadingContent

@Composable
fun QuestionBreakdownRoot(
    sessionId: String,
    navigateBack: () -> Unit,
    viewModel: QuestionBreakdownViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(sessionId) {
        viewModel.onAction(QuestionBreakdownAction.Load(sessionId))
    }
    ObserveEvent(viewModel.events) { event ->
        when (event) {
            QuestionBreakdownEvent.NavigateBack -> navigateBack()
        }
    }
    QuestionBreakdownScreen(state = state, onAction = viewModel::onAction)
}

@Composable
fun QuestionBreakdownScreen(
    state: QuestionBreakdownState,
    onAction: (QuestionBreakdownAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { onAction(QuestionBreakdownAction.BackClicked) }) {
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
        val content = state.content
        val selectedQuestion = state.selectedQuestion
        val phase = when {
            state.error != null && content == null -> ReportsContentPhase.ERROR
            content == null -> ReportsContentPhase.LOADING
            content.questions.isEmpty() || selectedQuestion == null -> ReportsContentPhase.EMPTY
            else -> ReportsContentPhase.CONTENT
        }
        ReportsAnimatedContent(
            phase = phase,
            modifier = Modifier.fillMaxSize(),
        ) { targetPhase ->
            when (targetPhase) {
                ReportsContentPhase.LOADING -> ReportsLoadingContent(
                    messageRes = R.string.reports_loading_questions,
                    modifier = Modifier.padding(innerPadding),
                )

                ReportsContentPhase.ERROR -> ReportsErrorContent(
                    error = requireNotNull(state.error),
                    isOnline = state.isOnline,
                    onRetry = { onAction(QuestionBreakdownAction.Retry) },
                    modifier = Modifier.padding(innerPadding),
                )

                ReportsContentPhase.EMPTY -> {
                    ReportsEmptyContent(
                        titleRes = R.string.reports_empty_questions_title,
                        messageRes = R.string.reports_empty_questions_message,
                        modifier = Modifier.padding(innerPadding),
                    )
                }

                ReportsContentPhase.CONTENT -> {
                    val breakdown = requireNotNull(content)
                    val question = requireNotNull(selectedQuestion)
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentPadding = PaddingValues(Dimens.SpaceXL),
                        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceL),
                    ) {
                        item(key = "question-selector", contentType = "selector") {
                            QuestionSelector(
                                questions = breakdown.questions,
                                selectedQuestionId = state.selectedQuestionId,
                                onQuestionSelected = { questionId ->
                                    onAction(QuestionBreakdownAction.QuestionSelected(questionId))
                                },
                            )
                        }
                        item(key = "question-${question.id}", contentType = "question") {
                            QuestionPromptCard(question)
                        }
                        item(key = "metrics-${question.id}", contentType = "metrics") {
                            QuestionMetricsRow(question)
                        }
                        item(key = "feedback-${question.id}", contentType = "feedback") {
                            CoachFeedbackCard(question.coachFeedback)
                        }
                        item(key = "transcript-${question.id}", contentType = "transcript") {
                            TranscriptCard(
                                transcript = question.transcript,
                                fillerWords = question.fillerWords,
                            )
                        }
                    }
                }
            }
        }
    }
}
