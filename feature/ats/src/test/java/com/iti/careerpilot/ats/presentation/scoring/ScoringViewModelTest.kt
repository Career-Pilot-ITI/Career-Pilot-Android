package com.iti.careerpilot.ats.presentation.scoring

import androidx.lifecycle.SavedStateHandle
import com.iti.careerpilot.ats.domain.model.AtsScore
import com.iti.careerpilot.ats.domain.model.AtsSectionScore
import com.iti.careerpilot.ats.domain.model.AiJob
import com.iti.careerpilot.ats.domain.model.AiJobStatus
import com.iti.careerpilot.ats.domain.model.AiJobType
import com.iti.careerpilot.ats.domain.model.CoverLetter
import com.iti.careerpilot.ats.domain.model.JobListing
import com.iti.careerpilot.ats.domain.model.JobWorkspace
import com.iti.careerpilot.ats.domain.repository.AtsRepository
import com.iti.careerpilot.ats.domain.usecase.GetWorkspaceUseCase
import com.iti.careerpilot.ats.domain.usecase.ObserveCurrentProfileUseCase
import com.iti.careerpilot.ats.domain.usecase.OptimizeCvUseCase
import com.iti.careerpilot.ats.domain.usecase.ScoreCvUseCase
import com.iti.careerpilot.ats.presentation.scoring.state.ScoringAction
import com.iti.careerpilot.ats.presentation.scoring.state.ScoringEffect
import com.iti.careerpilot.ats.presentation.scoring.viewmodel.ScoringViewModel
import com.iti.careerpilot.core.access.domain.usecase.CheckFeatureAccessUseCase
import com.iti.careerpilot.core.access.domain.usecase.RefreshAccessUseCase
import com.iti.careerpilot.core.access.testing.FakeAccessRepository
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import com.iti.core.datastore.models.UserProfile
import com.iti.core.datastore.models.CareerInfo
import com.iti.core.model.AccessState
import com.iti.core.model.FeatureKey
import com.iti.core.model.PdfFile
import com.iti.core.model.Plan
import kotlin.time.Clock
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ScoringViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `opening score with sufficient coins loads workspace and scores automatically`() = runTest(dispatcher) {
        val repository = ScoringRepository()
        val accessRepo = createAccessRepository(
            coins = 10,
            features = setOf(FeatureKey.AtsFeatures, FeatureKey.CvAiAnalysis),
        )
        val viewModel = createViewModel(repository, SavedStateHandle(), accessRepo)

        viewModel.onAction(ScoringAction.Initial(1L))
        advanceUntilIdle()

        assertEquals(1, repository.workspaceCalls)
        assertEquals(1, repository.scoreCalls)
        assertEquals(4, viewModel.state.value.score?.coinCost)
    }

    @Test
    fun `duplicate initial actions execute one paid score request`() = runTest(dispatcher) {
        val repository = ScoringRepository().apply { holdScore = CompletableDeferred() }
        val accessRepo = createAccessRepository(
            coins = 10,
            features = setOf(FeatureKey.AtsFeatures, FeatureKey.CvAiAnalysis),
        )
        val viewModel = createViewModel(repository, SavedStateHandle(), accessRepo)
        viewModel.onAction(ScoringAction.Initial(1L))
        viewModel.onAction(ScoringAction.Initial(1L))
        runCurrent()

        assertEquals(1, repository.scoreCalls)
        repository.holdScore?.complete(Unit)
        advanceUntilIdle()
        assertEquals(4, viewModel.state.value.score?.coinCost)
    }

    @Test
    fun `opening score with insufficient coins displays coin top up sheet and blocks score request`() = runTest(dispatcher) {
        val repository = ScoringRepository()
        val accessRepo = createAccessRepository(
            coins = 0,
            features = setOf(FeatureKey.AtsFeatures, FeatureKey.CvAiAnalysis),
        )
        val viewModel = createViewModel(repository, SavedStateHandle(), accessRepo)
        viewModel.onAction(ScoringAction.Initial(1L))
        advanceUntilIdle()

        assertEquals(0, repository.scoreCalls)
        assertTrue(viewModel.state.value.showCoinTopUpSheet)
        assertEquals(2, viewModel.state.value.coinTopUpRequiredCost)
    }

    @Test
    fun `start practice forwards workspace through readiness navigation`() = runTest(dispatcher) {
        val repository = ScoringRepository().apply {
            userProfile.value = UserProfile(
                career = CareerInfo(trackId = 5L, trackName = "Android"),
            )
        }
        val accessRepo = createAccessRepository(
            coins = 10,
            features = setOf(FeatureKey.AtsFeatures, FeatureKey.CvAiAnalysis),
        )
        val viewModel = createViewModel(repository, SavedStateHandle(), accessRepo)
        viewModel.onAction(ScoringAction.Initial(1L))
        advanceUntilIdle()
        val effect = async { viewModel.effects.first() }

        viewModel.onAction(ScoringAction.StartPractice)
        runCurrent()

        assertEquals(
            ScoringEffect.OpenPractice(
                trackId = 5L,
                trackName = "Android",
                workspaceId = 1L,
            ),
            effect.await(),
        )
    }

    @Test
    fun `optimize with sufficient coins starts background job and emits tracking effect`() = runTest(dispatcher) {
        val repository = ScoringRepository()
        val accessRepo = createAccessRepository(
            coins = 10,
            features = setOf(FeatureKey.AtsFeatures, FeatureKey.CvAiAnalysis),
        )
        val viewModel = createViewModel(repository, SavedStateHandle(), accessRepo)
        viewModel.onAction(ScoringAction.Initial(1L))
        advanceUntilIdle()
        val effect = async { viewModel.effects.first() }

        viewModel.onAction(ScoringAction.OptimizeCv)
        runCurrent()

        assertEquals(1, repository.optimizeCalls)
        assertEquals(ScoringEffect.StartOptimizationTracking(OPTIMIZATION_JOB), effect.await())
    }

    @Test
    fun `optimize with insufficient coins displays coin top up sheet and blocks optimize request`() = runTest(dispatcher) {
        val repository = ScoringRepository()
        val accessRepo = createAccessRepository(
            coins = 2,
            features = setOf(FeatureKey.AtsFeatures, FeatureKey.CvAiAnalysis),
        )
        val viewModel = createViewModel(repository, SavedStateHandle(), accessRepo)
        viewModel.onAction(ScoringAction.Initial(1L))
        advanceUntilIdle()

        viewModel.onAction(ScoringAction.OptimizeCv)
        advanceUntilIdle()

        assertEquals(0, repository.optimizeCalls)
        assertTrue(viewModel.state.value.showCoinTopUpSheet)
        assertEquals(5, viewModel.state.value.coinTopUpRequiredCost)
    }

    @Test
    fun `optimize with insufficient coins shows coin top up sheet`() = runTest(dispatcher) {
        val repository = ScoringRepository()
        val accessRepo = createAccessRepository(
            coins = 0,
            features = setOf(FeatureKey.AtsFeatures), // Missing CvAiAnalysis
            plan = Plan.FREE,
        )
        val viewModel = createViewModel(repository, SavedStateHandle(), accessRepo)
        viewModel.onAction(ScoringAction.Initial(1L))
        advanceUntilIdle()

        viewModel.onAction(ScoringAction.OptimizeCv)
        advanceUntilIdle()

        assertEquals(0, repository.optimizeCalls)
        assertTrue(viewModel.state.value.showCoinTopUpSheet)
        assertTrue(viewModel.state.value.hasInsufficientCoins)
        assertFalse(viewModel.state.value.showGateSheet)
        assertEquals(5, viewModel.state.value.coinTopUpRequiredCost)
    }

    @Test
    fun `optimize with sufficient coins via coin fallback is granted`() = runTest(dispatcher) {
        val repository = ScoringRepository()
        val accessRepo = createAccessRepository(
            coins = 10,
            features = setOf(FeatureKey.AtsFeatures), // Missing CvAiAnalysis, fallback to 5 coins
            plan = Plan.FREE,
        )
        val viewModel = createViewModel(repository, SavedStateHandle(), accessRepo)
        viewModel.onAction(ScoringAction.Initial(1L))
        advanceUntilIdle()
        val effect = async { viewModel.effects.first() }

        viewModel.onAction(ScoringAction.OptimizeCv)
        runCurrent()

        assertEquals(1, repository.optimizeCalls)
        assertFalse(viewModel.state.value.showGateSheet)
        assertFalse(viewModel.state.value.showCoinTopUpSheet)
        assertEquals(ScoringEffect.StartOptimizationTracking(OPTIMIZATION_JOB), effect.await())
    }

    private fun createAccessRepository(
        coins: Int,
        features: Set<FeatureKey>,
        plan: Plan = Plan.PLUS,
    ): FakeAccessRepository {
        val accessState = AccessState(
            plan = plan,
            features = features,
            quotas = emptyMap(),
            expiresAt = null,
            lastSyncedAt = Clock.System.now(),
            coinBalance = coins,
        )
        return FakeAccessRepository(initialState = accessState)
    }

    private fun createViewModel(
        repository: ScoringRepository,
        state: SavedStateHandle,
        accessRepository: FakeAccessRepository = createAccessRepository(10, setOf(FeatureKey.AtsFeatures, FeatureKey.CvAiAnalysis)),
    ) = ScoringViewModel(
        getWorkspace = GetWorkspaceUseCase(repository),
        scoreCv = ScoreCvUseCase(repository),
        observeCurrentProfile = ObserveCurrentProfileUseCase(repository),
        optimizeCv = OptimizeCvUseCase(repository),
        savedStateHandle = state,
        checkFeatureAccess = CheckFeatureAccessUseCase(accessRepository),
        refreshAccess = RefreshAccessUseCase(accessRepository),
        accessRepository = accessRepository,
    )
}

