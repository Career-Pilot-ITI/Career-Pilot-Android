package com.iti.careerpilot.core.access

import com.iti.careerpilot.core.access.domain.usecase.CheckFeatureAccessUseCase
import com.iti.careerpilot.core.access.testing.FakeAccessRepository
import com.iti.core.model.AccessState
import com.iti.core.model.FeatureAccess
import com.iti.core.model.FeatureKey
import com.iti.core.model.FeatureQuota
import com.iti.core.model.Plan
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.time.Clock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.hours

class CheckFeatureAccessUseCaseTest {

    private lateinit var fakeRepo: FakeAccessRepository
    private lateinit var useCase: CheckFeatureAccessUseCase

    @Before
    fun setUp() {
        fakeRepo = FakeAccessRepository()
        useCase = CheckFeatureAccessUseCase(fakeRepo)
    }

    // ── Scenario 1: Feature not in plan → Locked(requiredPlan) ──────────

    @Test
    fun `feature not in plan returns Locked with required plan`() = runTest {
        // FREE plan only has CvAiAnalysis and MockInterviews, not AdvancedReports
        fakeRepo.mutableState.value = AccessState(
            plan = Plan.FREE,
            features = PlanAccessMap.featuresFor(Plan.FREE),
            quotas = emptyMap(),
            expiresAt = null,
            lastSyncedAt = Clock.System.now(),
            coinBalance = 0
        )

        val result = useCase(FeatureKey.AdvancedReports).first()

        assertTrue(result is FeatureAccess.Locked)
        // AdvancedReports first appears in MAX plan
        assertEquals(Plan.MAX, (result as FeatureAccess.Locked).requiredPlan)
    }

    // ── Scenario 2: Feature in plan & quota remaining > 0 → Granted ────

    @Test
    fun `feature in plan with remaining quota returns Granted`() = runTest {
        val quota = FeatureQuota(
            feature = FeatureKey.MockInterviews,
            remaining = 5,
            max = 10,
            coinCost = null
        )
        fakeRepo.mutableState.value = AccessState(
            plan = Plan.PLUS,
            features = PlanAccessMap.featuresFor(Plan.PLUS),
            quotas = mapOf(FeatureKey.MockInterviews to quota),
            expiresAt = null,
            lastSyncedAt = Clock.System.now(),
            coinBalance = 100
        )

        val result = useCase(FeatureKey.MockInterviews).first()

        assertTrue(result is FeatureAccess.Granted)
        assertEquals(quota, (result as FeatureAccess.Granted).quota)
    }

    // ── Scenario 3: Quota exhausted with coinCost > 0 → CoinTopUpRequired

    @Test
    fun `quota exhausted with positive coinCost returns CoinTopUpRequired`() = runTest {
        val quota = FeatureQuota(
            feature = FeatureKey.MockInterviews,
            remaining = 0,
            max = 10,
            coinCost = 50
        )
        fakeRepo.mutableState.value = AccessState(
            plan = Plan.PLUS,
            features = PlanAccessMap.featuresFor(Plan.PLUS),
            quotas = mapOf(FeatureKey.MockInterviews to quota),
            expiresAt = null,
            lastSyncedAt = Clock.System.now(),
            coinBalance = 30
        )

        val result = useCase(FeatureKey.MockInterviews).first()

        assertTrue(result is FeatureAccess.CoinTopUpRequired)
        val topUp = result as FeatureAccess.CoinTopUpRequired
        assertEquals(FeatureKey.MockInterviews, topUp.feature)
        assertEquals(50, topUp.coinCost)
        assertEquals(30, topUp.currentCoins)
    }

    // ── Scenario 4: Quota exhausted with coinCost == 0 → Locked ────────

    @Test
    fun `quota exhausted with zero coinCost returns Locked`() = runTest {
        val quota = FeatureQuota(
            feature = FeatureKey.MockInterviews,
            remaining = 0,
            max = 10,
            coinCost = 0
        )
        fakeRepo.mutableState.value = AccessState(
            plan = Plan.PLUS,
            features = PlanAccessMap.featuresFor(Plan.PLUS),
            quotas = mapOf(FeatureKey.MockInterviews to quota),
            expiresAt = null,
            lastSyncedAt = Clock.System.now(),
            coinBalance = 100
        )

        val result = useCase(FeatureKey.MockInterviews).first()

        assertTrue(result is FeatureAccess.Locked)
    }

    // ── Scenario 5: Quota exhausted with coinCost == null → Locked ─────

    @Test
    fun `quota exhausted with null coinCost returns Locked`() = runTest {
        val quota = FeatureQuota(
            feature = FeatureKey.MockInterviews,
            remaining = 0,
            max = 10,
            coinCost = null
        )
        fakeRepo.mutableState.value = AccessState(
            plan = Plan.PLUS,
            features = PlanAccessMap.featuresFor(Plan.PLUS),
            quotas = mapOf(FeatureKey.MockInterviews to quota),
            expiresAt = null,
            lastSyncedAt = Clock.System.now(),
            coinBalance = 100
        )

        val result = useCase(FeatureKey.MockInterviews).first()

        assertTrue(result is FeatureAccess.Locked)
    }

    // ── Scenario 6: Cache stale (>24h) → StaleCacheBlocked ─────────────

    @Test
    fun `stale cache returns StaleCacheBlocked`() = runTest {
        val staleTime = Clock.System.now() - 25.hours
        fakeRepo.mutableState.value = AccessState(
            plan = Plan.PLUS,
            features = PlanAccessMap.featuresFor(Plan.PLUS),
            quotas = emptyMap(),
            expiresAt = null,
            lastSyncedAt = staleTime,
            coinBalance = 100
        )

        val result = useCase(FeatureKey.MockInterviews).first()

        assertEquals(FeatureAccess.StaleCacheBlocked, result)
    }
}
