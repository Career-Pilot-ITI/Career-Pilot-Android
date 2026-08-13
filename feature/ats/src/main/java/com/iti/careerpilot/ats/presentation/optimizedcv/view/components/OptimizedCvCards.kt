package com.iti.careerpilot.ats.presentation.optimizedcv.view.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.ats.R
import com.iti.careerpilot.ats.domain.model.CvOptimizationSection
import com.iti.careerpilot.ats.domain.model.CvSectionImprovement
import com.iti.careerpilot.ats.presentation.util.scoreColor
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard

@Composable
internal fun OptimizationSectionCard(
    section: CvOptimizationSection,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable(section.name) { mutableStateOf(false) }
    val expansionState = stringResource(
        if (expanded) R.string.ats_expanded else R.string.ats_collapsed,
    )

    CareerPilotCard(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                role = Role.Button
                stateDescription = expansionState
            }
            .clickable(
                indication = null,
                interactionSource = null,
                onClick = { expanded = !expanded },
            )
            .animateContentSize(),
        useShadow = false,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = scoreColor(section.score).copy(alpha = 0.12f),
                    contentColor = scoreColor(section.score),
                ) {
                    Text(
                        text = section.score.toString(),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    )
                }
                Text(
                    text = section.name,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                )
                Icon(
                    imageVector = if (expanded) {
                        Icons.Filled.KeyboardArrowUp
                    } else {
                        Icons.Filled.KeyboardArrowDown
                    },
                    contentDescription = stringResource(
                        if (expanded) R.string.ats_collapse_section else R.string.ats_expand_section,
                    ),
                )
            }
            LinearProgressIndicator(
                progress = { section.score / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .padding(start = 54.dp, end = 36.dp),
                color = scoreColor(section.score),
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            AnimatedVisibility(visible = expanded) {
                if (section.improvements.isEmpty()) {
                    Text(
                        text = stringResource(R.string.ats_section_good_enough),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 16.dp),
                    )
                } else {
                    Column(
                        modifier = Modifier.padding(top = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        section.improvements.forEachIndexed { index, improvement ->
                            if (index > 0) HorizontalDivider()
                            ImprovementContent(improvement = improvement)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ImprovementContent(
    improvement: CvSectionImprovement,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        ImprovementValue(
            label = stringResource(R.string.ats_original_text),
            value = improvement.original,
        )
        ImprovementValue(
            label = stringResource(R.string.ats_improved_text),
            value = improvement.improved,
            highlight = true,
        )
        ImprovementValue(
            label = stringResource(R.string.ats_improvement_reason),
            value = improvement.reason,
        )
    }
}

@Composable
private fun ImprovementValue(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    highlight: Boolean = false,
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (highlight) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

@Composable
internal fun RecommendedTracksCard(
    tracks: List<String>,
    modifier: Modifier = Modifier,
) {
    CareerPilotCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.ats_recommended_tracks),
                fontWeight = FontWeight.Bold,
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                tracks.forEach { track ->
                    AssistChip(
                        onClick = {},
                        label = { Text(track) },
                    )
                }
            }
        }
    }
}
