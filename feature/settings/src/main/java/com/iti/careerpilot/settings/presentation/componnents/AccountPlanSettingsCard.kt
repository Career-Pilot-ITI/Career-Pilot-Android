package com.iti.careerpilot.settings.presentation.componnents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MonetizationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.core.designsystem.CareerPilotTheme
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.core.designsystem.components.ButtonVariant
import com.iti.careerpilot.core.designsystem.components.CareerPilotButton
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard
import com.iti.careerpilot.settings.R

@Composable
fun AccountPlanSettingsCard(
    planDisplayName: String,
    isMaxPlan: Boolean,
    coinBalance: Int,
    onManageSubscriptionClick: () -> Unit,
    onCoinsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isPlus = planDisplayName.equals("Plus", ignoreCase = true)
    val planIcon = when {
        isMaxPlan || planDisplayName.equals("Max", ignoreCase = true) -> Icons.Rounded.WorkspacePremium
        isPlus -> Icons.Rounded.Star
        else -> Icons.Rounded.Person
    }

    val iconBgColor = when {
        isMaxPlan || planDisplayName.equals("Max", ignoreCase = true) -> Color(0xFFFFB800).copy(alpha = 0.15f)
        isPlus -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val iconTintColor = when {
        isMaxPlan || planDisplayName.equals("Max", ignoreCase = true) -> Color(0xFFFFB800)
        isPlus -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    CareerPilotCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.CardPadding)
        ) {
            // Header Row: Plan Info + Coins Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Plan info (Icon + Name)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(iconBgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = planIcon,
                            contentDescription = null,
                            tint = iconTintColor,
                            modifier = Modifier.size(Dimens.IconSizeM)
                        )
                    }

                    Column {
                        Text(
                            text = stringResource(R.string.settings_current_plan),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(R.string.settings_plan_format, planDisplayName),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Coins Balance Pill
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(onClick = onCoinsClick)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = Dimens.SpaceM, vertical = Dimens.SpaceS),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceXS)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MonetizationOn,
                            contentDescription = null,
                            tint = Color(0xFFFFB800),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = stringResource(R.string.settings_coins_count, coinBalance),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.SpaceL))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceS)
            ) {
                CareerPilotButton(
                    text = stringResource(
                        if (isMaxPlan) R.string.settings_manage_subscription else R.string.settings_upgrade_plan
                    ),
                    onClick = onManageSubscriptionClick,
                    variant = if (isMaxPlan) ButtonVariant.OUTLINE else ButtonVariant.PRIMARY,
                    modifier = Modifier.weight(1f)
                )

                CareerPilotButton(
                    text = stringResource(R.string.settings_get_coins),
                    onClick = onCoinsClick,
                    variant = ButtonVariant.SECONDARY,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AccountPlanSettingsCardFreePreview() {
    CareerPilotTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            AccountPlanSettingsCard(
                planDisplayName = "Free",
                isMaxPlan = false,
                coinBalance = 0,
                onManageSubscriptionClick = {},
                onCoinsClick = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AccountPlanSettingsCardPlusPreview() {
    CareerPilotTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            AccountPlanSettingsCard(
                planDisplayName = "Plus",
                isMaxPlan = false,
                coinBalance = 80,
                onManageSubscriptionClick = {},
                onCoinsClick = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AccountPlanSettingsCardMaxPreview() {
    CareerPilotTheme(darkTheme = true) {
        Box(modifier = Modifier.padding(16.dp)) {
            AccountPlanSettingsCard(
                planDisplayName = "Max",
                isMaxPlan = true,
                coinBalance = 500,
                onManageSubscriptionClick = {},
                onCoinsClick = {}
            )
        }
    }
}
