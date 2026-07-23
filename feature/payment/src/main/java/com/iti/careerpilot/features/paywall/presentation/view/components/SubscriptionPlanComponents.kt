package com.iti.careerpilot.features.paywall.presentation.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import com.iti.careerpilot.core.designsystem.CareerPilotPalette as CareerPilotColors
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard
import com.iti.careerpilot.features.paywall.domain.model.SubscriptionPlan
import com.iti.careerpilot.payment.R

@Composable
fun PlanSelectorRow(
    plans: List<SubscriptionPlan>,
    selectedPlanId: String?,
    currentTierId: String,
    onSelectPlan: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = Dimens.BorderThin,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(percent = 100)
            )
            .padding(Dimens.SpaceXS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        plans.forEach { plan ->
            key(plan.id) {
                val isSelected = plan.id == selectedPlanId
                val isCurrentPlan = plan.id == currentTierId

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(percent = 100))
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { onSelectPlan(plan.id) }
                        .padding(vertical = Dimens.SpaceM),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(id = plan.nameRes),
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        if (isCurrentPlan) {
                            Spacer(modifier = Modifier.width(Dimens.SpaceXS))
                            Box(
                                modifier = Modifier
                                    .size(Dimens.SpaceS)
                                    .clip(CircleShape)
                                    .background(if (isSelected) MaterialTheme.colorScheme.onPrimary else CareerPilotColors.green)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SubscriptionPlanDetailsCard(
    plan: SubscriptionPlan?,
    selectedPlanName: String,
    isCurrentPlan: Boolean,
    modifier: Modifier = Modifier
) {
    CareerPilotCard(
        elevation = Dimens.SpaceS,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.SpaceL, vertical = Dimens.SpaceXL)
        ) {
            if (plan?.originalPriceEgp != null && plan.priceEgp != null) {
                val discount = plan.discountPercentage ?: 0
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.paywall_currency_egp, plan.originalPriceEgp),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.outline,
                        textDecoration = TextDecoration.LineThrough
                    )
                    Spacer(modifier = Modifier.width(Dimens.SpaceS))
                    Surface(
                        color = CareerPilotColors.green.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(percent = 100)
                    ) {
                        Text(
                            text = stringResource(R.string.paywall_save_discount, discount),
                            style = MaterialTheme.typography.labelSmall,
                            color = CareerPilotColors.green,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = Dimens.SpaceS, vertical = Dimens.SpaceXS)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(Dimens.SpaceXS))
            }

            // Price Section
            Row(verticalAlignment = Alignment.Bottom) {
                if ((plan?.priceEgp ?: 0) > 0) {
                    Text(
                        text = stringResource(R.string.paywall_currency_egp, plan?.priceEgp ?: 0),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.paywall_price_per_month),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(bottom = Dimens.SpaceS)
                    )
                } else {
                    Text(
                        text = stringResource(R.string.paywall_plan_free),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.SpaceM))

            // Plan Name Banner
            Row(verticalAlignment = Alignment.CenterVertically) {
                val icon = when (plan?.id) {
                    "plus" -> Icons.Rounded.Star
                    "pro" -> Icons.Rounded.WorkspacePremium
                    else -> Icons.Rounded.Person
                }

                Box(
                    modifier = Modifier
                        .size(Dimens.ButtonHeight)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            ),
                            RoundedCornerShape(Dimens.SpaceM)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(Dimens.SpaceXXL)
                    )
                }
                Spacer(modifier = Modifier.width(Dimens.SpaceS))
                Text(
                    text = stringResource(R.string.paywall_plan_name_suffix, selectedPlanName),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CareerPilotColors.amber
                )
                if (isCurrentPlan) {
                    Spacer(modifier = Modifier.width(Dimens.SpaceS))
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(percent = 100)
                    ) {
                        Text(
                            text = stringResource(R.string.paywall_current_plan_badge),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = Dimens.SpaceS, vertical = Dimens.SpaceXS)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.SpaceL))

            PlanFeaturesList(features = plan?.features ?: emptyList())
        }
    }
}

@Composable
fun PlanFeaturesList(
    features: List<Int>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        features.forEachIndexed { index, feature ->
            key(feature) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Dimens.SpaceS)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = CareerPilotColors.teal,
                        modifier = Modifier.size(Dimens.SpaceXL)
                    )
                    Spacer(modifier = Modifier.width(Dimens.SpaceM))
                    Text(
                        text = stringResource(id = feature),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (index < features.size - 1) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                }
            }
        }
    }
}
