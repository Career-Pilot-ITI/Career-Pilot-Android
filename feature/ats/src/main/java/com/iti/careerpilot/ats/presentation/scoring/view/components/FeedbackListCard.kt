package com.iti.careerpilot.ats.presentation.scoring.view.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.ats.R
import com.iti.careerpilot.ats.presentation.scoring.uimodel.FeedbackStatus
import com.iti.careerpilot.core.designsystem.CareerPilotTheme
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard

@Composable
fun FeedbackListCard(
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