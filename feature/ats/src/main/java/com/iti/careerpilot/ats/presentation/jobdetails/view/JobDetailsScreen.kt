package com.iti.careerpilot.ats.presentation.jobdetails.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iti.careerpilot.ats.R
import com.iti.careerpilot.ats.presentation.components.AtsCenteredTopBar
import com.iti.careerpilot.ats.presentation.components.AtsWorkspaceErrorContent
import com.iti.careerpilot.ats.presentation.components.AtsWorkspaceLoadingContent
import com.iti.careerpilot.ats.presentation.components.JobHeaderCard
import com.iti.careerpilot.ats.presentation.jobdetails.state.JobDetailsAction
import com.iti.careerpilot.ats.presentation.jobdetails.state.JobDetailsEffect
import com.iti.careerpilot.ats.presentation.jobdetails.state.JobDetailsUiState
import com.iti.careerpilot.ats.presentation.jobdetails.view.components.DescriptionCard
import com.iti.careerpilot.ats.presentation.jobdetails.view.components.JobOverviewCard
import com.iti.careerpilot.ats.presentation.jobdetails.view.components.RequirementsCard
import com.iti.careerpilot.ats.presentation.jobdetails.viewmodel.JobDetailsViewModel
import com.iti.careerpilot.ats.presentation.util.UiStateProvider
import com.iti.careerpilot.ats.presentation.util.rememberUiStateProvider
import com.iti.careerpilot.ats.presentation.util.rememberUiStateValue
import com.iti.careerpilot.core.designsystem.components.CareerPilotButton

@Composable
fun JobDetailsRoot(
    workspaceId: Long,
    onBack: () -> Unit,
    openScore: (Long) -> Unit,
    openJob: (String) -> Unit,
    viewModel: JobDetailsViewModel = hiltViewModel(),
) {
    val state = viewModel.state.collectAsStateWithLifecycle()
    val stateProvider = rememberUiStateProvider(state)

    LaunchedEffect(workspaceId) {
        viewModel.onAction(JobDetailsAction.Initial(workspaceId))
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is JobDetailsEffect.OpenScore -> openScore(effect.workspaceId)
            }
        }
    }

    JobDetailsScreen(
        stateProvider = stateProvider,
        onAction = viewModel::onAction,
        onBack = onBack,
        onOpenJob = openJob,
    )
}

@Composable
fun JobDetailsScreen(
    stateProvider: UiStateProvider<JobDetailsUiState>,
    onAction: (JobDetailsAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenJob: (String) -> Unit = {},
) {
    Column(modifier = modifier.fillMaxSize()) {
        AtsCenteredTopBar(
            title = stringResource(R.string.ats_job_description_title),
            onBack = onBack,
        )

        JobDetailsBody(
            stateProvider = stateProvider,
            onAction = onAction,
            onOpenJob = onOpenJob,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun JobDetailsBody(
    stateProvider: UiStateProvider<JobDetailsUiState>,
    onAction: (JobDetailsAction) -> Unit,
    onOpenJob: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val phase by rememberUiStateValue(stateProvider) { state ->
        when {
            state.isLoading -> JobDetailsPhase.LOADING
            state.workspace == null -> JobDetailsPhase.ERROR
            else -> JobDetailsPhase.CONTENT
        }
    }

    when (phase) {
        JobDetailsPhase.LOADING -> AtsWorkspaceLoadingContent(modifier = modifier)
        JobDetailsPhase.ERROR -> JobDetailsErrorContent(
            stateProvider = stateProvider,
            onAction = onAction,
            modifier = modifier,
        )
        JobDetailsPhase.CONTENT -> JobDetailsContent(
            stateProvider = stateProvider,
            onAction = onAction,
            onOpenJob = onOpenJob,
            modifier = modifier,
        )
    }
}

@Composable
private fun JobDetailsErrorContent(
    stateProvider: UiStateProvider<JobDetailsUiState>,
    onAction: (JobDetailsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val error by rememberUiStateValue(stateProvider) { it.error }
    AtsWorkspaceErrorContent(
        error = error,
        onRetry = { onAction(JobDetailsAction.Retry) },
        modifier = modifier,
    )
}

@Composable
private fun JobDetailsContent(
    stateProvider: UiStateProvider<JobDetailsUiState>,
    onAction: (JobDetailsAction) -> Unit,
    onOpenJob: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val workspace by rememberUiStateValue(stateProvider) { it.workspace }
    val currentWorkspace = workspace
    if (currentWorkspace == null) {
        val error by rememberUiStateValue(stateProvider) { it.error }
        AtsWorkspaceErrorContent(
            error = error,
            onRetry = { onAction(JobDetailsAction.Retry) },
            modifier = modifier,
        )
        return
    }

    Column(
        modifier = modifier
            .padding(horizontal = 20.dp)
            .padding(top = 8.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { Spacer(Modifier.height(2.dp)) }
            item { JobHeaderCard(job = currentWorkspace.job, onOpenJob = onOpenJob) }
            item { JobOverviewCard(job = currentWorkspace.job) }

            if (currentWorkspace.job.description.isNotBlank()) {
                item { DescriptionCard(currentWorkspace.job.description) }
            }
            if (
                currentWorkspace.job.requiredSkills.isNotEmpty() ||
                currentWorkspace.job.preferredSkills.isNotEmpty() ||
                currentWorkspace.job.technologies.isNotEmpty()
            ) {
                item { RequirementsCard(currentWorkspace.job) }
            }

            item { Spacer(Modifier.height(8.dp)) }
        }

        CareerPilotButton(
            text = stringResource(R.string.ats_start_scoring),
            onClick = { onAction(JobDetailsAction.StartScoring) },
        )
    }
}

private enum class JobDetailsPhase { LOADING, ERROR, CONTENT }
