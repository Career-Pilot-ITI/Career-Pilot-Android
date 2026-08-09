package com.iti.careerpilot.practicesession.presentation.resultscreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.core.designsystem.common.GradientIcon
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard
import com.iti.careerpilot.practicesession.presentation.resultscreen.BodyLanguageUiState
import com.iti.careerpilot.practicesession.presentation.resultscreen.ScoreItem
import com.iti.core.model.bodylanguage.BodyLanguageEvaluation
import com.iti.core.model.bodylanguage.BodyLanguageMetrics
import com.iti.core.model.bodylanguage.ConfidenceBand
import com.iti.core.model.bodylanguage.FallbackReason
import com.iti.core.model.bodylanguage.MetricEvaluation

@Composable
fun BodyLanguageSection(
    uiState: BodyLanguageUiState,
    metrics: BodyLanguageMetrics? = null,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        BodyLanguageUiState.Loading -> {
            LoadingBodyLanguageCard(modifier = modifier)
        }
        is BodyLanguageUiState.Success -> {
            EvaluationBodyLanguageCard(
                evaluation = uiState.evaluation,
                fallbackReason = null,
                modifier = modifier
            )
        }
        is BodyLanguageUiState.FallbackUsed -> {
            EvaluationBodyLanguageCard(
                evaluation = uiState.evaluation,
                fallbackReason = uiState.reason,
                modifier = modifier
            )
        }
        BodyLanguageUiState.Idle -> {
            if (metrics != null) {
                RawMetricsBodyLanguageCard(metrics = metrics, modifier = modifier)
            }
        }
    }
}

@Composable
fun BodyLanguageSection(
    metrics: BodyLanguageMetrics,
    modifier: Modifier = Modifier,
) {
    BodyLanguageSection(
        uiState = BodyLanguageUiState.Idle,
        metrics = metrics,
        modifier = modifier
    )
}

@Composable
private fun LoadingBodyLanguageCard(modifier: Modifier = Modifier) {
    CareerPilotCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .padding(Dimens.SpaceXL)
                .semantics(mergeDescendants = true) {
                    contentDescription = "Analyzing video body language with AI..."
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceM)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.5.dp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(Dimens.SpaceM))
                Text(
                    text = "Analyzing video body language with AI...",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
            )
        }
    }
}

@Composable
private fun EvaluationBodyLanguageCard(
    evaluation: BodyLanguageEvaluation,
    fallbackReason: FallbackReason?,
    modifier: Modifier = Modifier,
) {
    CareerPilotCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .padding(Dimens.SpaceXL)
                .semantics(mergeDescendants = true) {
                    contentDescription = "Body Language AI Evaluation overall score ${evaluation.overallScore} out of 100"
                },
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceL)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(Dimens.SpaceXL)
                    )
                    Spacer(modifier = Modifier.width(Dimens.SpaceS))
                    Text(
                        text = "Body Language Evaluation",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceS)
                ) {
                    ConfidenceBandBadge(confidenceBand = evaluation.confidenceBand)
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = CircleShape
                    ) {
                        Text(
                            text = "${evaluation.overallScore}/100",
                            modifier = Modifier.padding(horizontal = Dimens.SpaceM, vertical = Dimens.SpaceXS),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Fallback Banner if fallbackReason != null
            if (fallbackReason != null) {
                FallbackDisclaimerBanner()
            }

            // AI Summary
            if (evaluation.summary.isNotBlank()) {
                Text(
                    text = evaluation.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

            // 4 Metric Breakdown Cards
            Text(
                text = "Metric Breakdown",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )

            MetricBreakdownCard(
                label = "Eye Contact",
                icon = Icons.Default.RemoveRedEye,
                metric = evaluation.eyeContact
            )
            MetricBreakdownCard(
                label = "Posture",
                icon = Icons.Default.Accessibility,
                metric = evaluation.posture
            )
            MetricBreakdownCard(
                label = "Facial Expression",
                icon = Icons.Default.Face,
                metric = evaluation.facialExpression
            )
            MetricBreakdownCard(
                label = "Hand Gestures",
                icon = Icons.Default.TouchApp,
                metric = evaluation.handGestures
            )

            // Actionable Coaching Tips
            if (evaluation.actionableTips.isNotEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(Dimens.SpaceL)
                    )
                    Spacer(modifier = Modifier.width(Dimens.SpaceS))
                    Text(
                        text = "Actionable Coaching Tips",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }

                evaluation.actionableTips.forEach { tip ->
                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.padding(start = Dimens.SpaceS)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier
                                .size(Dimens.SpaceL)
                                .padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(Dimens.SpaceS))
                        Text(
                            text = tip,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfidenceBandBadge(confidenceBand: ConfidenceBand) {
    val (backgroundColor, textColor, label) = when (confidenceBand) {
        ConfidenceBand.HIGH -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            "HIGH"
        )
        ConfidenceBand.MODERATE -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
            "MODERATE"
        )
        ConfidenceBand.LOW -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            "LOW"
        )
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(Dimens.SpaceS),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = Dimens.SpaceS, vertical = Dimens.SpaceXS),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = textColor,
        )
    }
}

