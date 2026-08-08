package com.iti.careerpilot.practicesession.presentation.resultscreen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.iti.careerpilot.bodylanguage.model.BodyLanguageMetrics
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard
import com.iti.careerpilot.practicesession.presentation.resultscreen.ScoreItem

@Composable
fun BodyLanguageSection(
    metrics: BodyLanguageMetrics,
    modifier: Modifier = Modifier,
) {
    CareerPilotCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(Dimens.SpaceXL),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceL),
        ) {
            Text(
                text = "Body Language",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
            )

            // Face metrics
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

            // Posture metrics
            ScoreItem(
                label = "Good Posture",
                score = (100 - metrics.slouchPercentage).toInt().coerceIn(0, 100),
                icon = Icons.Default.Accessibility,
            )

            // Hands metrics
            ScoreItem(
                label = "Hand Composure",
                score = ((1f - metrics.fidgetScore) * 100).toInt().coerceIn(0, 100),
                icon = Icons.Default.TouchApp,
            )
        }
    }
}
