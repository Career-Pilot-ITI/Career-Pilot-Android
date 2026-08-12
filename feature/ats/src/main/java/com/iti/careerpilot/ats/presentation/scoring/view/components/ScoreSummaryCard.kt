package com.iti.careerpilot.ats.presentation.scoring.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.ats.R
import com.iti.careerpilot.ats.domain.model.AtsScore
import com.iti.careerpilot.core.designsystem.CareerPilotTheme
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard
import com.iti.careerpilot.core.designsystem.components.ScoreRing

@Composable
internal fun ScoreSummaryCard(
    score: AtsScore,
    modifier: Modifier = Modifier,
) {
    val missingCount = score.missingRequiredSkills.size + score.missingPreferredSkills.size
    val colors = CareerPilotTheme.extendedColors

    CareerPilotCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                ScoreRing(
                    progress = score.overallScore / 100f,
                    progressColor = scoreColor(score.overallScore),
                    trackColor = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.size(72.dp),
                    centerContent = {
                        Text(
                            text = score.overallScore.toString(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    },
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = stringResource(R.string.match_score).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = stringResource(matchVerdict(score.overallScore)),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ScoreCountBadge(
                            text = stringResource(R.string.ats_matched_count, score.matchedSkills.size),
                            containerColor = colors.successContainer,
                            contentColor = colors.onSuccessContainer,
                        )
                        ScoreCountBadge(
                            text = stringResource(R.string.ats_missing_count, missingCount),
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        )
                    }
                }
            }
            LinearProgressIndicator(
                progress = { score.matchPercentage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = scoreColor(score.matchPercentage),
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
    }
}

@Composable
private fun ScoreCountBadge(
    text: String,
    containerColor: Color,
    contentColor: Color,
) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = containerColor,
        contentColor = contentColor,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

private fun matchVerdict(score: Int) = when (score) {
    in 0..59 -> R.string.ats_match_needs_work
    in 60..79 -> R.string.ats_match_good
    else -> R.string.ats_match_excellent
}

@Composable
internal fun scoreColor(score: Int): Color = when (score) {
    in 0..59 -> MaterialTheme.colorScheme.error
    in 60..79 -> CareerPilotTheme.extendedColors.warning
    else -> CareerPilotTheme.extendedColors.success
}
