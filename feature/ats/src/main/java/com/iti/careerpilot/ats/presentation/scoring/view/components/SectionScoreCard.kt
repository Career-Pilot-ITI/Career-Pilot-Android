package com.iti.careerpilot.ats.presentation.scoring.view.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.iti.careerpilot.ats.domain.model.AtsSectionScore
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard

@Composable
internal fun SectionScoreCard(
    section: AtsSectionScore,
    lowestScore: Int,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable(section.section) { mutableStateOf(false) }
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
            .clickable { expanded = !expanded }
            .animateContentSize(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = section.score.toString(),
                    color = scoreColor(section.score),
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = section.section,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                )
                if (section.score == lowestScore) {
                    Text(
                        text = stringResource(R.string.ats_lowest),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
                Icon(
                    imageVector = if (expanded) {
                        Icons.Filled.KeyboardArrowUp
                    } else {
                        Icons.Filled.KeyboardArrowDown
                    },
                    contentDescription = stringResource(
                        if (expanded) R.string.ats_collapse_section else R.string.ats_expand_section,
                    ),
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
            AnimatedVisibility(visible = expanded && section.feedback.isNotBlank()) {
                Text(
                    text = section.feedback,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 10.dp),
                )
            }
        }
    }
}
