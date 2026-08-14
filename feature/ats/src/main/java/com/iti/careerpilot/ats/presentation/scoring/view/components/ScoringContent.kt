package com.iti.careerpilot.ats.presentation.scoring.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.ats.R
import com.iti.careerpilot.ats.domain.model.AtsSectionScore
import com.iti.careerpilot.ats.presentation.components.AtsWorkspaceErrorContent
import com.iti.careerpilot.ats.presentation.components.JobHeaderCard
import com.iti.careerpilot.ats.presentation.scoring.state.ScoringAction
import com.iti.careerpilot.ats.presentation.scoring.state.ScoringUiState
import com.iti.careerpilot.ats.presentation.scoring.uimodel.FeedbackStatus
import com.iti.careerpilot.ats.presentation.scoring.uimodel.SkillStatus
import com.iti.careerpilot.ats.presentation.util.UiStateProvider
import com.iti.careerpilot.ats.presentation.util.rememberUiStateValue
import com.iti.careerpilot.core.designsystem.components.ButtonVariant
import com.iti.careerpilot.core.designsystem.components.CareerPilotButton

@Composable
fun ScoringContent(
    stateProvider: UiStateProvider<ScoringUiState>,
    onAction: (ScoringAction) -> Unit,
    onOpenJob: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scoreValue by rememberUiStateValue(stateProvider) { it.score }
    val workspaceValue by rememberUiStateValue(stateProvider) { it.workspace }
    val score = scoreValue
    val workspace = workspaceValue
    if (score == null || workspace == null) {
        val error by rememberUiStateValue(stateProvider) { it.error }
        AtsWorkspaceErrorContent(
            error = error,
            onRetry = { onAction(ScoringAction.Retry) },
            modifier = modifier,
        )
        return
    }
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
                    lowestScore = lowestSectionScore ?: section.score,
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
                        imageVector = Icons.Outlined.Email,
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
