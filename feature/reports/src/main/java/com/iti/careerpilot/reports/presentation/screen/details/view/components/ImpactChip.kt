package com.iti.careerpilot.reports.presentation.screen.details.view.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.reports.domain.model.CoachingImpact
import com.iti.careerpilot.reports.presentation.screen.components.coachingImpactLabel

@Composable
fun ImpactChip(impact: CoachingImpact) {
    Surface(
        shape = RoundedCornerShape(Dimens.SpaceL),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
        contentColor = MaterialTheme.colorScheme.primary,
    ) {
        Text(
            text = coachingImpactLabel(impact),
            modifier = Modifier.padding(horizontal = Dimens.SpaceS, vertical = Dimens.SpaceXS),
            style = MaterialTheme.typography.labelSmall,
        )
    }
}