package com.iti.careerpilot.core.designsystem.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.core.designsystem.R
import com.iti.core.model.FeatureAccess
import com.iti.core.model.Plan

/**
 * Animated gate that shows [content] when access is [FeatureAccess.Granted],
 * and a contextual locked/top-up/network card for every other state.
 * Uses [AnimatedContent] with fade + expand/shrink transitions.
 */
@Composable
fun FeatureGate(
    access: FeatureAccess,
    onUpgrade: (Plan) -> Unit,
    onSpendCoins: (FeatureAccess.CoinTopUpRequired) -> Unit,
    onBuyCoins: () -> Unit,
    modifier: Modifier = Modifier,
    loading: @Composable () -> Unit = { LoadingWave() },
    content: @Composable () -> Unit,
) {
    AnimatedContent(
        targetState = access,
        modifier = modifier,
        transitionSpec = {
            (fadeIn(tween(300)) + expandVertically(tween(300)))
                .togetherWith(fadeOut(tween(300)) + shrinkVertically(tween(300)))
        },
        contentKey = { it::class },
        label = "FeatureGateTransition",
    ) { state ->
        when (state) {
            is FeatureAccess.Unknown -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.SpaceXXL),
                    contentAlignment = Alignment.Center,
                ) {
                    loading()
                }
            }

            is FeatureAccess.Granted -> {
                content()
            }

            is FeatureAccess.Locked -> {
                LockedFeatureCard(
                    requiredPlan = state.requiredPlan,
                    onUpgrade = { onUpgrade(state.requiredPlan) },
                )
            }

            is FeatureAccess.CoinTopUpRequired -> {
                CoinTopUpCard(
                    coinCost = state.coinCost,
                    currentCoins = state.currentCoins,
                    onSpendCoins = { onSpendCoins(state) },
                    onBuyCoins = onBuyCoins,
                )
            }

            is FeatureAccess.StaleCacheBlocked -> {
                NetworkRequiredCard()
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Locked-feature card – shown when the user's plan is insufficient
// ---------------------------------------------------------------------------

@Composable
fun LockedFeatureCard(
    requiredPlan: Plan,
    onUpgrade: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pulseAlpha = rememberPulsingGlowAlpha()
    val planName = requiredPlan.displayName()

    CareerPilotCard(modifier = modifier.alpha(pulseAlpha)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.CardPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                modifier = Modifier.size(Dimens.IconSizeL),
                tint = MaterialTheme.colorScheme.primary,
            )

            Spacer(Modifier.height(Dimens.SpaceM))

            Text(
                text = stringResource(R.string.plan_upgrade_required_title),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(Dimens.SpaceS))

            Text(
                text = stringResource(R.string.plan_upgrade_required_message, planName),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(Dimens.SpaceL))

            CareerPilotButton(
                text = stringResource(R.string.upgrade_to_plan, planName),
                onClick = onUpgrade,
                variant = ButtonVariant.PRIMARY,
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Coin top-up card – shown when the monthly quota is exhausted
// ---------------------------------------------------------------------------

@Composable
fun CoinTopUpCard(
    coinCost: Int,
    currentCoins: Int,
    onSpendCoins: () -> Unit,
    onBuyCoins: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pulseAlpha = rememberPulsingGlowAlpha()
    val canAfford = currentCoins >= coinCost

    CareerPilotCard(modifier = modifier.alpha(pulseAlpha)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.CardPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                modifier = Modifier.size(Dimens.IconSizeL),
                tint = MaterialTheme.colorScheme.primary,
            )

            Spacer(Modifier.height(Dimens.SpaceM))

            Text(
                text = stringResource(R.string.quota_reached_title),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(Dimens.SpaceS))

            Text(
                text = stringResource(R.string.quota_reached_message, coinCost),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(Dimens.SpaceL))

            if (canAfford) {
                CareerPilotButton(
                    text = stringResource(R.string.spend_coins_action, coinCost, currentCoins),
                    onClick = onSpendCoins,
                    variant = ButtonVariant.PRIMARY,
                )
            } else {
                CareerPilotButton(
                    text = stringResource(
                        R.string.get_more_coins_action,
                        currentCoins,
                        coinCost,
                    ),
                    onClick = onBuyCoins,
                    variant = ButtonVariant.SECONDARY,
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Network-required card – shown when cache is stale and offline
// ---------------------------------------------------------------------------

@Composable
private fun NetworkRequiredCard(
    modifier: Modifier = Modifier,
) {
    CareerPilotCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.CardPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.WifiOff,
                contentDescription = null,
                modifier = Modifier.size(Dimens.IconSizeL),
                tint = MaterialTheme.colorScheme.error,
            )

            Spacer(Modifier.height(Dimens.SpaceM))

            Text(
                text = stringResource(R.string.network_required_title),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(Dimens.SpaceS))

            Text(
                text = stringResource(R.string.network_required_message),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Shared pulsing-glow animation helper
// ---------------------------------------------------------------------------

@Composable
private fun rememberPulsingGlowAlpha(): Float {
    val transition = rememberInfiniteTransition(label = "pulseGlow")
    val alpha by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulseAlpha",
    )
    return alpha
}
