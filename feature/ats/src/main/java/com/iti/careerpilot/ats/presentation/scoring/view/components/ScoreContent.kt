package com.iti.careerpilot.ats.presentation.scoring.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.ats.R
import com.iti.careerpilot.ats.domain.model.AtsSectionScore
import com.iti.careerpilot.ats.presentation.scoring.state.ScoringAction
import com.iti.careerpilot.ats.presentation.scoring.state.ScoringUiState
import com.iti.careerpilot.core.designsystem.components.ButtonVariant
import com.iti.careerpilot.core.designsystem.components.CareerPilotButton

@Composable
internal fun ScoreContent(
    state: ScoringUiState,
    onAction: (ScoringAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val score = requireNotNull(state.score)

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { ScoreSummaryCard(score = score) }
        if (score.matchedSkills.isNotEmpty()) {
            item {
                SkillGroupCard(
                    title = stringResource(R.string.ats_matched_requirements),
                    skills = score.matchedSkills,
                )
            }
        }
        if (score.missingRequiredSkills.isNotEmpty()) {
            item {
                SkillGroupCard(
                    title = stringResource(R.string.ats_missing_required_skills),
                    skills = score.missingRequiredSkills,
                )
            }
        }
        if (score.missingPreferredSkills.isNotEmpty()) {
            item {
                SkillGroupCard(
                    title = stringResource(R.string.ats_missing_preferred_skills),
                    skills = score.missingPreferredSkills,
                )
            }
        }
        if (score.strengths.isNotEmpty()) {
            item {
                FeedbackListCard(
                    title = stringResource(R.string.ats_strengths),
                    values = score.strengths,
                )
            }
        }
        if (score.weaknesses.isNotEmpty()) {
            item {
                FeedbackListCard(
                    title = stringResource(R.string.ats_weaknesses),
                    values = score.weaknesses,
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
                FeedbackListCard(
                    title = stringResource(R.string.ats_recommendations),
                    values = score.recommendations,
                )
            }
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
