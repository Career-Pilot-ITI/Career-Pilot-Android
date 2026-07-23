package com.iti.careerpilot.reports.presentation.screen.details.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.iti.careerpilot.core.designsystem.CareerPilotPalette
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.core.designsystem.components.ScoreRing
import com.iti.careerpilot.reports.R
import com.iti.careerpilot.reports.presentation.screen.components.performanceTierLabel
import com.iti.careerpilot.reports.presentation.screen.details.uimodels.ReportDetailsUiModel

@Composable
fun PerformanceSummaryCard(
    report: ReportDetailsUiModel,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = CareerPilotPalette.navyMid,
        contentColor = CareerPilotPalette.white,
    ) {
        Row(
            modifier = Modifier.padding(Dimens.SpaceXL),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceXL),
        ) {
            ScoreRing(
                progress = report.overallScore / 100f,
                progressColor = CareerPilotPalette.amber,
                trackColor = CareerPilotPalette.white.copy(alpha = 0.16f),
                modifier = Modifier.size(Dimens.ScoreRingSize),
                centerContent = {
                    Text(
                        text = report.overallScore.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.reports_overall_score),
                    modifier = Modifier.padding(top = Dimens.SpaceS),
                    style = MaterialTheme.typography.labelSmall,
                    color = CareerPilotPalette.white.copy(alpha = 0.68f),
                )
                Text(
                    text = performanceTierLabel(report.performanceTier),
                    style = MaterialTheme.typography.titleMedium,
                )
                report.topPercent?.let { topPercent ->
                    Text(
                        text = stringResource(R.string.reports_top_percent, topPercent),
                        modifier = Modifier.padding(top = Dimens.SpaceXS),
                        style = MaterialTheme.typography.bodyMedium,
                        color = CareerPilotPalette.teal,
                    )
                }
            }
        }
    }
}