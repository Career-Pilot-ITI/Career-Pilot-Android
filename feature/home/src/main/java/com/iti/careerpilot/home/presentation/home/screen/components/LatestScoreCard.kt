package com.iti.careerpilot.home.presentation.home.screen.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.core.designsystem.CareerPilotPalette
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard
import com.iti.careerpilot.core.designsystem.components.ScoreRing
import com.iti.careerpilot.home.R
import com.iti.careerpilot.home.domain.model.ScoreSummary

@Composable
fun LatestScoreCard(
    summary: ScoreSummary?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CareerPilotCard(modifier = modifier.fillMaxWidth()) {
        if (summary == null) {
            EmptyScoreContent()
        } else {
            ScoreContent(summary = summary, onClick = onClick)
        }
    }
}

@Composable
private fun ScoreContent(
    summary: ScoreSummary,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(Dimens.CardPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceL),
    ) {
        ScoreRing(
            progress = summary.latestScore / MAX_SCORE,
            progressColor = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(SCORE_RING_SIZE),
        ) {
            Text(
                text = summary.latestScore.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.home_latest_score),
                style = MaterialTheme.typography.labelMedium,
                color = CareerPilotPalette.gray600,
            )
            Text(
                text = stringResource(scoreLabelRes(summary.latestScore)),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            ScoreDelta(delta = summary.deltaFromPrevious)
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = stringResource(R.string.home_open_reports),
            tint = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun EmptyScoreContent() {
    Row(
        modifier = Modifier.padding(Dimens.CardPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceL),
    ) {
        ScoreRing(
            progress = 0f,
            progressColor = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(SCORE_RING_SIZE),
        ) {
            Text(
                text = stringResource(R.string.home_score_empty_value),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = CareerPilotPalette.gray400,
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.home_latest_score),
                style = MaterialTheme.typography.labelMedium,
                color = CareerPilotPalette.gray600,
            )
            Text(
                text = stringResource(R.string.home_score_none),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.home_score_none_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = CareerPilotPalette.gray400,
            )
        }
    }
}

@Composable
private fun ScoreDelta(delta: Int?) {
    if (delta == null) {
        Text(
            text = stringResource(R.string.home_score_no_previous),
            style = MaterialTheme.typography.bodyMedium,
            color = CareerPilotPalette.gray400,
        )
        return
    }

    val isImprovement = delta >= 0
    val tint = if (isImprovement) CareerPilotPalette.green else MaterialTheme.colorScheme.error

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (isImprovement) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(Dimens.SpaceXL),
        )
        Text(
            text = stringResource(
                if (isImprovement) R.string.home_score_delta_up else R.string.home_score_delta_down,
                delta,
            ),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = tint,
        )
    }
}

private fun scoreLabelRes(score: Int): Int = when {
    score >= EXCELLENT_THRESHOLD -> R.string.home_score_excellent
    score >= GOOD_THRESHOLD -> R.string.home_score_good
    score >= KEEP_GOING_THRESHOLD -> R.string.home_score_keep_going
    else -> R.string.home_score_needs_work
}

private const val MAX_SCORE = 100f
private const val EXCELLENT_THRESHOLD = 85
private const val GOOD_THRESHOLD = 70
private const val KEEP_GOING_THRESHOLD = 50
private val SCORE_RING_SIZE = 88.dp
