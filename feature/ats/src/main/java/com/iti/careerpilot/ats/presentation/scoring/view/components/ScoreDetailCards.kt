package com.iti.careerpilot.ats.presentation.scoring.view.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.ats.R
import com.iti.careerpilot.core.designsystem.CareerPilotTheme
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard

internal enum class SkillStatus { MATCHED, REQUIRED_MISSING, PREFERRED_MISSING }

internal enum class FeedbackStatus { STRENGTH, WEAKNESS }

@Composable
internal fun SkillGroupCard(
    title: String,
    countLabel: String,
    skills: List<String>,
    status: SkillStatus,
    modifier: Modifier = Modifier,
) {
    val palette = skillPalette(status)
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = title, fontWeight = FontWeight.Bold)
            Text(
                text = countLabel,
                style = MaterialTheme.typography.labelSmall,
                color = palette.contentColor,
                fontWeight = FontWeight.SemiBold,
            )
        }
        CareerPilotCard(
            modifier = Modifier.fillMaxWidth(),
            useShadow = false,
        ) {
            FlowRow(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                skills.forEach { skill ->
                    Surface(
                        shape = MaterialTheme.shapes.extraLarge,
                        color = palette.containerColor,
                        contentColor = palette.contentColor,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = palette.icon,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                            )
                            Text(text = skill, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun FeedbackListCard(
    title: String,
    values: List<String>,
    status: FeedbackStatus,
    modifier: Modifier = Modifier,
) {
    val colors = CareerPilotTheme.extendedColors
    val isStrength = status == FeedbackStatus.STRENGTH
    val containerColor = if (isStrength) colors.successContainer else colors.warningContainer
    val contentColor = if (isStrength) colors.onSuccessContainer else colors.onWarningContainer
    val icon = if (isStrength) Icons.Outlined.ThumbUp else Icons.Outlined.ThumbDown

    CareerPilotCard(
        modifier = modifier.fillMaxWidth(),
        useShadow = false,
        containerColor = containerColor,
        borderStroke = BorderStroke(width = 1.dp, contentColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = contentColor)
                Text(text = title, color = contentColor, fontWeight = FontWeight.Bold)
            }
            values.forEach { value ->
                Text(
                    text = stringResource(R.string.ats_list_item, value),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
internal fun RecommendationsCard(
    values: List<String>,
    modifier: Modifier = Modifier,
) {
    CareerPilotCard(modifier = modifier.fillMaxWidth()) {
        Column {
            values.forEachIndexed { index, value ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = .05f),
                        contentColor = MaterialTheme.colorScheme.onBackground.copy(alpha = .8f),
                    ) {
                        Text(
                            text = (index + 1).toString(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                        )
                    }
                    Text(
                        text = value,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (index != values.lastIndex) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}

private data class SkillPalette(
    val icon: ImageVector,
    val containerColor: Color,
    val contentColor: Color,
)

@Composable
private fun skillPalette(status: SkillStatus): SkillPalette {
    val colors = CareerPilotTheme.extendedColors
    return when (status) {
        SkillStatus.MATCHED -> SkillPalette(
            icon = Icons.Outlined.Check,
            containerColor = colors.successContainer,
            contentColor = colors.onSuccessContainer,
        )
        SkillStatus.REQUIRED_MISSING -> SkillPalette(
            icon = Icons.Outlined.Close,
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        )
        SkillStatus.PREFERRED_MISSING -> SkillPalette(
            icon = Icons.Outlined.Schedule,
            containerColor = colors.warningContainer,
            contentColor = colors.onWarningContainer,
        )
    }
}