@Composable
private fun FallbackDisclaimerBanner() {
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
        shape = RoundedCornerShape(Dimens.SpaceM),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(Dimens.SpaceM),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.size(Dimens.SpaceL)
            )
            Spacer(modifier = Modifier.width(Dimens.SpaceS))
            Text(
                text = "AI evaluation unavailable — showing guidance based on on-device metrics",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
        }
    }
}

@Composable
private fun MetricBreakdownCard(
    label: String,
    icon: ImageVector,
    metric: MetricEvaluation,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = RoundedCornerShape(Dimens.SpaceM),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(Dimens.SpaceM)
                .semantics(mergeDescendants = true) {
                    contentDescription = "$label metric: score ${metric.score} percent. Observation: ${metric.observation}. Tip: ${metric.tip}"
                },
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceS)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(Dimens.SpaceXXL)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    GradientIcon(icon = icon)
                }
                Spacer(modifier = Modifier.width(Dimens.SpaceM))
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${metric.score}%",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            LinearProgressIndicator(
                progress = { metric.score / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                strokeCap = StrokeCap.Round
            )

            if (metric.observation.isNotBlank()) {
                Text(
                    text = metric.observation,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (metric.tip.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(Dimens.SpaceXS))
                    Text(
                        text = metric.tip,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Composable
private fun RawMetricsBodyLanguageCard(
    metrics: BodyLanguageMetrics,
    modifier: Modifier = Modifier,
) {
    CareerPilotCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .padding(Dimens.SpaceXL)
                .semantics(mergeDescendants = true) {
                    contentDescription = "Body language telemetry metrics"
                },
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceL)
        ) {
            Text(
                text = "Body Language Metrics",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )

            ScoreItem(
                label = "Eye Contact",
                score = metrics.eyeContactPercentage.toInt().coerceIn(0, 100),
                icon = Icons.Default.RemoveRedEye,
            )
            ScoreItem(
                label = "Smile",
                score = (metrics.averageSmile * 100).toInt().coerceIn(0, 100),
                icon = Icons.Default.Face,
            )
            ScoreItem(
                label = "Good Posture",
                score = (100 - metrics.slouchPercentage).toInt().coerceIn(0, 100),
                icon = Icons.Default.Accessibility,
            )
            ScoreItem(
                label = "Hand Composure",
                score = ((1f - metrics.fidgetScore) * 100).toInt().coerceIn(0, 100),
                icon = Icons.Default.TouchApp,
            )
        }
    }
}

@Preview
@Composable
private fun BodyLanguageSectionLoadingPreview() {
    BodyLanguageSection(uiState = BodyLanguageUiState.Loading)
}

@Preview
@Composable
private fun BodyLanguageSectionSuccessPreview() {
    BodyLanguageSection(
        uiState = BodyLanguageUiState.Success(
            evaluation = BodyLanguageEvaluation(
                overallScore = 85,
                eyeContact = MetricEvaluation(80, "Maintained consistent eye contact.", "Look at camera when speaking."),
                posture = MetricEvaluation(90, "Excellent upright posture.", "Keep shoulders relaxed."),
                facialExpression = MetricEvaluation(85, "Warm and engaging smile.", "Nod occasionally to show engagement."),
                handGestures = MetricEvaluation(80, "Natural hand gestures.", "Avoid tapping or fidgeting."),
                confidenceBand = ConfidenceBand.HIGH,
                summary = "Candidate demonstrates confident body language with strong posture and eye contact.",
                actionableTips = listOf(
                    "Maintain eye contact especially during key summary statements.",
                    "Keep hands visible in upper chest frame for better engagement."
                )
            )
        )
    )
}

@Preview
@Composable
private fun BodyLanguageSectionFallbackPreview() {
    BodyLanguageSection(
        uiState = BodyLanguageUiState.FallbackUsed(
            evaluation = BodyLanguageEvaluation(
                overallScore = 72,
                eyeContact = MetricEvaluation(70, "Eye contact percentage 70%.", "Focus on screen center."),
                posture = MetricEvaluation(75, "Slouch percentage 25%.", "Sit up straight."),
                facialExpression = MetricEvaluation(70, "Average smile 0.4.", "Smile more at beginning."),
                handGestures = MetricEvaluation(75, "Fidget score 0.25.", "Keep hands steady."),
                confidenceBand = ConfidenceBand.MODERATE,
                summary = "Guidance generated from on-device posture and face tracking data.",
                actionableTips = listOf(
                    "Practice speaking to camera directly.",
                    "Ensure good lighting on your face."
                )
            ),
            reason = FallbackReason.OFFLINE
        )
    )
}
