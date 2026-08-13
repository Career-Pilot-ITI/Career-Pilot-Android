package com.iti.careerpilot.ats.presentation.workspace.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.iti.careerpilot.ats.R
import com.iti.careerpilot.ats.presentation.components.AtsCenteredTopBar
import com.iti.careerpilot.ats.presentation.components.AtsWorkspaceErrorContent
import com.iti.careerpilot.ats.presentation.components.AtsWorkspaceLoadingContent
import com.iti.careerpilot.ats.presentation.jobdetails.view.JobDetailsScreen
import com.iti.careerpilot.ats.presentation.scoring.state.ScoringAction
import com.iti.careerpilot.ats.presentation.scoring.state.ScoringEffect
import com.iti.careerpilot.ats.presentation.scoring.state.ScoringUiState
import com.iti.careerpilot.ats.presentation.scoring.view.ScoringScreen
import com.iti.careerpilot.ats.presentation.scoring.viewmodel.ScoringViewModel

@Composable
fun AtsWorkspaceRoot(
    workspaceId: Long,
    onBack: () -> Unit,
    openCoinsPaywall: () -> Unit,
    openCoverLetter: (Long) -> Unit,
    openOptimizedCv: (Long) -> Unit,
    openReadyToPractice: (trackId: Long, trackName: String, workspaceId: Long) -> Unit,
    openJob: (String) -> Unit,
    viewModel: ScoringViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(workspaceId) {
        viewModel.onAction(ScoringAction.Initial(workspaceId))
    }

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    ScoringEffect.OpenCoinsPaywall -> openCoinsPaywall()
                    is ScoringEffect.OpenCoverLetter -> openCoverLetter(effect.workspaceId)
                    is ScoringEffect.OpenOptimizedCv -> openOptimizedCv(effect.workspaceId)
                    is ScoringEffect.OpenPractice -> openReadyToPractice(
                        effect.trackId,
                        effect.trackName,
                        effect.workspaceId,
                    )
                }
            }
        }
    }

    AtsWorkspaceScreen(
        state = state,
        onAction = viewModel::onAction,
        onBack = onBack,
        onOpenJob = openJob,
    )
}

@Composable
internal fun AtsWorkspaceScreen(
    state: ScoringUiState,
    onAction: (ScoringAction) -> Unit,
    onBack: () -> Unit,
    onOpenJob: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        state.isLoading -> Column(modifier = modifier.fillMaxSize()) {
            AtsCenteredTopBar(
                title = stringResource(R.string.ats_job_description_title),
                onBack = onBack,
            )
            AtsWorkspaceLoadingContent(modifier = Modifier.fillMaxSize())
        }

        state.workspace == null -> Column(modifier = modifier.fillMaxSize()) {
            AtsCenteredTopBar(
                title = stringResource(R.string.ats_job_description_title),
                onBack = onBack,
            )
            AtsWorkspaceErrorContent(
                state = state,
                onAction = onAction,
                modifier = Modifier.fillMaxSize(),
            )
        }

        state.score == null -> JobDetailsScreen(
            state = state,
            onAction = onAction,
            onBack = onBack,
            onOpenJob = onOpenJob,
            modifier = modifier,
        )

        else -> ScoringScreen(
            state = state,
            onAction = onAction,
            onBack = onBack,
            onOpenJob = onOpenJob,
            modifier = modifier,
        )
    }
}
