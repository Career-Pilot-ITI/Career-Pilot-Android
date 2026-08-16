package com.iti.careerpilot.features.paywall

import com.iti.careerpilot.features.paywall.domain.model.CoinPack
import com.iti.careerpilot.features.paywall.presentation.viewmodel.PaywallState
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PaywallStateTest {

    @Test
    fun `default state exposes the required local offers`() {
        val state = PaywallState()

        assertEquals(0, state.coinBalance)
        assertTrue(state.coinPacks.isEmpty())
        assertEquals("plus", state.subscriptionPlans.find { it.isInitiallySelected }?.id)
        assertFalse(state.isCheckoutInProgress)
    }

    @Test
    fun `isUpgrade returns true when higher tier selected`() {
        val state = PaywallState(
            currentSubscriptionTier = "free",
            selectedPlanId = "plus"
        )

        assertTrue(state.isUpgrade)
        assertFalse(state.isDowngrade)
    }

    @Test
    fun `isUpgrade returns false when same or lower tier selected`() {
        val sameTierState = PaywallState(
            currentSubscriptionTier = "plus",
            selectedPlanId = "plus"
        )
        val lowerTierState = PaywallState(
            currentSubscriptionTier = "pro",
            selectedPlanId = "plus"
        )

        assertFalse(sameTierState.isUpgrade)
        assertFalse(lowerTierState.isUpgrade)
    }

    @Test
    fun `isDowngrade returns true when lower tier selected`() {
        val state = PaywallState(
            currentSubscriptionTier = "pro",
            selectedPlanId = "plus"
        )

        assertTrue(state.isDowngrade)
        assertFalse(state.isUpgrade)
    }

    @Test
    fun `isDowngrade returns false when selectedPlanId is null or higher tier selected`() {
        val nullPlanState = PaywallState(
            currentSubscriptionTier = "pro",
            selectedPlanId = null
        )
        val higherTierState = PaywallState(
            currentSubscriptionTier = "free",
            selectedPlanId = "plus"
        )

        assertFalse(nullPlanState.isDowngrade)
        assertFalse(higherTierState.isDowngrade)
    }

    @Test
    fun `isPlanChangeEnabled returns true only when valid plan selected and not current tier and checkout not in progress`() {
        val validChangeState = PaywallState(
            currentSubscriptionTier = "plus",
            selectedPlanId = "pro",
            isCheckoutInProgress = false
        )

        assertTrue(validChangeState.isPlanChangeEnabled)
    }

    @Test
    fun `isPlanChangeEnabled returns false when plan is null, current tier, or checkout in progress`() {
        val nullPlanState = PaywallState(
            currentSubscriptionTier = "plus",
            selectedPlanId = null,
            isCheckoutInProgress = false
        )
        val sameTierState = PaywallState(
            currentSubscriptionTier = "plus",
            selectedPlanId = "plus",
            isCheckoutInProgress = false
        )
        val checkoutInProgressState = PaywallState(
            currentSubscriptionTier = "plus",
            selectedPlanId = "pro",
            isCheckoutInProgress = true
        )

        assertFalse(nullPlanState.isPlanChangeEnabled)
        assertFalse(sameTierState.isPlanChangeEnabled)
        assertFalse(checkoutInProgressState.isPlanChangeEnabled)
    }

    @Test
    fun `selectedCoinPack returns null when coinPacks is empty`() {
        val state = PaywallState(
            coinPacks = persistentListOf(),
            selectedCoinPackId = "coin_pack_1"
        )

        assertNull(state.selectedCoinPack)
    }

    @Test
    fun `selectedCoinPack returns matching coin pack when present`() {
        val pack = CoinPack(id = "coin_pack_1", coins = 50, priceEgp = 149)
        val state = PaywallState(
            coinPacks = persistentListOf(pack),
            selectedCoinPackId = "coin_pack_1"
        )

        assertEquals(pack, state.selectedCoinPack)
    }

    @Test
    fun `SubscriptionTier getNameRes maps pro and max to paywall_plan_max`() {
        assertEquals(com.iti.careerpilot.payment.R.string.paywall_plan_max, com.iti.careerpilot.features.paywall.domain.model.SubscriptionTier.getNameRes("pro"))
        assertEquals(com.iti.careerpilot.payment.R.string.paywall_plan_max, com.iti.careerpilot.features.paywall.domain.model.SubscriptionTier.getNameRes("max"))
        assertEquals(com.iti.careerpilot.payment.R.string.paywall_plan_max, com.iti.careerpilot.features.paywall.domain.model.SubscriptionTier.getNameRes("PRO"))
        assertEquals(com.iti.careerpilot.payment.R.string.paywall_plan_max, com.iti.careerpilot.features.paywall.domain.model.SubscriptionTier.getNameRes("MAX"))
        assertEquals(com.iti.careerpilot.payment.R.string.paywall_plan_plus, com.iti.careerpilot.features.paywall.domain.model.SubscriptionTier.getNameRes("plus"))
        assertEquals(com.iti.careerpilot.payment.R.string.paywall_plan_free, com.iti.careerpilot.features.paywall.domain.model.SubscriptionTier.getNameRes("free"))
    }

    @Test
    fun `successfulCoinCount returns purchasedCoinCount when set`() {
        val state = PaywallState(
            purchasedCoinCount = 300,
            selectedCoinPackId = "coins_50",
            coinPacks = persistentListOf(CoinPack(id = "coins_50", coins = 50, priceEgp = 149))
        )
        assertEquals(300, state.successfulCoinCount)
    }

    @Test
    fun `successfulCoinCount falls back to selected pack coins when purchasedCoinCount is null`() {
        val state = PaywallState(
            purchasedCoinCount = null,
            selectedCoinPackId = "coins_120",
            coinPacks = persistentListOf(CoinPack(id = "coins_120", coins = 120, priceEgp = 299))
        )
        assertEquals(120, state.successfulCoinCount)
    }

    @Test
    fun `successfulCoinCount falls back to 50 when both purchasedCoinCount and selected pack are null`() {
        val state = PaywallState(
            purchasedCoinCount = null,
            selectedCoinPackId = null,
            coinPacks = persistentListOf()
        )
        assertEquals(50, state.successfulCoinCount)
    }

    @Test
    fun `plus plan features include technical quizzes`() {
        val state = PaywallState()
        val plusPlan = state.subscriptionPlans.first { it.id == "plus" }

        assertTrue(
            "Plus plan should include technical quizzes",
            plusPlan.features.contains(com.iti.careerpilot.payment.R.string.paywall_feature_technical_quizzes)
        )
        assertEquals(
            listOf(
                com.iti.careerpilot.payment.R.string.paywall_feature_everything_in_free,
                com.iti.careerpilot.payment.R.string.paywall_feature_ats_scan,
                com.iti.careerpilot.payment.R.string.paywall_feature_resume_optimizer,
                com.iti.careerpilot.payment.R.string.paywall_feature_technical_quizzes,
                com.iti.careerpilot.payment.R.string.paywall_feature_80_monthly_coins
            ),
            plusPlan.features
        )
    }

    @Test
    fun `unlockedFeatures include technical quizzes`() {
        val state = PaywallState()

        assertTrue(
            "unlockedFeatures should include technical quizzes",
            state.unlockedFeatures.contains(com.iti.careerpilot.payment.R.string.paywall_feature_technical_quizzes)
        )
        assertEquals(
            listOf(
                com.iti.careerpilot.payment.R.string.paywall_feature_everything_in_free,
                com.iti.careerpilot.payment.R.string.paywall_feature_ats_scan,
                com.iti.careerpilot.payment.R.string.paywall_feature_resume_optimizer,
                com.iti.careerpilot.payment.R.string.paywall_feature_technical_quizzes,
                com.iti.careerpilot.payment.R.string.paywall_feature_80_monthly_coins
            ),
            state.unlockedFeatures
        )
    }

    @Test
    fun `free and pro subscription plan features are configured correctly`() {
        val state = PaywallState()
        val freePlan = state.subscriptionPlans.first { it.id == "free" }
        val proPlan = state.subscriptionPlans.first { it.id == "pro" }

        assertTrue(
            "Free plan should include participate in challenges",
            freePlan.features.contains(com.iti.careerpilot.payment.R.string.paywall_feature_enter_challenges)
        )
        assertTrue(
            "Pro/Max plan should include create challenges",
            proPlan.features.contains(com.iti.careerpilot.payment.R.string.paywall_feature_create_challenges)
        )
        assertEquals(
            listOf(
                com.iti.careerpilot.payment.R.string.paywall_feature_voice_mock_interview,
                com.iti.careerpilot.payment.R.string.paywall_feature_performance_analytics,
                com.iti.careerpilot.payment.R.string.paywall_feature_voice_scoring,
                com.iti.careerpilot.payment.R.string.paywall_feature_enter_challenges
            ),
            freePlan.features
        )
        assertEquals(
            listOf(
                com.iti.careerpilot.payment.R.string.paywall_feature_everything_in_plus,
                com.iti.careerpilot.payment.R.string.paywall_feature_video_interview,
                com.iti.careerpilot.payment.R.string.paywall_feature_create_challenges,
                com.iti.careerpilot.payment.R.string.paywall_feature_160_monthly_coins
            ),
            proPlan.features
        )
    }
}


