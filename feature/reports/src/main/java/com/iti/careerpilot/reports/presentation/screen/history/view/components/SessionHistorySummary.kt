package com.iti.careerpilot.reports.presentation.screen.history.view.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.iti.careerpilot.reports.R

@Composable
fun SessionHistorySummary(
    sessionCount: Int,
    averageScore: Int,
    modifier: Modifier = Modifier,
) {
    val sessionText = pluralStringResource(R.plurals.reports_sessions, sessionCount, sessionCount)
    val averageText = stringResource(R.string.reports_average_score, averageScore)
    Text(
        text = stringResource(R.string.reports_summary_separator, sessionText, averageText),
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.66f),
    )
}
