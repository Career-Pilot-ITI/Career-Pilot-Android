package com.iti.careerpilot.ats.presentation.optimizedcv

import com.iti.careerpilot.ats.domain.model.AiJob
import com.iti.careerpilot.ats.domain.model.AiJobStatus
import com.iti.careerpilot.ats.domain.model.AiJobType
import com.iti.careerpilot.ats.domain.model.AtsScore
import com.iti.careerpilot.ats.domain.model.CoverLetter
import com.iti.careerpilot.ats.domain.model.CvOptimization
import com.iti.careerpilot.ats.domain.model.CvOptimizationSection
import com.iti.careerpilot.ats.domain.model.CvSectionImprovement
import com.iti.careerpilot.ats.domain.model.JobWorkspace
import com.iti.careerpilot.ats.domain.repository.AtsRepository
import com.iti.careerpilot.ats.domain.usecase.GetAiJobUseCase
import com.iti.careerpilot.ats.presentation.optimizedcv.state.OptimizedCvIntent
import com.iti.careerpilot.ats.presentation.optimizedcv.viewmodel.OptimizedCvViewModel
import com.iti.careerpilot.core.access.domain.usecase.CheckFeatureAccessUseCase
import com.iti.careerpilot.core.access.domain.usecase.RefreshAccessUseCase
import com.iti.careerpilot.core.access.testing.FakeAccessRepository
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import com.iti.core.datastore.models.UserProfile
import com.iti.core.model.AccessState
import com.iti.core.model.FeatureKey
import com.iti.core.model.FeatureQuota
import com.iti.core.model.Plan
import kotlin.time.Clock
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
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
class OptimizedCvViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    // ----- existing happy-path tests -----

    @Test
    fun `completed job exposes section improvements`() = runTest(dispatcher) {
        val viewModel = createViewModel(
            repository = OptimizationRepository(COMPLETED_JOB),
            accessRepo = createAccessRepository(coins = 20, features = setOf(FeatureKey.CvAiAnalysis)),
        )

        viewModel.onIntent(OptimizedCvIntent.Initial(42L))
        advanceUntilIdle()

        assertEquals("Experience", viewModel.state.value.sections.single().name)
        assertEquals("Delivered 12 APIs.", viewModel.state.value.sections.single().improvements.single().improved)
    }

    @Test
    fun `pending job exposes a retryable not-ready message`() = runTest(dispatcher) {
        val viewModel = createViewModel(
            repository = OptimizationRepository(PENDING_JOB),
            accessRepo = createAccessRepository(coins = 20, features = setOf(FeatureKey.CvAiAnalysis)),
        )

        viewModel.onIntent(OptimizedCvIntent.Initial(42L))
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.error)
    }

    // ----- gate path tests -----

    @Test
    fun `access Locked for free tier user shows gate sheet and stops loading`() = runTest(dispatcher) {
        val viewModel = createViewModel(
            repository = OptimizationRepository(COMPLETED_JOB),
            accessRepo = createAccessRepository(coins = 0, features = emptySet(), plan = Plan.FREE),
        )

        viewModel.onIntent(OptimizedCvIntent.Initial(42L))
        advanceUntilIdle()

        assertTrue(viewModel.state.value.showGateSheet)
        assertEquals(Plan.PLUS, viewModel.state.value.gateRequiredPlan)
        assertFalse(viewModel.state.value.showCoinTopUpSheet)
        assertFalse(viewModel.state.value.isLoading)
        assertEquals(0, viewModel.state.value.sections.size)
    }

    @Test
    fun `access CoinTopUpRequired for plus tier user shows coin top-up sheet and marks hasInsufficientCoins`() = runTest(dispatcher) {
        // CvAiAnalysis costs 15 coins; give plus user 2 coins (< 15) → CoinTopUpRequired
        val viewModel = createViewModel(
            repository = OptimizationRepository(COMPLETED_JOB),
            accessRepo = createAccessRepository(coins = 2, features = setOf(FeatureKey.CvAiAnalysis), plan = Plan.PLUS),
        )

        viewModel.onIntent(OptimizedCvIntent.Initial(42L))
        advanceUntilIdle()

        assertTrue(viewModel.state.value.showCoinTopUpSheet)
        assertTrue(viewModel.state.value.hasInsufficientCoins)
        assertEquals(15, viewModel.state.value.coinTopUpRequiredCost)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `access StaleCacheBlocked calls refreshAccess and stops loading`() = runTest(dispatcher) {
        // lastSyncedAt = null → StaleCacheBlocked
        val accessRepo = FakeAccessRepository(initialState = AccessState.Free)
        val viewModel = createViewModel(
            repository = OptimizationRepository(COMPLETED_JOB),
            accessRepo = accessRepo,
        )

        viewModel.onIntent(OptimizedCvIntent.Initial(42L))
        advanceUntilIdle()

        assertEquals(1, accessRepo.refreshCount)
        assertFalse(viewModel.state.value.isLoading)
        // No getAiJob call was made
        assertEquals(0, viewModel.state.value.sections.size)
    }

    @Test
    fun `access Granted proceeds to load AI job result`() = runTest(dispatcher) {
        val viewModel = createViewModel(
            repository = OptimizationRepository(COMPLETED_JOB),
            accessRepo = createAccessRepository(coins = 20, features = setOf(FeatureKey.CvAiAnalysis)),
        )

        viewModel.onIntent(OptimizedCvIntent.Initial(42L))
        advanceUntilIdle()

        assertFalse(viewModel.state.value.showGateSheet)
        assertFalse(viewModel.state.value.showCoinTopUpSheet)
        assertEquals(1, viewModel.state.value.sections.size)
    }

    @Test
    fun `DismissCoinTopUpSheet clears coin top-up sheet flag`() = runTest(dispatcher) {
        val viewModel = createViewModel(
            repository = OptimizationRepository(COMPLETED_JOB),
            accessRepo = createAccessRepository(coins = 2, features = setOf(FeatureKey.CvAiAnalysis), plan = Plan.PLUS),
        )
        viewModel.onIntent(OptimizedCvIntent.Initial(42L))
        advanceUntilIdle()
        assertTrue(viewModel.state.value.showCoinTopUpSheet)

        viewModel.onIntent(OptimizedCvIntent.DismissCoinTopUpSheet)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.showCoinTopUpSheet)
    }

    @Test
    fun `DismissGateSheet clears gate sheet flag`() = runTest(dispatcher) {
        val viewModel = createViewModel(
            repository = OptimizationRepository(COMPLETED_JOB),
            accessRepo = createAccessRepository(coins = 0, features = emptySet(), plan = Plan.FREE),
        )
        viewModel.onIntent(OptimizedCvIntent.Initial(42L))
        advanceUntilIdle()
        assertTrue(viewModel.state.value.showGateSheet)

        viewModel.onIntent(OptimizedCvIntent.DismissGateSheet)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.showGateSheet)
    }

    // ----- helpers -----

    private fun createAccessRepository(
        coins: Int,
        features: Set<FeatureKey>,
        plan: Plan = Plan.PLUS,
        quotas: Map<FeatureKey, FeatureQuota> = emptyMap(),
    ): FakeAccessRepository {
        val accessState = AccessState(
            plan = plan,
            features = features,
            quotas = quotas,
            expiresAt = null,
            lastSyncedAt = Clock.System.now(),
            coinBalance = coins,
        )
        return FakeAccessRepository(initialState = accessState)
    }

    private fun createViewModel(
        repository: OptimizationRepository,
        accessRepo: FakeAccessRepository,
    ) = OptimizedCvViewModel(
        getAiJob = GetAiJobUseCase(repository),
        checkFeatureAccess = CheckFeatureAccessUseCase(accessRepo),
        refreshAccess = RefreshAccessUseCase(accessRepo),
    )
}

