package com.iti.careerpilot.challenges.presentation.viewmodel

import com.iti.careerpilot.challengefirestore.Challenge
import com.iti.careerpilot.challengefirestore.ChallengeType
import com.iti.careerpilot.challengefirestore.ChallengeVisibility
import com.iti.careerpilot.challengefirestore.SeniorityLevel
import com.iti.careerpilot.challenges.domain.repository.ChallengesRepository
import com.iti.careerpilot.challenges.presentation.action.ChallengesAction
import com.iti.careerpilot.challenges.presentation.event.ChallengesEvent
import com.iti.careerpilot.core.access.PlanAccessMap
import com.iti.careerpilot.core.access.domain.usecase.CheckFeatureAccessUseCase
import com.iti.careerpilot.core.access.testing.FakeAccessRepository
import com.iti.common.error.FirebaseError
import com.iti.common.result.CareerPilotResult
import com.iti.core.model.AccessState
import com.iti.core.model.FeatureKey
import com.iti.core.model.FeatureQuota
import com.iti.core.model.Plan
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChallengesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val dummyChallenge = Challenge(
        id = "c1",
        creatorId = 123L,
        creatorUsername = "johndoe",
        creatorName = "John Doe",
        trackId = 1L,
        trackName = "Android Development",
        visibility = ChallengeVisibility.PUBLIC,
        seniorityLevel = SeniorityLevel.MID_LEVEL,
        creationDate = 1700000000L,
        invitationCode = "INVITE123",
        questions = emptyList(),
        type = ChallengeType.AUDIO_ONLY,
    )

    private class FakeChallengesRepo : ChallengesRepository {
        var publicChallengesResult: CareerPilotResult<List<Challenge>, FirebaseError> =
            CareerPilotResult.Success(emptyList())
        var checkExistsResult: CareerPilotResult<Boolean, FirebaseError> =
            CareerPilotResult.Success(true)

        var getPublicChallengesCallCount = 0
        var checkChallengeExistsCallCount = 0
        var lastCheckedChallengeId: String? = null

        override suspend fun getPublicChallenges(): CareerPilotResult<List<Challenge>, FirebaseError> {
            getPublicChallengesCallCount++
            return publicChallengesResult
        }

        override suspend fun checkChallengeExists(challengeId: String): CareerPilotResult<Boolean, FirebaseError> {
            checkChallengeExistsCallCount++
            lastCheckedChallengeId = challengeId
            return checkExistsResult
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        challengesRepo: FakeChallengesRepo = FakeChallengesRepo(),
        accessRepository: FakeAccessRepository = FakeAccessRepository(
            AccessState(
                plan = Plan.MAX,
                features = PlanAccessMap.featuresFor(Plan.MAX),
                quotas = emptyMap(),
                expiresAt = null,
                lastSyncedAt = Clock.System.now(),
                coinBalance = 50,
            )
        ),
    ): ChallengesViewModel {
        return ChallengesViewModel(
            repository = challengesRepo,
            checkFeatureAccess = CheckFeatureAccessUseCase(accessRepository),
        )
    }

    @Test
    fun `CreateChallengeClicked when user is on Plan MAX emits NavigateToCreateChallenge`() = runTest {
        val accessRepo = FakeAccessRepository(
            AccessState(
                plan = Plan.MAX,
                features = PlanAccessMap.featuresFor(Plan.MAX),
                quotas = emptyMap(),
                expiresAt = null,
                lastSyncedAt = Clock.System.now(),
                coinBalance = 50,
            )
        )
        val viewModel = createViewModel(accessRepository = accessRepo)
        val events = mutableListOf<ChallengesEvent>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(events)
        }

        viewModel.onAction(ChallengesAction.CreateChallengeClicked)
        testScheduler.advanceUntilIdle()

        assertEquals(1, events.size)
        assertTrue(events.first() is ChallengesEvent.NavigateToCreateChallenge)
        job.cancel()
    }

    @Test
    fun `CreateChallengeClicked when user is on Plan FREE emits ShowFeatureGate with requiredPlan MAX`() = runTest {
        val accessRepo = FakeAccessRepository(
            AccessState(
                plan = Plan.FREE,
                features = PlanAccessMap.featuresFor(Plan.FREE),
                quotas = emptyMap(),
                expiresAt = null,
                lastSyncedAt = Clock.System.now(),
                coinBalance = 0,
            )
        )
        val viewModel = createViewModel(accessRepository = accessRepo)
        val events = mutableListOf<ChallengesEvent>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(events)
        }

        viewModel.onAction(ChallengesAction.CreateChallengeClicked)
        testScheduler.advanceUntilIdle()

        assertEquals(1, events.size)
        val gateEvent = events.first() as? ChallengesEvent.ShowFeatureGate
        assertNotNull("Expected ShowFeatureGate event", gateEvent)
        assertEquals(Plan.MAX, gateEvent!!.requiredPlan)
        assertEquals(FeatureKey.CreateChallenge.displayName(), gateEvent.featureName)
        val expectedFeatures = PlanAccessMap.featuresFor(Plan.MAX).map { it.displayName() }
        assertEquals(expectedFeatures, gateEvent.planFeatures)
        job.cancel()
    }

    @Test
    fun `CreateChallengeClicked when user is on Plan PLUS emits ShowFeatureGate with requiredPlan MAX`() = runTest {
        val accessRepo = FakeAccessRepository(
            AccessState(
                plan = Plan.PLUS,
                features = PlanAccessMap.featuresFor(Plan.PLUS),
                quotas = emptyMap(),
                expiresAt = null,
                lastSyncedAt = Clock.System.now(),
                coinBalance = 0,
            )
        )
        val viewModel = createViewModel(accessRepository = accessRepo)
        val events = mutableListOf<ChallengesEvent>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(events)
        }

        viewModel.onAction(ChallengesAction.CreateChallengeClicked)
        testScheduler.advanceUntilIdle()

        assertEquals(1, events.size)
        val gateEvent = events.first() as? ChallengesEvent.ShowFeatureGate
        assertNotNull("Expected ShowFeatureGate event", gateEvent)
        assertEquals(Plan.MAX, gateEvent!!.requiredPlan)
        job.cancel()
    }

    @Test
    fun `CreateChallengeClicked when FeatureAccess is CoinTopUpRequired emits NavigateToPlansPaywall`() = runTest {
        val quota = FeatureQuota(
            feature = FeatureKey.CreateChallenge,
            remaining = 0,
            max = 5,
            coinCost = 10,
        )
        val accessRepo = FakeAccessRepository(
            AccessState(
                plan = Plan.MAX,
                features = PlanAccessMap.featuresFor(Plan.MAX),
                quotas = mapOf(FeatureKey.CreateChallenge to quota),
                expiresAt = null,
                lastSyncedAt = Clock.System.now(),
                coinBalance = 5, // less than cost 10
            )
        )
        val viewModel = createViewModel(accessRepository = accessRepo)
        val events = mutableListOf<ChallengesEvent>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(events)
        }

        viewModel.onAction(ChallengesAction.CreateChallengeClicked)
        testScheduler.advanceUntilIdle()

        assertEquals(1, events.size)
        assertTrue(events.first() is ChallengesEvent.NavigateToPlansPaywall)
        job.cancel()
    }

    @Test
    fun `CreateChallengeClicked when FeatureAccess is StaleCacheBlocked does not emit navigation`() = runTest {
        val accessRepo = FakeAccessRepository(
            AccessState(
                plan = Plan.MAX,
                features = PlanAccessMap.featuresFor(Plan.MAX),
                quotas = emptyMap(),
                expiresAt = null,
                lastSyncedAt = Clock.System.now() - 25.hours, // > 24h stale
                coinBalance = 50,
            )
        )
        val viewModel = createViewModel(accessRepository = accessRepo)
        val events = mutableListOf<ChallengesEvent>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(events)
        }

        viewModel.onAction(ChallengesAction.CreateChallengeClicked)
        testScheduler.advanceUntilIdle()

        assertTrue(events.isEmpty())
        job.cancel()
    }

    @Test
    fun `OnChallengeClicked emits NavigateToChallengeDetails directly for Free user without lock`() = runTest {
        val accessRepo = FakeAccessRepository(
            AccessState(
                plan = Plan.FREE,
                features = PlanAccessMap.featuresFor(Plan.FREE),
                quotas = emptyMap(),
                expiresAt = null,
                lastSyncedAt = Clock.System.now(),
                coinBalance = 0,
            )
        )
        val viewModel = createViewModel(accessRepository = accessRepo)
        val events = mutableListOf<ChallengesEvent>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(events)
        }

        viewModel.onAction(ChallengesAction.OnChallengeClicked("challenge_123"))
        testScheduler.advanceUntilIdle()

        assertEquals(1, events.size)
        val event = events.first() as? ChallengesEvent.NavigateToChallengeDetails
        assertNotNull(event)
        assertEquals("challenge_123", event!!.challengeId)
        job.cancel()
    }

    @Test
    fun `OnChallengeClicked emits NavigateToChallengeDetails directly for Max user`() = runTest {
        val viewModel = createViewModel()
        val events = mutableListOf<ChallengesEvent>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(events)
        }

        viewModel.onAction(ChallengesAction.OnChallengeClicked("challenge_456"))
        testScheduler.advanceUntilIdle()

        assertEquals(1, events.size)
        val event = events.first() as? ChallengesEvent.NavigateToChallengeDetails
        assertNotNull(event)
        assertEquals("challenge_456", event!!.challengeId)
        job.cancel()
    }

    @Test
    fun `SubmitPrivateCode when challenge exists navigates to challenge details for Free user`() = runTest {
        val accessRepo = FakeAccessRepository(AccessState.Free)
        val fakeRepo = FakeChallengesRepo().apply {
            checkExistsResult = CareerPilotResult.Success(true)
        }
        val viewModel = createViewModel(challengesRepo = fakeRepo, accessRepository = accessRepo)
        val events = mutableListOf<ChallengesEvent>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(events)
        }

        viewModel.onAction(ChallengesAction.TogglePrivateCodeDialog)
        viewModel.onAction(ChallengesAction.OnPrivateCodeChange("CODE123"))
        viewModel.onAction(ChallengesAction.SubmitPrivateCode)
        testScheduler.advanceUntilIdle()

        assertEquals(1, fakeRepo.checkChallengeExistsCallCount)
        assertEquals("CODE123", fakeRepo.lastCheckedChallengeId)
        assertFalse(viewModel.state.value.isPrivateCodeDialogOpen)
        assertEquals(1, events.size)
        val event = events.first() as? ChallengesEvent.NavigateToChallengeDetails
        assertNotNull(event)
        assertEquals("CODE123", event!!.challengeId)
        job.cancel()
    }

    @Test
    fun `SubmitPrivateCode when challenge does not exist does not navigate and keeps dialog open`() = runTest {
        val fakeRepo = FakeChallengesRepo().apply {
            checkExistsResult = CareerPilotResult.Success(false)
        }
        val viewModel = createViewModel(challengesRepo = fakeRepo)
        val events = mutableListOf<ChallengesEvent>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(events)
        }

        viewModel.onAction(ChallengesAction.TogglePrivateCodeDialog)
        viewModel.onAction(ChallengesAction.OnPrivateCodeChange("INVALID_CODE"))
        viewModel.onAction(ChallengesAction.SubmitPrivateCode)
        testScheduler.advanceUntilIdle()

        assertEquals(1, fakeRepo.checkChallengeExistsCallCount)
        assertTrue(viewModel.state.value.isPrivateCodeDialogOpen)
        assertTrue(events.isEmpty())
        job.cancel()
    }

    @Test
    fun `Initial action loads public challenges into state`() = runTest {
        val fakeRepo = FakeChallengesRepo().apply {
            publicChallengesResult = CareerPilotResult.Success(listOf(dummyChallenge))
        }
        val viewModel = createViewModel(challengesRepo = fakeRepo)

        viewModel.onAction(ChallengesAction.Initial)
        testScheduler.advanceUntilIdle()

        assertEquals(1, fakeRepo.getPublicChallengesCallCount)
        assertEquals(listOf(dummyChallenge), viewModel.state.value.publicChallenges)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `Refresh action re-fetches public challenges`() = runTest {
        val fakeRepo = FakeChallengesRepo().apply {
            publicChallengesResult = CareerPilotResult.Success(listOf(dummyChallenge))
        }
        val viewModel = createViewModel(challengesRepo = fakeRepo)

        viewModel.onAction(ChallengesAction.Initial)
        testScheduler.advanceUntilIdle()

        viewModel.onAction(ChallengesAction.Refresh)
        testScheduler.advanceUntilIdle()

        assertEquals(2, fakeRepo.getPublicChallengesCallCount)
        assertFalse(viewModel.state.value.isRefreshing)
    }

    @Test
    fun `OnSearchQueryChange updates query in state`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(ChallengesAction.OnSearchQueryChange("Kotlin"))
        assertEquals("Kotlin", viewModel.state.value.searchQuery)
    }

    @Test
    fun `TogglePrivateCodeDialog toggles dialog state and resets privateCode`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(ChallengesAction.OnPrivateCodeChange("temp"))
        viewModel.onAction(ChallengesAction.TogglePrivateCodeDialog)
        assertTrue(viewModel.state.value.isPrivateCodeDialogOpen)
        assertEquals("", viewModel.state.value.privateCode)

        viewModel.onAction(ChallengesAction.TogglePrivateCodeDialog)
        assertFalse(viewModel.state.value.isPrivateCodeDialogOpen)
    }

    @Test
    fun `ChallengeDashboardClicked emits NavigateToChallengeDashboard`() = runTest {
        val viewModel = createViewModel()
        val events = mutableListOf<ChallengesEvent>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(events)
        }

        viewModel.onAction(ChallengesAction.ChallengeDashboardClicked)
        testScheduler.advanceUntilIdle()

        assertEquals(1, events.size)
        assertTrue(events.first() is ChallengesEvent.NavigateToChallengeDashboard)
        job.cancel()
    }
}
