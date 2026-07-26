package com.iti.careerpilot.features.paywall

import com.iti.careerpilot.features.paywall.domain.model.CoinPack
import com.iti.careerpilot.features.paywall.presentation.viewmodel.PaywallState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class PaywallStateTest {

    @Test
    fun `default state exposes the required local offers`() {
        val state = PaywallState()

        assertEquals(0, state.coinBalance)
        assertEquals(listOf(100, 500, 1000), state.coinPacks.map(CoinPack::coins))
        assertEquals("plus", state.subscriptionPlans.find { it.isInitiallySelected }?.id)
        assertFalse(state.isCheckoutInProgress)
    }
}
