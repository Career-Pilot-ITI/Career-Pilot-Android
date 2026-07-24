package com.iti.careerpilot.reports.presentation.screen.breakdown.view.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import com.iti.careerpilot.core.designsystem.CareerPilotPalette
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.reports.R
import com.iti.careerpilot.reports.presentation.screen.breakdown.uimodels.QuestionUiModel
import com.iti.careerpilot.reports.presentation.screen.components.formatQuestionDuration

@Composable
fun QuestionMetricsRow(
    question: QuestionUiModel,
    modifier: Modifier = Modifier,
) {
    val locale = LocalConfiguration.current.locales[0]
    val duration = remember(question.durationSeconds, locale) {
        formatQuestionDuration(question.durationSeconds, locale)
    }
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
    ) {
        QuestionMetricCard(
            value = question.score.toString(),
            label = stringResource(R.string.reports_score),
            valueColor = CareerPilotPalette.green,
            modifier = Modifier.weight(1f),
        )
        QuestionMetricCard(
            value = question.fillerWordCount.toString(),
            label = stringResource(R.string.reports_metric_filler_words),
            valueColor = CareerPilotPalette.yellow,
            modifier = Modifier.weight(1f),
        )
        QuestionMetricCard(
            value = duration,
            label = stringResource(R.string.reports_duration),
            valueColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
    }
}