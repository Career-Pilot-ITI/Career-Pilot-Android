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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.core.designsystem.CareerPilotPalette
import com.iti.careerpilot.core.designsystem.CareerPilotTheme
import com.iti.careerpilot.core.designsystem.R
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
    coinCost: Int? = null,
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
                    coinCost = coinCost,
                    modifier = modifier,
                )
            }
        }
    }
}

/**
 * Standalone badge displaying a feature coin cost, e.g. "20 Coins".
 *
 * @param coinCost The cost in coins required for this feature or action.
 * @param modifier Modifier applied to the container.
 * @param compact  If true, renders as an inline chip/pill. If false, renders as a full-width card.
 * @param onClick  Optional click callback if the badge is interactive.
 */
@Composable
fun FeaturePricingBadge(
    coinCost: Int,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val coinText = stringResource(R.string.pricing_badge_coins, coinCost)

    if (compact) {
        Surface(
            modifier = modifier,
            shape = MaterialTheme.shapes.small,
            color = CareerPilotPalette.amber.copy(alpha = 0.12f),
            onClick = onClick ?: {},
            enabled = onClick != null,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.MonetizationOn,
                    contentDescription = null,
                    tint = CareerPilotPalette.amber,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = coinText,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = CareerPilotPalette.amber,
                )
            }
        }
    } else {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.secondaryContainer,
            onClick = onClick ?: {},
            enabled = onClick != null,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.MonetizationOn,
                    contentDescription = null,
                    tint = CareerPilotPalette.amber,
                    modifier = Modifier.size(20.dp),
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = stringResource(R.string.pricing_badge_coins_per_use, coinCost),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                    Text(
                        text = coinText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.75f),
                    )
                }
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
    coinCost: Int? = null,
) {
    val quotaCoinCost = quota?.coinCost
    val remaining = quota?.remaining
    val max = quota?.max
    val effectiveCoinCost = (quotaCoinCost?.takeIf { it > 0 }) ?: (coinCost?.takeIf { it > 0 })

    val (icon, title, subtitle) = when {
        remaining != null && max != null -> Triple(
            Icons.Rounded.CheckCircle,
            stringResource(R.string.pricing_badge_quota_remaining, remaining, max),
            stringResource(R.string.pricing_badge_included_in_plan, planDisplayName),
        )

        effectiveCoinCost != null -> Triple(
            Icons.Rounded.MonetizationOn,
            stringResource(R.string.pricing_badge_coins_per_use, effectiveCoinCost),
            stringResource(R.string.pricing_badge_balance, coinBalance),
        )

        else -> Triple(
            Icons.Rounded.CheckCircle,
            stringResource(R.string.pricing_badge_included_in_plan, planDisplayName),
            stringResource(R.string.pricing_badge_unlimited),
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

@Preview(showBackground = true)
@Composable
private fun FeaturePricingBadgeStandalonePreview() {
    CareerPilotTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FeaturePricingBadge(coinCost = 15, compact = false)
            FeaturePricingBadge(coinCost = 20, compact = true)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E1428)
@Composable
private fun FeaturePricingBadgeStandaloneDarkPreview() {
    CareerPilotTheme(darkTheme = true) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FeaturePricingBadge(coinCost = 15, compact = false)
            FeaturePricingBadge(coinCost = 20, compact = true)
        }
    }
}
