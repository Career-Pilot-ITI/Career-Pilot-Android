package com.iti.careerpilot.ats.presentation.scoring.view

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.iti.careerpilot.ats.R
import com.iti.careerpilot.ats.background.CvOptimizationService
import com.iti.careerpilot.ats.domain.model.AtsSectionScore
import com.iti.careerpilot.ats.presentation.components.AtsCenteredTopBar
import com.iti.careerpilot.ats.presentation.components.AtsWorkspaceErrorContent
import com.iti.careerpilot.ats.presentation.components.AtsWorkspaceLoadingContent
import com.iti.careerpilot.ats.presentation.components.JobHeaderCard
import com.iti.careerpilot.ats.presentation.scoring.state.ScoringAction
import com.iti.careerpilot.ats.presentation.scoring.state.ScoringEffect
import com.iti.careerpilot.ats.presentation.scoring.state.ScoringUiState
import com.iti.careerpilot.ats.presentation.scoring.uimodel.FeedbackStatus
import com.iti.careerpilot.ats.presentation.scoring.uimodel.SkillStatus
import com.iti.careerpilot.ats.presentation.scoring.view.components.FeedbackListCard
import com.iti.careerpilot.ats.presentation.scoring.view.components.RecommendationsCard
import com.iti.careerpilot.ats.presentation.scoring.view.components.ScoreSummaryCard
import com.iti.careerpilot.ats.presentation.scoring.view.components.SectionScoreCard
import com.iti.careerpilot.ats.presentation.scoring.view.components.SkillGroupCard
import com.iti.careerpilot.ats.presentation.scoring.viewmodel.ScoringViewModel
import com.iti.careerpilot.ats.presentation.util.UiStateProvider
import com.iti.careerpilot.ats.presentation.util.rememberUiStateProvider
import com.iti.careerpilot.ats.presentation.util.rememberUiStateValue
import com.iti.careerpilot.core.designsystem.components.ButtonVariant
import com.iti.careerpilot.core.designsystem.components.CareerPilotButton
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

@Composable
private fun ScoringBody(
    stateProvider: UiStateProvider<ScoringUiState>,
    onAction: (ScoringAction) -> Unit,
    onOpenJob: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val phase by rememberUiStateValue(stateProvider) { state ->
        when {
            state.isLoading -> ScoringPhase.LOADING
            state.workspace == null || state.score == null -> ScoringPhase.ERROR
            else -> ScoringPhase.CONTENT
        }
    }

    when (phase) {
        ScoringPhase.LOADING -> AtsWorkspaceLoadingContent(modifier = modifier)
        ScoringPhase.ERROR -> ScoringErrorContent(
            stateProvider = stateProvider,
            onAction = onAction,
            modifier = modifier,
        )
        ScoringPhase.CONTENT -> ScoringContent(
            stateProvider = stateProvider,
            onAction = onAction,
            onOpenJob = onOpenJob,
            modifier = modifier,
        )
    }
}

