package com.iti.careerpilot.reports.presentation.screen.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.iti.careerpilot.reports.R
import com.iti.careerpilot.reports.domain.model.CoachingImpact
import com.iti.careerpilot.reports.domain.model.PerformanceTier
import com.iti.careerpilot.reports.presentation.screen.details.uimodels.PerformanceMetricType
import java.util.Locale

fun formatQuestionDuration(
    durationSeconds: Int,
    locale: Locale,
): String = String.format(
    locale,
    "%d:%02d",
    durationSeconds / 60,
    durationSeconds % 60,
)

@Composable
fun performanceTierLabel(tier: PerformanceTier): String = stringResource(
    when (tier) {
        PerformanceTier.STRONG -> R.string.reports_performance_strong
        PerformanceTier.GOOD -> R.string.reports_performance_good
        PerformanceTier.NEEDS_IMPROVEMENT -> R.string.reports_performance_needs_improvement
    },
)

@Composable
fun performanceMetricLabel(type: PerformanceMetricType): String = stringResource(
    when (type) {
        PerformanceMetricType.CLARITY -> R.string.reports_metric_clarity
        PerformanceMetricType.CONFIDENCE -> R.string.reports_metric_confidence
        PerformanceMetricType.PACING -> R.string.reports_metric_pacing
        PerformanceMetricType.FILLER_WORDS -> R.string.reports_metric_filler_words
        PerformanceMetricType.CONTENT -> R.string.reports_metric_content
    },
)

@Composable
fun coachingImpactLabel(impact: CoachingImpact): String = stringResource(
    when (impact) {
        CoachingImpact.HIGH -> R.string.reports_impact_high
        CoachingImpact.MEDIUM -> R.string.reports_impact_medium
        CoachingImpact.LOW -> R.string.reports_impact_low
    },
)
