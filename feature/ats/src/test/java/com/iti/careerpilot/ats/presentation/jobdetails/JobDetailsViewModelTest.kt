package com.iti.careerpilot.ats.presentation.jobdetails

import com.iti.careerpilot.ats.domain.model.AtsScore
import com.iti.careerpilot.ats.domain.model.AiJob
import com.iti.careerpilot.ats.domain.model.CoverLetter
import com.iti.careerpilot.ats.domain.model.JobListing
import com.iti.careerpilot.ats.domain.model.JobWorkspace
import com.iti.careerpilot.ats.domain.repository.AtsRepository
import com.iti.careerpilot.ats.domain.usecase.GetWorkspaceUseCase
import com.iti.careerpilot.ats.presentation.jobdetails.state.JobDetailsEffect
import com.iti.careerpilot.ats.presentation.jobdetails.state.JobDetailsIntent
import com.iti.careerpilot.ats.presentation.jobdetails.viewmodel.JobDetailsViewModel
import com.iti.careerpilot.core.access.domain.usecase.CheckFeatureAccessUseCase
import com.iti.careerpilot.core.access.domain.usecase.RefreshAccessUseCase
import com.iti.careerpilot.core.access.testing.FakeAccessRepository
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import com.iti.core.datastore.models.UserProfile
import com.iti.core.model.AccessState
import com.iti.core.model.FeatureKey
import com.iti.core.model.PdfFile
import com.iti.core.model.Plan
import kotlin.time.Clock
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
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
class JobDetailsViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `job details loads workspace without scoring`() = runTest(dispatcher) {
        val repository = JobDetailsRepository()
        val accessRepo = createAccessRepository(coins = 10, features = setOf(FeatureKey.AtsFeatures))
        val viewModel = createViewModel(repository, accessRepo)

        viewModel.onIntent(JobDetailsIntent.Initial(1L))
        advanceUntilIdle()

        assertEquals(WORKSPACE, viewModel.state.value.workspace)
        assertEquals(1, repository.workspaceCalls)
        assertEquals(0, repository.scoreCalls)
    }

    @Test
    fun `start scoring with sufficient coins emits score route with workspace id`() = runTest(dispatcher) {
        val accessRepo = createAccessRepository(coins = 10, features = setOf(FeatureKey.AtsFeatures))
        val viewModel = createViewModel(JobDetailsRepository(), accessRepo)
        viewModel.onIntent(JobDetailsIntent.Initial(1L))
        advanceUntilIdle()
        val effect = async { viewModel.effects.first() }

        viewModel.onIntent(JobDetailsIntent.StartScoring)
        runCurrent()

        assertEquals(JobDetailsEffect.OpenScore(1L), effect.await())
    }

    @Test
    fun `start scoring with insufficient coins shows coin top up sheet and blocks navigation`() = runTest(dispatcher) {
        val accessRepo = createAccessRepository(coins = 0, features = setOf(FeatureKey.AtsFeatures))
        val viewModel = createViewModel(JobDetailsRepository(), accessRepo)
        viewModel.onIntent(JobDetailsIntent.Initial(1L))
        advanceUntilIdle()

        viewModel.onIntent(JobDetailsIntent.StartScoring)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.showCoinTopUpSheet)
        assertEquals(5, viewModel.state.value.coinTopUpRequiredCost)
    }

    @Test
    fun `start scoring with insufficient coins shows coin top-up sheet`() = runTest(dispatcher) {
        val accessRepo = createAccessRepository(coins = 0, features = emptySet(), plan = Plan.FREE)
        val viewModel = createViewModel(JobDetailsRepository(), accessRepo)
        viewModel.onIntent(JobDetailsIntent.Initial(1L))
        advanceUntilIdle()

        viewModel.onIntent(JobDetailsIntent.StartScoring)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.showCoinTopUpSheet)
        assertFalse(viewModel.state.value.showGateSheet)
        assertEquals(5, viewModel.state.value.coinTopUpRequiredCost)
    }

    @Test
    fun `start scoring with sufficient coins via coin fallback is granted`() = runTest(dispatcher) {
        val accessRepo = createAccessRepository(coins = 10, features = emptySet(), plan = Plan.FREE)
        val viewModel = createViewModel(JobDetailsRepository(), accessRepo)
        viewModel.onIntent(JobDetailsIntent.Initial(1L))
        advanceUntilIdle()
        val effect = async { viewModel.effects.first() }

        viewModel.onIntent(JobDetailsIntent.StartScoring)
        runCurrent()

        assertFalse(viewModel.state.value.showGateSheet)
        assertFalse(viewModel.state.value.showCoinTopUpSheet)
        assertEquals(JobDetailsEffect.OpenScore(1L), effect.await())
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
        repository: JobDetailsRepository,
        accessRepository: FakeAccessRepository,
    ) = JobDetailsViewModel(
        getWorkspace = GetWorkspaceUseCase(repository),
        checkFeatureAccess = CheckFeatureAccessUseCase(accessRepository),
        refreshAccess = RefreshAccessUseCase(accessRepository),
        accessRepository = accessRepository,
    )
}

private class JobDetailsRepository : AtsRepository {
    override val userProfile = MutableStateFlow(UserProfile())
    var workspaceCalls = 0
    var scoreCalls = 0

    override suspend fun replaceCurrentCv(file: PdfFile, onProgress: (Int) -> Unit) =
        CareerPilotResult.Success(Unit)
    override suspend fun importJob(url: String) = CareerPilotResult.Success(WORKSPACE)
    override suspend fun getWorkspace(workspaceId: Long): CareerPilotResult<JobWorkspace, NetworkError> {
        workspaceCalls++
        return CareerPilotResult.Success(WORKSPACE)
    }
    override suspend fun scoreCv(workspaceId: Long): CareerPilotResult<AtsScore, NetworkError> {
        scoreCalls++
        return CareerPilotResult.Error(NetworkError.UNKNOWN)
    }
    override suspend fun optimizeCv(workspaceId: Long): CareerPilotResult<AiJob, NetworkError> =
        CareerPilotResult.Error(NetworkError.UNKNOWN)
    override suspend fun getAiJob(jobId: Long): CareerPilotResult<AiJob, NetworkError> =
        CareerPilotResult.Error(NetworkError.UNKNOWN)
    override suspend fun generateCoverLetter(workspaceId: Long): CareerPilotResult<CoverLetter, NetworkError> =
        CareerPilotResult.Error(NetworkError.UNKNOWN)
}

private val WORKSPACE = JobWorkspace(
    id = 1L,
    job = JobListing(
        id = 2L,
        title = "Engineer",
        companyName = "CareerPilot",
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
    ),
    status = "IMPORTED",
    cvScore = null,
    cvScoreUpdatedAt = null,
    cvOptimizedText = null,
    coverLetterText = null,
    lastInterviewSessionId = null,
    createdAt = null,
    updatedAt = null,
)
