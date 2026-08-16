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
    val purchasedCoinCount: Int? = null,
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
                R.string.paywall_feature_voice_mock_interview,
                R.string.paywall_feature_performance_analytics,
                R.string.paywall_feature_voice_scoring,
                R.string.paywall_feature_enter_challenges
            )
        ),
        SubscriptionPlan(
            id = "plus",
            nameRes = R.string.paywall_plan_plus,
            priceEgp = 349,
            originalPriceEgp = null,
            isInitiallySelected = true,
            features = persistentListOf(
                R.string.paywall_feature_everything_in_free,
                R.string.paywall_feature_ats_scan,
                R.string.paywall_feature_resume_optimizer,
                R.string.paywall_feature_technical_quizzes,
                R.string.paywall_feature_80_monthly_coins
            )
        ),
        SubscriptionPlan(
            id = "pro",
            nameRes = R.string.paywall_plan_max,
            priceEgp = 599,
            originalPriceEgp = null,
            features = persistentListOf(
                R.string.paywall_feature_everything_in_plus,
                R.string.paywall_feature_video_interview,
                R.string.paywall_feature_create_challenges,
                R.string.paywall_feature_160_monthly_coins
            )
        )
    ),
    val coinPacks: ImmutableList<CoinPack> = persistentListOf(),
    val unlockedFeatures: ImmutableList<Int> = persistentListOf(
        R.string.paywall_feature_everything_in_free,
        R.string.paywall_feature_ats_scan,
        R.string.paywall_feature_resume_optimizer,
        R.string.paywall_feature_technical_quizzes,
        R.string.paywall_feature_80_monthly_coins
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
        get() = purchasedCoinCount ?: selectedCoinPack?.coins ?: 50
}
