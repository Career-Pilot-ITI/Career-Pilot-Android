package com.iti.careerpilot.features.paywall.presentation.viewmodel

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.iti.careerpilot.features.paywall.domain.model.CoinPack
import com.iti.careerpilot.features.paywall.domain.model.PaymentFailureReason
import com.iti.careerpilot.features.paywall.domain.model.SubscriptionPlan
import com.iti.careerpilot.features.paywall.domain.model.SubscriptionTier
import com.iti.careerpilot.payment.R
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

enum class CheckoutItemType {
    SUBSCRIPTION,
    COIN_PACK
}

@Immutable
data class PaywallState(
    val isLoadingSubscription: Boolean = false,
    val isLoadingBalance: Boolean = false,
    val isLoadingTierPrices: Boolean = false,
    val isLoadingCoinPacks: Boolean = false,
    val isCheckoutInProgress: Boolean = false,
    val coinBalance: Int = 0,
    val currentSubscriptionTier: String = "",
    val selectedPlanId: String? = null,
    val selectedCoinPackId: String? = null,
    val checkoutItemType: CheckoutItemType = CheckoutItemType.SUBSCRIPTION,
    val merchantOrderId: String? = null,
    val failureReason: PaymentFailureReason = PaymentFailureReason.DECLINED,
    val preCheckoutBalance: Int? = null,
    val preCheckoutTier: String? = null,
    val usedFreeSessions: Int = 5,
    val maxFreeSessions: Int = 5,
    val resetDate: String = "1st of next month",
    val subscriptionPlans: ImmutableList<SubscriptionPlan> = persistentListOf(
        SubscriptionPlan(
            id = "free",
            nameRes = R.string.paywall_plan_free,
            priceEgp = 0,
            originalPriceEgp = null,
            features = persistentListOf(
                R.string.paywall_feature_5_free_sessions,
                R.string.paywall_feature_basic_radar,
                R.string.paywall_feature_limited_ai
            )
        ),
        SubscriptionPlan(
            id = "plus",
            nameRes = R.string.paywall_plan_plus,
            priceEgp = 199,
            originalPriceEgp = null,
            isInitiallySelected = true,
            features = persistentListOf(
                R.string.paywall_feature_detailed_radar,
                R.string.paywall_feature_priority_ai,
                R.string.paywall_feature_coaching_tips_library
            )
        ),
        SubscriptionPlan(
            id = "pro",
            nameRes = R.string.paywall_plan_max,
            priceEgp = 499,
            originalPriceEgp = null,
            features = persistentListOf(
                R.string.paywall_feature_unlimited_sessions,
                R.string.paywall_feature_advanced_radar,
                R.string.paywall_feature_instant_ai,
                R.string.paywall_feature_1on1_mentorship
            )
        )
    ),
    val coinPacks: ImmutableList<CoinPack> = persistentListOf(),
    val unlockedFeatures: ImmutableList<Int> = persistentListOf(
        R.string.paywall_feature_detailed_radar,
        R.string.paywall_feature_priority_ai,
        R.string.paywall_feature_coaching_tips_library,
        R.string.paywall_feature_unlimited_sessions
    )
) {
    val selectedPlan: SubscriptionPlan?
        get() = subscriptionPlans.find { it.id == selectedPlanId }

    val currentTierId: String
        get() = SubscriptionTier.normalizeTierId(currentSubscriptionTier)

    val currentTierLevel: Int
        get() = SubscriptionTier.getTierLevel(currentSubscriptionTier)

    val selectedLevel: Int
        get() = SubscriptionTier.getTierLevel(selectedPlanId)

    val isUpgrade: Boolean
        get() = selectedLevel > currentTierLevel

    val isDowngrade: Boolean
        get() = selectedLevel < currentTierLevel && selectedPlanId != null

    val isPlanChangeEnabled: Boolean
        get() = selectedPlanId != null && selectedLevel != currentTierLevel && !isCheckoutInProgress

    val selectedCoinPack: CoinPack?
        get() = coinPacks.find { it.id == selectedCoinPackId }

    @get:StringRes
    val successfulPlanNameRes: Int
        get() = selectedPlan?.nameRes ?: R.string.paywall_plan_plus

    val successfulCoinCount: Int
        get() = selectedCoinPack?.coins ?: 500
}