private class ScoringRepository : AtsRepository {
    override val userProfile = MutableStateFlow(UserProfile())
    var workspaceCalls = 0
    var scoreCalls = 0
    var optimizeCalls = 0
    var holdScore: CompletableDeferred<Unit>? = null
    var scoreResult: CareerPilotResult<AtsScore, NetworkError> = CareerPilotResult.Success(SCORE)

    override suspend fun replaceCurrentCv(file: PdfFile, onProgress: (Int) -> Unit) =
        CareerPilotResult.Success(Unit)
    override suspend fun importJob(url: String) = CareerPilotResult.Success(WORKSPACE)
    override suspend fun getWorkspace(workspaceId: Long): CareerPilotResult<JobWorkspace, NetworkError> {
        workspaceCalls++
        return CareerPilotResult.Success(WORKSPACE)
    }
    override suspend fun scoreCv(workspaceId: Long): CareerPilotResult<AtsScore, NetworkError> {
        scoreCalls++
        holdScore?.await()
        return scoreResult
    }
    override suspend fun optimizeCv(workspaceId: Long): CareerPilotResult<AiJob, NetworkError> {
        optimizeCalls++
        return CareerPilotResult.Success(OPTIMIZATION_JOB)
    }
    override suspend fun getAiJob(jobId: Long): CareerPilotResult<AiJob, NetworkError> =
        CareerPilotResult.Error(NetworkError.UNKNOWN)
    override suspend fun generateCoverLetter(workspaceId: Long): CareerPilotResult<CoverLetter, NetworkError> =
        CareerPilotResult.Error(NetworkError.UNKNOWN)