@Composable
private fun ScoringErrorContent(
    stateProvider: UiStateProvider<ScoringUiState>,
    onAction: (ScoringAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val error by rememberUiStateValue(stateProvider) { it.error }
    val hasInsufficientCoins by rememberUiStateValue(stateProvider) { it.hasInsufficientCoins }
    Column(modifier = modifier) {
        AtsWorkspaceErrorContent(
            error = error,
            onRetry = { onAction(ScoringAction.Retry) },
            modifier = Modifier.weight(1f),
        )
        if (hasInsufficientCoins) {
            CareerPilotButton(
                text = stringResource(R.string.ats_get_coins),
                onClick = { onAction(ScoringAction.OpenCoins) },
                variant = ButtonVariant.OUTLINE,
                modifier = Modifier.padding(20.dp),
            )
        }
    }
}

@Composable
private fun ScoringContent(
    stateProvider: UiStateProvider<ScoringUiState>,
    onAction: (ScoringAction) -> Unit,
    onOpenJob: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scoreValue by rememberUiStateValue(stateProvider) { it.score }
    val workspaceValue by rememberUiStateValue(stateProvider) { it.workspace }
    val score = requireNotNull(scoreValue)
    val workspace = requireNotNull(workspaceValue)
    val requiredKeywordCount = remember(score) {
        score.matchedSkills.size + score.missingRequiredSkills.size
    }
    val lowestSectionScore = remember(score.sections) {
        score.sections.minOfOrNull(AtsSectionScore::score)
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
            item {
                JobHeaderCard(
                    job = workspace.job,
                    onOpenJob = onOpenJob,
                    compact = true,
                )
            }
            item { ScoreSummaryCard(score = score) }
            if (score.matchedSkills.isNotEmpty()) {
                item {
                    SkillGroupCard(
                        title = stringResource(R.string.ats_matched_requirements),
                        countLabel = stringResource(
                            R.string.ats_keywords_ratio,
                            score.matchedSkills.size,
                            requiredKeywordCount,
                        ),
                        skills = score.matchedSkills,
                        status = SkillStatus.MATCHED,
                    )
                }
            }
            if (score.missingRequiredSkills.isNotEmpty()) {
                item {
                    SkillGroupCard(
                        title = stringResource(R.string.ats_missing_required_skills),
                        countLabel = stringResource(
                            R.string.ats_keywords_ratio,
                            score.missingRequiredSkills.size,
                            requiredKeywordCount,
                        ),
                        skills = score.missingRequiredSkills,
                        status = SkillStatus.REQUIRED_MISSING,
                    )
                }
            }
            if (score.missingPreferredSkills.isNotEmpty()) {
                item {
                    SkillGroupCard(
                        title = stringResource(R.string.ats_missing_preferred_skills),
                        countLabel = stringResource(
                            R.string.ats_keywords_count,
                            score.missingPreferredSkills.size,
                        ),
                        skills = score.missingPreferredSkills,
                        status = SkillStatus.PREFERRED_MISSING,
                    )
                }
            }
            if (score.strengths.isNotEmpty()) {
                item {
                    FeedbackListCard(
                        title = stringResource(R.string.ats_strengths),
                        values = score.strengths,
                        status = FeedbackStatus.STRENGTH,
                    )
                }
            }
            if (score.weaknesses.isNotEmpty()) {
                item {
                    FeedbackListCard(
                        title = stringResource(R.string.ats_weaknesses),
                        values = score.weaknesses,
                        status = FeedbackStatus.WEAKNESS,
                    )
                }
            }
            if (score.sections.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.ats_section_breakdown),
                        fontWeight = FontWeight.Bold,
                    )
                }
                items(score.sections, key = AtsSectionScore::section) { section ->
                    SectionScoreCard(
                        section = section,
                        lowestScore = requireNotNull(lowestSectionScore),
                    )
                }
            }
            if (score.recommendations.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.ats_recommendations),
                        fontWeight = FontWeight.Bold,
                    )
                }
                item { RecommendationsCard(values = score.recommendations) }
            }
            item {
                OptimizeCvActionItem(
                    stateProvider = stateProvider,
                    onAction = onAction,
                )
            }
            item {
                CareerPilotButton(
                    text = stringResource(R.string.ats_generate_cover_letter),
                    onClick = { onAction(ScoringAction.GenerateCoverLetter) },
                    variant = ButtonVariant.OUTLINE,
                    leadingContent = {
                        Icon(
                            painter = painterResource(R.drawable.ic_email_outline),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                    },
                )
            }
            item {
                PracticeActionItem(
                    stateProvider = stateProvider,
                    onAction = onAction,
                )
            }
    }
}

@Composable
private fun OptimizeCvActionItem(
    stateProvider: UiStateProvider<ScoringUiState>,
    onAction: (ScoringAction) -> Unit,
) {
    val hasInsufficientCoins by rememberUiStateValue(stateProvider) { it.hasInsufficientCoins }
    val isStartingOptimization by rememberUiStateValue(stateProvider) {
        it.isStartingOptimization
    }
    val optimizationError by rememberUiStateValue(stateProvider) { it.optimizationError }

    if (hasInsufficientCoins) {
        CareerPilotButton(
            text = stringResource(R.string.ats_get_coins),
            onClick = { onAction(ScoringAction.OpenCoins) },
            variant = ButtonVariant.OUTLINE,
        )
    } else {
        CareerPilotButton(
            text = stringResource(
                if (isStartingOptimization) {
                    R.string.ats_cv_optimization_starting
                } else {
                    R.string.ats_optimize_cv
                },
            ),
            onClick = { onAction(ScoringAction.OptimizeCv) },
            enabled = !isStartingOptimization,
            leadingContent = {
                Icon(
                    painter = painterResource(R.drawable.ic_lightbulb_outline),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            },
        )
    }
    optimizationError?.let { error ->
        Text(
            text = error.asString(),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

@Composable
private fun PracticeActionItem(
    stateProvider: UiStateProvider<ScoringUiState>,
    onAction: (ScoringAction) -> Unit,
) {
    val trackId by rememberUiStateValue(stateProvider) { it.trackId }
    CareerPilotButton(
        text = stringResource(R.string.ats_start_practice_for_job),
        onClick = { onAction(ScoringAction.StartPractice) },
        enabled = trackId != null,
        variant = ButtonVariant.OUTLINE,
        leadingContent = {
            Icon(
                painter = painterResource(R.drawable.ic_record_outline),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
        },
    )
    if (trackId == null) {
        Text(
            text = stringResource(R.string.ats_practice_track_required),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

private enum class ScoringPhase { LOADING, ERROR, CONTENT }
