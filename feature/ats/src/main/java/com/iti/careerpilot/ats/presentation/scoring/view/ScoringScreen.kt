package com.iti.careerpilot.ats.presentation.scoring.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.iti.careerpilot.ats.R
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
import com.iti.careerpilot.core.designsystem.components.ButtonVariant
import com.iti.careerpilot.core.designsystem.components.CareerPilotButton

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
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(workspaceId) {
        viewModel.onAction(ScoringAction.Initial(workspaceId))
    }

    LaunchedEffect(lifecycleOwner, viewModel) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    ScoringEffect.OpenCoinsPaywall -> openCoinsPaywall()
                    is ScoringEffect.OpenCoverLetter -> openCoverLetter(effect.workspaceId)
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
        state = state,
        onAction = viewModel::onAction,
        onBack = onBack,
        onOpenJob = openJob,
    )
}

@Composable
fun ScoringScreen(
    state: ScoringUiState,
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

        when {
            state.isLoading -> AtsWorkspaceLoadingContent(modifier = Modifier.fillMaxSize())
            state.workspace == null || state.score == null -> ScoringErrorContent(
                state = state,
                onAction = onAction,
                modifier = Modifier.fillMaxSize(),
            )
            else -> ScoringContent(
                state = state,
                onAction = onAction,
                onOpenJob = onOpenJob,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun ScoringErrorContent(
    state: ScoringUiState,
    onAction: (ScoringAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        AtsWorkspaceErrorContent(
            error = state.error,
            onRetry = { onAction(ScoringAction.Retry) },
            modifier = Modifier.weight(1f),
        )
        if (state.hasInsufficientCoins) {
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
    state: ScoringUiState,
    onAction: (ScoringAction) -> Unit,
    onOpenJob: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val score = requireNotNull(state.score)
    val workspace = requireNotNull(state.workspace)
    val requiredKeywordCount = score.matchedSkills.size + score.missingRequiredSkills.size

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
                        lowestScore = score.sections.minOf(AtsSectionScore::score),
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
                CareerPilotButton(
                    text = stringResource(R.string.ats_optimize_cv),
                    onClick = { onAction(ScoringAction.OptimizeCv) },
                )
            }
            item {
                CareerPilotButton(
                    text = stringResource(R.string.ats_generate_cover_letter),
                    onClick = { onAction(ScoringAction.GenerateCoverLetter) },
                    variant = ButtonVariant.OUTLINE,
                )
            }
            item {
                CareerPilotButton(
                    text = stringResource(R.string.ats_start_practice_for_job),
                    onClick = { onAction(ScoringAction.StartPractice) },
                    enabled = state.trackId != null,
                    variant = ButtonVariant.OUTLINE,
                )
                if (state.trackId == null) {
                    Text(
                        text = stringResource(R.string.ats_practice_track_required),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
        }
}
