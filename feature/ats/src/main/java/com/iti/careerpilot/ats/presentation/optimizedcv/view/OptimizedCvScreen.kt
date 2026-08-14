package com.iti.careerpilot.ats.presentation.optimizedcv.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iti.careerpilot.ats.R
import com.iti.careerpilot.ats.presentation.components.AtsCenteredTopBar
import com.iti.careerpilot.ats.presentation.optimizedcv.state.OptimizedCvAction
import com.iti.careerpilot.ats.presentation.optimizedcv.state.OptimizedCvUiState
import com.iti.careerpilot.ats.presentation.optimizedcv.view.components.OptimizedCvContent
import com.iti.careerpilot.ats.presentation.optimizedcv.viewmodel.OptimizedCvViewModel
import com.iti.careerpilot.ats.presentation.util.UiStateProvider
import com.iti.careerpilot.ats.presentation.util.rememberUiStateProvider
import com.iti.careerpilot.ats.presentation.util.rememberUiStateValue

@Composable
fun OptimizedCvRoot(
    jobId: Long,
    onBack: () -> Unit,
    viewModel: OptimizedCvViewModel = hiltViewModel(),
) {
    val state = viewModel.state.collectAsStateWithLifecycle()
    val stateProvider = rememberUiStateProvider(state)

    LaunchedEffect(jobId) {
        viewModel.onAction(OptimizedCvAction.Initial(jobId))
    }

    OptimizedCvScreen(
        stateProvider = stateProvider,
        onAction = viewModel::onAction,
        onBack = onBack,
        modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars),
    )
}

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun OptimizedCvScreen(
    stateProvider: UiStateProvider<OptimizedCvUiState>,
    onAction: (OptimizedCvAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        AtsCenteredTopBar(
            title = stringResource(R.string.optimized_cv),
            onBack = onBack,
        )
        OptimizedCvBody(
            stateProvider = stateProvider,
            onAction = onAction,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun OptimizedCvBody(
    stateProvider: UiStateProvider<OptimizedCvUiState>,
    onAction: (OptimizedCvAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isLoading by rememberUiStateValue(stateProvider) { it.isLoading }
    if (isLoading) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center,
        ) {
            CircularWavyProgressIndicator()
        }
    } else {
        OptimizedCvContent(
            stateProvider = stateProvider,
            onAction = onAction,
            modifier = modifier,
        )
    }
}