private class OptimizationRepository(
    private val job: AiJob,
) : AtsRepository {
    override val userProfile = MutableStateFlow(UserProfile())

    override suspend fun importJob(url: String): CareerPilotResult<JobWorkspace, NetworkError> =
        CareerPilotResult.Error(NetworkError.UNKNOWN)
    override suspend fun getWorkspace(workspaceId: Long): CareerPilotResult<JobWorkspace, NetworkError> =
        CareerPilotResult.Error(NetworkError.UNKNOWN)
    override suspend fun scoreCv(workspaceId: Long): CareerPilotResult<AtsScore, NetworkError> =
        CareerPilotResult.Error(NetworkError.UNKNOWN)
    override suspend fun optimizeCv(workspaceId: Long) = CareerPilotResult.Success(PENDING_JOB)
    override suspend fun getAiJob(jobId: Long) = CareerPilotResult.Success(job)
    override suspend fun generateCoverLetter(workspaceId: Long): CareerPilotResult<CoverLetter, NetworkError> =
        CareerPilotResult.Error(NetworkError.UNKNOWN)
}

private val PENDING_JOB = AiJob(
    id = 42L,
    workspaceId = 7L,
    type = AiJobType.CV_OPTIMIZE,
    status = AiJobStatus.PENDING,
    progressPercentage = 25,
    currentStep = "Processing",
    result = null,
    errorMessage = null,
    createdAt = null,
    startedAt = null,
    completedAt = null,
)

private val COMPLETED_JOB = PENDING_JOB.copy(
    status = AiJobStatus.COMPLETED,
    progressPercentage = 100,
    result = CvOptimization(
        sections = persistentListOf(
            CvOptimizationSection(
                name = "Experience",
                score = 82,
                improvements = persistentListOf(
                    CvSectionImprovement(
                        original = "Worked on APIs.",
                        improved = "Delivered 12 APIs.",
                        reason = "Adds measurable impact.",
                    ),
                ),
            ),
        ),
        recommendedTracks = persistentListOf("Backend Development"),
        coinCost = 50,
    ),
)
