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
        // FREE plan only has MockInterviews and VoicePracticeMode, not AdvancedReports
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

    @Test
    fun `FREE plan user accessing PLUS feature returns Locked PLUS even with sufficient coins`() = runTest {
        // CvAiAnalysis costs 15 coins in FeaturePricingMap, but user is on FREE plan
        // Coins CANNOT bypass an un-entitled plan tier
        fakeRepo.mutableState.value = AccessState(
            plan = Plan.FREE,
            features = PlanAccessMap.featuresFor(Plan.FREE),
            quotas = emptyMap(),
            expiresAt = null,
            lastSyncedAt = Clock.System.now(),
            coinBalance = 100
        )

        val result = useCase(FeatureKey.CvAiAnalysis).first()

        assertTrue(result is FeatureAccess.Locked)
        assertEquals(Plan.PLUS, (result as FeatureAccess.Locked).requiredPlan)
    }

    @Test
    fun `FREE plan user accessing ATS feature returns Locked PLUS even with sufficient coins`() = runTest {
        fakeRepo.mutableState.value = AccessState(
            plan = Plan.FREE,
            features = PlanAccessMap.featuresFor(Plan.FREE),
            quotas = emptyMap(),
            expiresAt = null,
            lastSyncedAt = Clock.System.now(),
            coinBalance = 50
        )

        val result = useCase(FeatureKey.AtsFeatures).first()

        assertTrue(result is FeatureAccess.Locked)
        assertEquals(Plan.PLUS, (result as FeatureAccess.Locked).requiredPlan)
    }

    @Test
    fun `PLUS plan user accessing MAX feature returns Locked MAX even with sufficient coins`() = runTest {
        fakeRepo.mutableState.value = AccessState(
            plan = Plan.PLUS,
            features = PlanAccessMap.featuresFor(Plan.PLUS),
            quotas = emptyMap(),
            expiresAt = null,
            lastSyncedAt = Clock.System.now(),
            coinBalance = 100
        )

        val result = useCase(FeatureKey.VideoInterview).first()

        assertTrue(result is FeatureAccess.Locked)
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

    // ── Scenario 3: Quota exhausted with coinCost on quota > 0 ──────────

    @Test
    fun `quota exhausted with positive quota coinCost and insufficient coins returns CoinTopUpRequired`() = runTest {
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

    @Test
    fun `quota exhausted with positive quota coinCost and sufficient coins returns Granted`() = runTest {
        val quota = FeatureQuota(
            feature = FeatureKey.MockInterviews,
            remaining = 0,
            max = 10,
            coinCost = 20
        )
        fakeRepo.mutableState.value = AccessState(
            plan = Plan.PLUS,
            features = PlanAccessMap.featuresFor(Plan.PLUS),
            quotas = mapOf(FeatureKey.MockInterviews to quota),
            expiresAt = null,
            lastSyncedAt = Clock.System.now(),
            coinBalance = 50
        )

        val result = useCase(FeatureKey.MockInterviews).first()

        assertTrue(result is FeatureAccess.Granted)
        assertEquals(quota, (result as FeatureAccess.Granted).quota)
    }

    // ── Scenario 4: Quota exhausted with null coinCost falls back to FeaturePricingMap ──

    @Test
    fun `quota exhausted with null coinCost falls back to FeaturePricingMap and returns CoinTopUpRequired when coins insufficient`() = runTest {
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
            coinBalance = 2 // MockInterviews costs 10 coins in FeaturePricingMap
        )

        val result = useCase(FeatureKey.MockInterviews).first()

        assertTrue(result is FeatureAccess.CoinTopUpRequired)
        val topUp = result as FeatureAccess.CoinTopUpRequired
        assertEquals(FeatureKey.MockInterviews, topUp.feature)
        assertEquals(10, topUp.coinCost)
        assertEquals(2, topUp.currentCoins)
    }

    @Test
    fun `quota exhausted with null coinCost falls back to FeaturePricingMap and returns Granted when coins sufficient`() = runTest {
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
            coinBalance = 10 // MockInterviews costs 10 coins in FeaturePricingMap
        )

        val result = useCase(FeatureKey.MockInterviews).first()

        assertTrue(result is FeatureAccess.Granted)
        assertEquals(quota, (result as FeatureAccess.Granted).quota)
    }

    // ── Scenario 5: Quota exhausted with zero coinCost and zero pricing map → Locked ─────

    @Test
    fun `quota exhausted with zero coinCost and zero pricing map returns Locked`() = runTest {
        val quota = FeatureQuota(
            feature = FeatureKey.ExportPdfReport,
            remaining = 0,
            max = 10,
            coinCost = 0
        )
        fakeRepo.mutableState.value = AccessState(
            plan = Plan.PLUS,
            features = PlanAccessMap.featuresFor(Plan.PLUS),
            quotas = mapOf(FeatureKey.ExportPdfReport to quota),
            expiresAt = null,
            lastSyncedAt = Clock.System.now(),
            coinBalance = 100
        )

        val result = useCase(FeatureKey.ExportPdfReport).first()

        assertTrue(result is FeatureAccess.Locked)
        assertEquals(Plan.PLUS, (result as FeatureAccess.Locked).requiredPlan)
    }

    // ── Scenario 6: Feature in plan with no quota object (pay-per-use or unlimited) ──

    @Test
    fun `FREE plan user with sufficient coins accessing VoicePracticeMode returns Granted`() = runTest {
        // VoicePracticeMode is in FREE plan, costs 10 coins in FeaturePricingMap
        fakeRepo.mutableState.value = AccessState(
            plan = Plan.FREE,
            features = PlanAccessMap.featuresFor(Plan.FREE),
            quotas = emptyMap(),
            expiresAt = null,
            lastSyncedAt = Clock.System.now(),
            coinBalance = 10
        )

        val result = useCase(FeatureKey.VoicePracticeMode).first()

        assertTrue(result is FeatureAccess.Granted)
        assertEquals(null, (result as FeatureAccess.Granted).quota)
    }

    @Test
    fun `FREE plan user with insufficient coins accessing VoicePracticeMode returns CoinTopUpRequired`() = runTest {
        fakeRepo.mutableState.value = AccessState(
            plan = Plan.FREE,
            features = PlanAccessMap.featuresFor(Plan.FREE),
            quotas = emptyMap(),
            expiresAt = null,
            lastSyncedAt = Clock.System.now(),
            coinBalance = 5
        )

        val result = useCase(FeatureKey.VoicePracticeMode).first()

        assertTrue(result is FeatureAccess.CoinTopUpRequired)
        val topUp = result as FeatureAccess.CoinTopUpRequired
        assertEquals(FeatureKey.VoicePracticeMode, topUp.feature)
        assertEquals(10, topUp.coinCost)
        assertEquals(5, topUp.currentCoins)
    }

    @Test
    fun `feature in plan with no quota object returns CoinTopUpRequired when coins insufficient`() = runTest {
        fakeRepo.mutableState.value = AccessState(
            plan = Plan.MAX,
            features = PlanAccessMap.featuresFor(Plan.MAX),
            quotas = emptyMap(),
            expiresAt = null,
            lastSyncedAt = Clock.System.now(),
            coinBalance = 4 // VoicePracticeMode costs 10 coins in FeaturePricingMap
        )

        val result = useCase(FeatureKey.VoicePracticeMode).first()

        assertTrue(result is FeatureAccess.CoinTopUpRequired)
        val topUp = result as FeatureAccess.CoinTopUpRequired
        assertEquals(FeatureKey.VoicePracticeMode, topUp.feature)
        assertEquals(10, topUp.coinCost)
        assertEquals(4, topUp.currentCoins)
    }

    @Test
    fun `feature in plan with no quota object returns Granted when coins sufficient`() = runTest {
        fakeRepo.mutableState.value = AccessState(
            plan = Plan.MAX,
            features = PlanAccessMap.featuresFor(Plan.MAX),
            quotas = emptyMap(),
            expiresAt = null,
            lastSyncedAt = Clock.System.now(),
            coinBalance = 15 // VoicePracticeMode costs 10 coins in FeaturePricingMap
        )

        val result = useCase(FeatureKey.VoicePracticeMode).first()

        assertTrue(result is FeatureAccess.Granted)
    }

    @Test
    fun `feature in plan with zero coin cost and no quota returns Granted`() = runTest {
        fakeRepo.mutableState.value = AccessState(
            plan = Plan.PLUS,
            features = PlanAccessMap.featuresFor(Plan.PLUS),
            quotas = emptyMap(),
            expiresAt = null,
            lastSyncedAt = Clock.System.now(),
            coinBalance = 0
        )

        val result = useCase(FeatureKey.ExportPdfReport).first()

        assertTrue(result is FeatureAccess.Granted)
    }

    // ── Scenario 7: Cache stale (>24h) → StaleCacheBlocked ─────────────

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

    // ── Scenario 8: Challenge Features Access ───────────────────────────

    @Test
    fun `FREE plan user accessing EnterChallenge returns Granted`() = runTest {
        fakeRepo.mutableState.value = AccessState(
            plan = Plan.FREE,
            features = PlanAccessMap.featuresFor(Plan.FREE),
            quotas = emptyMap(),
            expiresAt = null,
            lastSyncedAt = Clock.System.now(),
            coinBalance = 0
        )

        val result = useCase(FeatureKey.EnterChallenge).first()

        assertTrue(result is FeatureAccess.Granted)
    }

    @Test
    fun `FREE plan user accessing CreateChallenge returns Locked MAX`() = runTest {
        fakeRepo.mutableState.value = AccessState(
            plan = Plan.FREE,
            features = PlanAccessMap.featuresFor(Plan.FREE),
            quotas = emptyMap(),
            expiresAt = null,
            lastSyncedAt = Clock.System.now(),
            coinBalance = 100
        )

        val result = useCase(FeatureKey.CreateChallenge).first()

        assertTrue(result is FeatureAccess.Locked)
        assertEquals(Plan.MAX, (result as FeatureAccess.Locked).requiredPlan)
    }

    @Test
    fun `PLUS plan user accessing CreateChallenge returns Locked MAX`() = runTest {
        fakeRepo.mutableState.value = AccessState(
            plan = Plan.PLUS,
            features = PlanAccessMap.featuresFor(Plan.PLUS),
            quotas = emptyMap(),
            expiresAt = null,
            lastSyncedAt = Clock.System.now(),
            coinBalance = 100
        )

        val result = useCase(FeatureKey.CreateChallenge).first()

        assertTrue(result is FeatureAccess.Locked)
        assertEquals(Plan.MAX, (result as FeatureAccess.Locked).requiredPlan)
    }

    @Test
    fun `MAX plan user with insufficient coins accessing CreateChallenge returns CoinTopUpRequired`() = runTest {
        fakeRepo.mutableState.value = AccessState(
            plan = Plan.MAX,
            features = PlanAccessMap.featuresFor(Plan.MAX),
            quotas = emptyMap(),
            expiresAt = null,
            lastSyncedAt = Clock.System.now(),
            coinBalance = 0 // CreateChallenge costs 10 coins in FeaturePricingMap
        )

        val result = useCase(FeatureKey.CreateChallenge).first()

        assertTrue(result is FeatureAccess.CoinTopUpRequired)
        val topUp = result as FeatureAccess.CoinTopUpRequired
        assertEquals(FeatureKey.CreateChallenge, topUp.feature)
        assertEquals(10, topUp.coinCost)
        assertEquals(0, topUp.currentCoins)
    }

    @Test
    fun `MAX plan user with sufficient coins accessing CreateChallenge returns Granted`() = runTest {
        fakeRepo.mutableState.value = AccessState(
            plan = Plan.MAX,
            features = PlanAccessMap.featuresFor(Plan.MAX),
            quotas = emptyMap(),
            expiresAt = null,
            lastSyncedAt = Clock.System.now(),
            coinBalance = 10
        )

        val result = useCase(FeatureKey.CreateChallenge).first()

        assertTrue(result is FeatureAccess.Granted)
    }
}
