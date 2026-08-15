package com.iti.careerpilot.core.designsystem.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MonetizationOn
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.iti.careerpilot.core.designsystem.R
import androidx.compose.ui.unit.dp
import com.iti.core.model.FeatureAccess
import com.iti.core.model.FeatureQuota
import com.iti.core.model.Plan

/**
 * Inline badge shown on gated feature screens explaining what the user will pay
 * (or what they get) before they interact with the feature.
 *
 * @param access         Current [FeatureAccess] collected from CheckFeatureAccessUseCase.
 * @param coinBalance    User's current coin balance.
 * @param planDisplayName Display name of the user's current plan, e.g. "Plus".
 * @param onUpgradeClick Called when the user taps the badge in [FeatureAccess.Locked] state.
 */
@Composable
fun FeaturePricingBadge(
    access: FeatureAccess,
    coinBalance: Int,
    planDisplayName: String,
    modifier: Modifier = Modifier,
    onUpgradeClick: () -> Unit = {},
) {
    AnimatedContent(targetState = access, label = "pricing_badge") { currentAccess ->
        when (currentAccess) {
            is FeatureAccess.Unknown -> Unit

            is FeatureAccess.StaleCacheBlocked -> {
                Surface(
                    modifier = modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = androidx.compose.ui.res.stringResource(com.iti.careerpilot.core.designsystem.R.string.pricing_badge_checking),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            is FeatureAccess.Locked -> {
                Surface(
                    modifier = modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.errorContainer,
                    onClick = onUpgradeClick,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(20.dp),
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = androidx.compose.ui.res.stringResource(
                                    com.iti.careerpilot.core.designsystem.R.string.pricing_badge_requires_plan,
                                    currentAccess.requiredPlan.displayName()
                                ),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                            )
                            Text(
                                text = androidx.compose.ui.res.stringResource(com.iti.careerpilot.core.designsystem.R.string.pricing_badge_tap_to_upgrade),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.75f),
                            )
                        }
                    }
                }
            }

            is FeatureAccess.CoinTopUpRequired -> {
                Surface(
                    modifier = modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(20.dp),
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = androidx.compose.ui.res.stringResource(
                                    com.iti.careerpilot.core.designsystem.R.string.pricing_badge_coins_needed,
                                    currentAccess.coinCost
                                ),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                            )
                            Text(
                                text = androidx.compose.ui.res.stringResource(
                                    com.iti.careerpilot.core.designsystem.R.string.pricing_badge_balance_insufficient,
                                    coinBalance
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.75f),
                            )
                        }
                    }
                }
            }

            is FeatureAccess.Granted -> {
                GrantedPricingBadge(
                    quota = currentAccess.quota,
                    coinBalance = coinBalance,
                    planDisplayName = planDisplayName,
                    modifier = modifier,
                )
            }
        }
    }
}

@Composable
private fun GrantedPricingBadge(
    quota: FeatureQuota?,
    coinBalance: Int,
    planDisplayName: String,
    modifier: Modifier = Modifier,
) {
    val coinCost = quota?.coinCost
    val remaining = quota?.remaining
    val max = quota?.max

    val (icon: ImageVector, title: String, subtitle: String) = when {
        coinCost != null && coinCost > 0 -> Triple(
            Icons.Rounded.MonetizationOn,
            androidx.compose.ui.res.stringResource(com.iti.careerpilot.core.designsystem.R.string.pricing_badge_coins_per_use, coinCost),
            androidx.compose.ui.res.stringResource(com.iti.careerpilot.core.designsystem.R.string.pricing_badge_balance, coinBalance),
        )

        remaining != null && max != null -> Triple(
            Icons.Rounded.CheckCircle,
            androidx.compose.ui.res.stringResource(com.iti.careerpilot.core.designsystem.R.string.pricing_badge_quota_remaining, remaining, max),
            androidx.compose.ui.res.stringResource(com.iti.careerpilot.core.designsystem.R.string.pricing_badge_included_in_plan, planDisplayName),
        )

        planDisplayName.equals("Free", ignoreCase = true) -> Triple(
            Icons.Rounded.MonetizationOn,
            androidx.compose.ui.res.stringResource(com.iti.careerpilot.core.designsystem.R.string.pricing_badge_coins_per_use, 20),
            androidx.compose.ui.res.stringResource(com.iti.careerpilot.core.designsystem.R.string.pricing_badge_balance, coinBalance),
        )

        else -> Triple(
            Icons.Rounded.CheckCircle,
            androidx.compose.ui.res.stringResource(com.iti.careerpilot.core.designsystem.R.string.pricing_badge_included_in_plan, planDisplayName),
            androidx.compose.ui.res.stringResource(com.iti.careerpilot.core.designsystem.R.string.pricing_badge_unlimited),
        )
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(20.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.75f),
                )
            }
        }
    }
}
