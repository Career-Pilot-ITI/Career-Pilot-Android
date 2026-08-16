package com.iti.careerpilot.home.presentation.home.screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.core.designsystem.CareerPilotShapes
import com.iti.careerpilot.core.designsystem.CareerPilotTheme
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.core.designsystem.softShadow
import com.iti.careerpilot.home.R

@Composable
fun SubscriptionCard(
    planLabel: String,
    subscriptionTier: String,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val brush = CareerPilotTheme.extendedColors.studioBrandBrush
    val normalizedTier = subscriptionTier.uppercase().trim()
    val isMax = normalizedTier in setOf("PRO", "MAX")
    val isPlus = normalizedTier == "PLUS"

    val (subtitleRes, ctaRes) = when {
        isMax -> Pair(R.string.home_plan_max_subtitle, R.string.home_plan_manage)
        isPlus -> Pair(R.string.home_plan_plus_subtitle, R.string.home_plan_upgrade_to_max)
        else -> Pair(R.string.home_plan_upgrade_subtitle, R.string.home_upgrade)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .softShadow(elevation = 8.dp)
            .clip(CareerPilotShapes.medium)
            .background(brush)
            .clickable(onClick = onCardClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.SpaceXL),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.home_plan_label, planLabel).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.8f),
                )
                Text(
                    text = stringResource(subtitleRes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }

            Spacer(modifier = Modifier.width(Dimens.SpaceM))

            Text(
                text = stringResource(ctaRes),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(horizontal = Dimens.SpaceL, vertical = Dimens.SpaceS),
            )
        }
    }
}