    private companion object {
        val JOB = JobListing(
            id = 2,
            title = "Engineer",
            companyName = "Example",
            location = "Cairo",
            description = "Description",
            employmentType = null,
            seniorityLevel = null,
            requiredSkills = persistentListOf(),
            preferredSkills = persistentListOf(),
            responsibilities = persistentListOf(),
            qualifications = persistentListOf(),
            technologies = persistentListOf(),
            salaryMin = null,
            salaryMax = null,
            currency = null,
            experienceYears = null,
            educationLevel = null,
            applicationUrl = null,
            sourceUrl = null,
            sourceType = null,
        )
        val WORKSPACE = JobWorkspace(
            id = 1,
            job = JOB,
            status = "IMPORTED",
            cvScore = null,
            cvScoreUpdatedAt = null,
            cvOptimizedText = null,
            coverLetterText = null,
            lastInterviewSessionId = null,
            createdAt = null,
            updatedAt = null,
        )
        val SCORE = AtsScore(
            overallScore = 78,
            matchPercentage = 78,
            matchedSkills = persistentListOf("Kotlin"),
            missingRequiredSkills = persistentListOf(),
            missingPreferredSkills = persistentListOf(),
            strengths = persistentListOf(),
            weaknesses = persistentListOf(),
            sections = persistentListOf(AtsSectionScore("Projects", 60, "Add metrics")),
            recommendations = persistentListOf(),
            coinCost = 4,
            cvScoreUpdatedAt = null,
        )
    }
}

private val OPTIMIZATION_JOB = AiJob(
    id = 42L,
    workspaceId = 1L,
    type = AiJobType.CV_OPTIMIZE,
    status = AiJobStatus.PENDING,
    progressPercentage = 0,
    currentStep = "Queued",
    result = null,
    errorMessage = null,
    createdAt = null,
    startedAt = null,
    completedAt = null,
)
