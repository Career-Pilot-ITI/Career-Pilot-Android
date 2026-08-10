package com.iti.careerpilot.ats.presentation.scoring

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iti.careerpilot.ats.R
import com.iti.careerpilot.ats.domain.model.AtsScore
import com.iti.careerpilot.ats.domain.model.AtsSectionScore
import com.iti.careerpilot.core.designsystem.common.ObserveEvent
import com.iti.careerpilot.core.designsystem.components.ButtonVariant
import com.iti.careerpilot.core.designsystem.components.CareerPilotButton
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard
import com.iti.careerpilot.core.designsystem.components.ScoreRing

@Composable
fun ScoringRoot(
    workspaceId: Long,
    onBack: () -> Unit,
    openCoinsPaywall: () -> Unit,
    openCoverLetter: (Long) -> Unit,
    openOptimizedCv: (Long) -> Unit,
    openPracticeSession: (Long) -> Unit,
    viewModel: ScoringViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(workspaceId) { viewModel.loadWorkspace(workspaceId) }
    ObserveEvent(viewModel.effects) { effect ->
        when (effect) {
            ScoringEffect.OpenCoinsPaywall -> openCoinsPaywall()
            is ScoringEffect.OpenCoverLetter -> openCoverLetter(effect.workspaceId)
            is ScoringEffect.OpenOptimizedCv -> openOptimizedCv(effect.workspaceId)
            is ScoringEffect.OpenPractice -> openPracticeSession(effect.trackId)
        }
    }
    ScoringScreen(state, viewModel::onAction, onBack)
}

