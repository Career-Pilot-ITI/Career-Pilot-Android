package com.iti.careerpilot.ats.presentation.scoring.view

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.iti.careerpilot.ats.R
import com.iti.careerpilot.ats.background.CvOptimizationService
import com.iti.careerpilot.ats.presentation.components.AtsCenteredTopBar
import com.iti.careerpilot.ats.presentation.scoring.state.ScoringAction
import com.iti.careerpilot.ats.presentation.scoring.state.ScoringEffect
import com.iti.careerpilot.ats.presentation.scoring.state.ScoringUiState
import com.iti.careerpilot.ats.presentation.scoring.view.components.ScoringBody
import com.iti.careerpilot.ats.presentation.scoring.viewmodel.ScoringViewModel
import com.iti.careerpilot.ats.presentation.util.UiStateProvider
import com.iti.careerpilot.ats.presentation.util.rememberUiStateProvider
import com.iti.common.snackbar.CareerPilotSnackbarController

@Composable
fun ScoringRoot(
    workspaceId: Long,
    onBack: () -> Unit,
    openCoinsPaywall: () -> Unit,
    openCoverLetter: (Long) -> Unit,
    openReadyToPractice: (trackId: Long, trackName: String, workspaceId: Long) -> Unit,
    openJob: (String) -> Unit,
    viewModel: ScoringViewModel = hiltViewModel(),
) {
    val state = viewModel.state.collectAsStateWithLifecycle()
    val stateProvider = rememberUiStateProvider(state)
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) {
        viewModel.onAction(ScoringAction.OptimizeCv)
    }

    LaunchedEffect(workspaceId) {
        viewModel.onAction(ScoringAction.Initial(workspaceId))
    }

    LaunchedEffect(lifecycleOwner, viewModel) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    ScoringEffect.OpenCoinsPaywall -> openCoinsPaywall()
                    is ScoringEffect.OpenCoverLetter -> openCoverLetter(effect.workspaceId)
                    is ScoringEffect.StartOptimizationTracking -> {
                        CvOptimizationService.start(context, effect.job)
                    }
                    is ScoringEffect.ShowMessage -> {
                        CareerPilotSnackbarController.show(effect.message)
                    }
                    is ScoringEffect.OpenPractice -> openReadyToPractice(
                        effect.trackId,
                        effect.trackName,
                        effect.workspaceId,
                    )
                }
            }
        }
    }

    ScoringScreen(
        stateProvider = stateProvider,
        onAction = { action ->
            if (
                action == ScoringAction.OptimizeCv &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                viewModel.onAction(action)
            }
        },
        onBack = onBack,
        onOpenJob = openJob,
    )
}

@Composable
fun ScoringScreen(
    stateProvider: UiStateProvider<ScoringUiState>,
    onAction: (ScoringAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenJob: (String) -> Unit = {},
) {
    Column(modifier = modifier.fillMaxSize()) {
        AtsCenteredTopBar(
            title = stringResource(R.string.ats_job_match_title),
            onBack = onBack,
        )

        ScoringBody(
            stateProvider = stateProvider,
            onAction = onAction,
            onOpenJob = onOpenJob,
            modifier = Modifier.fillMaxSize(),
        )
    }
}