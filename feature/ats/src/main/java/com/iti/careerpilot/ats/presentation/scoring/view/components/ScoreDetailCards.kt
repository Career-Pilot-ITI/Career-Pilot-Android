package com.iti.careerpilot.ats.presentation.scoring.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.ats.presentation.scoring.uimodel.SkillStatus
import com.iti.careerpilot.ats.presentation.util.skillPalette
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard



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