@Composable
fun ScoringScreen(
    state: ScoringUiState,
    onAction: (ScoringAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onBack) { Text(stringResource(R.string.ats_back)) }
            Text(
                stringResource(R.string.ats_job_match_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
        }
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.workspace == null -> ErrorState(state, onAction)
            state.score == null -> BeforeScoreState(state, onAction)
            else -> ScoreContent(state, onAction)
        }
    }
    if (state.isScoreConfirmationVisible) {
        AlertDialog(
            onDismissRequest = { onAction(ScoringAction.DismissConfirmation) },
            title = { Text(stringResource(R.string.ats_confirm_scoring_title)) },
            text = { Text(stringResource(R.string.ats_paid_operation_confirmation)) },
            confirmButton = {
                TextButton(onClick = { onAction(ScoringAction.ConfirmScore) }) {
                    Text(stringResource(R.string.ats_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { onAction(ScoringAction.DismissConfirmation) }) {
                    Text(stringResource(R.string.ats_cancel))
                }
            },
        )
    }
}

@Composable
private fun BeforeScoreState(state: ScoringUiState, onAction: (ScoringAction) -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        state.workspace?.let { workspace ->
            Text(workspace.job.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            if (workspace.job.companyName.isNotBlank()) Text(
                workspace.job.companyName,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (state.wasInterrupted) Text(
            stringResource(R.string.ats_scoring_interrupted),
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(top = 16.dp),
        )
        state.error?.let { Text(it.asString(), color = MaterialTheme.colorScheme.error) }
        if (state.hasInsufficientCoins) CareerPilotButton(
            text = stringResource(R.string.ats_get_coins),
            onClick = { onAction(ScoringAction.OpenCoins) },
            variant = ButtonVariant.OUTLINE,
            modifier = Modifier.padding(top = 12.dp),
        )
        CareerPilotButton(
            text = stringResource(R.string.ats_score_cv),
            onClick = { onAction(ScoringAction.RequestScore) },
            modifier = Modifier.padding(top = 20.dp),
        )
    }
}

@Composable
private fun ErrorState(state: ScoringUiState, onAction: (ScoringAction) -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(state.error?.asString() ?: stringResource(R.string.ats_unknown_error))
        CareerPilotButton(
            text = stringResource(R.string.ats_retry),
            onClick = { onAction(ScoringAction.RetryWorkspace) },
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}

@Composable
private fun ScoreContent(state: ScoringUiState, onAction: (ScoringAction) -> Unit) {
    val score = requireNotNull(state.score)
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { ScoreSummary(score) }
        if (score.matchedSkills.isNotEmpty()) item {
            SkillGroup(stringResource(R.string.ats_matched_requirements), score.matchedSkills)
        }
        if (score.missingRequiredSkills.isNotEmpty()) item {
            SkillGroup(stringResource(R.string.ats_missing_required_skills), score.missingRequiredSkills)
        }
        if (score.missingPreferredSkills.isNotEmpty()) item {
            SkillGroup(stringResource(R.string.ats_missing_preferred_skills), score.missingPreferredSkills)
        }
        if (score.strengths.isNotEmpty()) item {
            FeedbackList(stringResource(R.string.ats_strengths), score.strengths)
        }
        if (score.weaknesses.isNotEmpty()) item {
            FeedbackList(stringResource(R.string.ats_weaknesses), score.weaknesses)
        }
        if (score.sections.isNotEmpty()) {
            item { Text(stringResource(R.string.ats_section_breakdown), fontWeight = FontWeight.Bold) }
            items(score.sections, key = AtsSectionScore::section) { section ->
                SectionScoreRow(section, score.sections.minOf(AtsSectionScore::score))
            }
        }
        if (score.recommendations.isNotEmpty()) item {
            FeedbackList(stringResource(R.string.ats_recommendations), score.recommendations)
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
                text = stringResource(R.string.ats_start_practice),
                onClick = { onAction(ScoringAction.StartPractice) },
                enabled = state.trackId != null,
                variant = ButtonVariant.OUTLINE,
            )
            if (state.trackId == null) Text(
                stringResource(R.string.ats_practice_track_required),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

@Composable
private fun ScoreSummary(score: AtsScore) {
    CareerPilotCard(Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            val color = scoreColor(score.overallScore)
            ScoreRing(
                progress = score.overallScore / 100f,
                progressColor = color,
                trackColor = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.size(76.dp),
                centerContent = { Text(score.overallScore.toString(), fontWeight = FontWeight.Bold) },
            )
            Column {
                Text(stringResource(R.string.match_score).uppercase(), style = MaterialTheme.typography.labelSmall)
                Text(stringResource(R.string.ats_match_score_value, score.matchPercentage), fontWeight = FontWeight.Bold)
                score.coinCost?.let {
                    Text(stringResource(R.string.ats_coins_used, it), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun SkillGroup(title: String, skills: List<String>) = CareerPilotCard(Modifier.fillMaxWidth()) {
    Column(Modifier.padding(16.dp)) {
        Text(stringResource(R.string.ats_group_count, title, skills.size), fontWeight = FontWeight.Bold)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            skills.forEach { skill -> AssistChip(onClick = {}, label = { Text(skill) }) }
        }
    }
}

@Composable
private fun FeedbackList(title: String, values: List<String>) = CareerPilotCard(Modifier.fillMaxWidth()) {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, fontWeight = FontWeight.Bold)
        values.forEach { Text("• $it", color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable
private fun SectionScoreRow(section: AtsSectionScore, lowestScore: Int) {
    var expanded by rememberSaveable(section.section) { mutableStateOf(false) }
    CareerPilotCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .animateContentSize(),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(section.score.toString(), color = scoreColor(section.score), fontWeight = FontWeight.Bold)
                Text(section.section, Modifier.weight(1f).padding(horizontal = 12.dp), fontWeight = FontWeight.SemiBold)
                if (section.score == lowestScore) Text(
                    stringResource(R.string.ats_lowest),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            AnimatedVisibility(visible = expanded && section.feedback.isNotBlank()) {
                Text(
                    section.feedback,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 10.dp),
                )
            }
        }
    }
}

@Composable
private fun scoreColor(score: Int): Color = when (score) {
    in 0..59 -> MaterialTheme.colorScheme.error
    in 60..79 -> MaterialTheme.colorScheme.tertiary
    else -> MaterialTheme.colorScheme.primary
}
