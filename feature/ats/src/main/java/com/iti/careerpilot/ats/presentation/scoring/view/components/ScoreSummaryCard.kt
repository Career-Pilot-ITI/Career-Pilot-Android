package com.iti.careerpilot.ats.presentation.scoring.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.ats.R
import com.iti.careerpilot.ats.domain.model.AtsScore
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard
import com.iti.careerpilot.core.designsystem.components.ScoreRing

@Composable
internal fun ScoreSummaryCard(
    score: AtsScore,
    modifier: Modifier = Modifier,
) {
    CareerPilotCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ScoreRing(
                progress = score.overallScore / 100f,
                progressColor = scoreColor(score.overallScore),
                trackColor = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.size(76.dp),
                centerContent = {
                    Text(
                        text = score.overallScore.toString(),
                        fontWeight = FontWeight.Bold,
                    )
                },
            )
            Column {
                Text(
                    text = stringResource(R.string.match_score).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                )
                Text(
                    text = stringResource(R.string.ats_match_score_value, score.matchPercentage),
                    fontWeight = FontWeight.Bold,
                )
                score.coinCost?.let { coinCost ->
                    Text(
                        text = pluralStringResource(R.plurals.ats_coins_used, coinCost, coinCost),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

@Composable
internal fun scoreColor(score: Int): Color = when (score) {
    in 0..59 -> MaterialTheme.colorScheme.error
    in 60..79 -> MaterialTheme.colorScheme.tertiary
    else -> MaterialTheme.colorScheme.primary
}
