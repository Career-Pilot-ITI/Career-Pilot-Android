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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
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
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                    )
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
                                text = "Requires ${currentAccess.requiredPlan.displayName()} plan",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                            )
                            Text(
                                text = "Tap to upgrade and unlock this feature",
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
                                text = "${currentAccess.coinCost} coins needed to continue",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                            )
                            Text(
                                text = "Your balance: $coinBalance coins (insufficient)",
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
            "$coinCost coins per use",
            "Your balance: $coinBalance coins",
        )

        remaining != null && max != null -> Triple(
            Icons.Rounded.CheckCircle,
            "$remaining of $max uses remaining",
            "Included in $planDisplayName plan",
        )

        else -> Triple(
            Icons.Rounded.CheckCircle,
            "Included in $planDisplayName plan",
            "No usage limits",
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
