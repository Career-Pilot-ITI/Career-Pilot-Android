package com.iti.careerpilot.core.access

import com.iti.careerpilot.core.access.domain.usecase.handle
import com.iti.core.model.FeatureAccess
import com.iti.core.model.FeatureKey
import com.iti.core.model.Plan
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FeatureAccessHandlerTest {

    // ─── helpers ──────────────────────────────────────────────────────────────

    private fun lockedAccess() = FeatureAccess.Locked(requiredPlan = Plan.PLUS)
    private fun coinTopUpAccess() = FeatureAccess.CoinTopUpRequired(
        feature = FeatureKey.AtsFeatures,
        coinCost = 50,
        currentCoins = 10,
    )

    // ─── tests ────────────────────────────────────────────────────────────────

    @Test
    fun `Granted - onGranted called, others not called`() = runTest {
        var grantedCalled = false
        var lockedCalled = false
        var coinCalled = false
        var staleCalled = false

        val access: FeatureAccess = FeatureAccess.Granted()
        access.handle(
            onGranted = { grantedCalled = true },
            onLocked = { lockedCalled = true },
            onCoinTopUpRequired = { coinCalled = true },
            onStale = { staleCalled = true },
        )

        assertTrue("onGranted should be called", grantedCalled)
        assertFalse("onLocked should NOT be called", lockedCalled)
        assertFalse("onCoinTopUpRequired should NOT be called", coinCalled)
        assertFalse("onStale should NOT be called", staleCalled)
    }

    @Test
    fun `Locked - onLocked called with correct plan`() = runTest {
        var receivedPlan: Plan? = null
        var grantedCalled = false

        val access: FeatureAccess = lockedAccess()
        access.handle(
            onGranted = { grantedCalled = true },
            onLocked = { locked -> receivedPlan = locked.requiredPlan },
            onCoinTopUpRequired = {},
        )

        assertFalse("onGranted should NOT be called", grantedCalled)
        assertEquals("onLocked should receive the correct plan", Plan.PLUS, receivedPlan)
    }

    @Test
    fun `CoinTopUpRequired - onCoinTopUpRequired called with correct cost`() = runTest {
        var receivedCost: Int? = null
        var grantedCalled = false

        val access: FeatureAccess = coinTopUpAccess()
        access.handle(
            onGranted = { grantedCalled = true },
            onLocked = {},
            onCoinTopUpRequired = { coinReq -> receivedCost = coinReq.coinCost },
        )

        assertFalse("onGranted should NOT be called", grantedCalled)
        assertEquals("onCoinTopUpRequired should receive correct cost", 50, receivedCost)
    }

    @Test
    fun `StaleCacheBlocked - onStale called`() = runTest {
        var staleCalled = false
        var grantedCalled = false

        val access: FeatureAccess = FeatureAccess.StaleCacheBlocked
        access.handle(
            onGranted = { grantedCalled = true },
            onLocked = {},
            onCoinTopUpRequired = {},
            onStale = { staleCalled = true },
        )

        assertFalse("onGranted should NOT be called", grantedCalled)
        assertTrue("onStale should be called", staleCalled)
    }

    @Test
    fun `Unknown - no handler called`() = runTest {
        var grantedCalled = false
        var lockedCalled = false
        var coinCalled = false
        var staleCalled = false

        val access: FeatureAccess = FeatureAccess.Unknown
        access.handle(
            onGranted = { grantedCalled = true },
            onLocked = { lockedCalled = true },
            onCoinTopUpRequired = { coinCalled = true },
            onStale = { staleCalled = true },
        )

        assertFalse("onGranted should NOT be called for Unknown", grantedCalled)
        assertFalse("onLocked should NOT be called for Unknown", lockedCalled)
        assertFalse("onCoinTopUpRequired should NOT be called for Unknown", coinCalled)
        assertFalse("onStale should NOT be called for Unknown", staleCalled)
    }
}
