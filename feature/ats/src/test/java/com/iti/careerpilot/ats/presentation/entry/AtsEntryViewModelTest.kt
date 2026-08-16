package com.iti.careerpilot.ats.presentation.entry

import com.iti.careerpilot.ats.domain.model.AtsScore
import com.iti.careerpilot.ats.domain.model.AiJob
import com.iti.careerpilot.ats.domain.model.CoverLetter
import com.iti.careerpilot.ats.domain.model.JobListing
import com.iti.careerpilot.ats.domain.model.JobWorkspace
import com.iti.careerpilot.ats.domain.repository.AtsRepository
import com.iti.careerpilot.ats.domain.usecase.ImportJobUseCase
import com.iti.careerpilot.ats.domain.usecase.ObserveCurrentProfileUseCase
import com.iti.careerpilot.ats.presentation.entry.state.AtsEntryEffect
import com.iti.careerpilot.ats.presentation.entry.state.AtsEntryIntent
import com.iti.careerpilot.ats.presentation.entry.viewmodel.AtsEntryViewModel
import com.iti.careerpilot.core.access.domain.usecase.CheckFeatureAccessUseCase
import com.iti.careerpilot.core.access.domain.usecase.RefreshAccessUseCase
import com.iti.careerpilot.core.access.testing.FakeAccessRepository
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import com.iti.core.datastore.models.CvInfo
import com.iti.core.datastore.models.UserProfile
import com.iti.core.model.AccessState
import com.iti.core.model.FeatureKey
import com.iti.core.model.FeatureQuota
import com.iti.core.model.Plan
import kotlin.time.Clock
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
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
class AtsEntryViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ----- existing tests -----

    @Test
    fun `compare requires valid URL and backend synchronized CV`() = runTest(dispatcher) {
        val repository = FakeAtsRepository()
        val viewModel = createViewModel(repository)
        viewModel.onIntent(AtsEntryIntent.Initial)
        runCurrent()

        viewModel.onIntent(
            AtsEntryIntent.JobUrlChanged("https://www.linkedin.com/jobs/view/123456789"),
        )
        assertFalse(viewModel.state.value.canCompare)

        repository.userProfile.value = UserProfile(
            cv = CvInfo(cvUrl = "https://cdn.example.com/cv.pdf", cvFileName = "resume.pdf"),
        )
        runCurrent()

        assertTrue(viewModel.state.value.canCompare)
    }

    @Test
    fun `shared URL prefills but never imports automatically`() = runTest(dispatcher) {
        val repository = FakeAtsRepository()
        val viewModel = createViewModel(repository)

        viewModel.onIntent(
            AtsEntryIntent.SharedTextReceived(
                "Apply: https://www.linkedin.com/jobs/view/123456789?source=share",
            ),
        )
        runCurrent()

        assertEquals(
            "https://www.linkedin.com/jobs/view/123456789?source=share",
            viewModel.state.value.jobUrl,
        )
        assertEquals(0, repository.importCount)
    }

    @Test
    fun `duplicate compare taps start one import`() = runTest(dispatcher) {
        val repository = FakeAtsRepository().apply {
            userProfile.value = UserProfile(cv = CvInfo(cvUrl = "https://cdn.example.com/cv.pdf"))
            holdImport = CompletableDeferred()
        }
        val viewModel = createViewModel(repository)
        viewModel.onIntent(AtsEntryIntent.Initial)
        runCurrent()
        viewModel.onIntent(
            AtsEntryIntent.JobUrlChanged("https://www.linkedin.com/jobs/view/123456789"),
        )

        viewModel.onIntent(AtsEntryIntent.CompareClicked)
        viewModel.onIntent(AtsEntryIntent.CompareClicked)
        runCurrent()

        assertEquals(1, repository.importCount)
        repository.holdImport?.complete(Unit)
        advanceUntilIdle()
    }

    @Test
    fun `EditProfileClicked emits NavigateToEditProfile effect`() = runTest(dispatcher) {
        val viewModel = createViewModel(FakeAtsRepository())
        var receivedEffect: AtsEntryEffect? = null
        val job = launch(dispatcher) {
            viewModel.effects.collect { receivedEffect = it }
        }
        runCurrent()

        viewModel.onIntent(AtsEntryIntent.EditProfileClicked)
        runCurrent()

        assertEquals(AtsEntryEffect.NavigateToEditProfile, receivedEffect)
        job.cancel()
    }

    // ----- gate path tests -----

    @Test
    fun `compare when Locked for free tier shows gate sheet and blocks import`() = runTest(dispatcher) {
        val repository = FakeAtsRepository().apply {
            userProfile.value = UserProfile(cv = CvInfo(cvUrl = "https://cdn.example.com/cv.pdf"))
        }
        val accessRepo = createAccessRepository(coins = 0, features = emptySet(), plan = Plan.FREE)
        val viewModel = createViewModel(repository, accessRepo)
        viewModel.onIntent(AtsEntryIntent.Initial)
        runCurrent()
        viewModel.onIntent(
            AtsEntryIntent.JobUrlChanged("https://www.linkedin.com/jobs/view/123456789"),
        )

        viewModel.onIntent(AtsEntryIntent.CompareClicked)
        advanceUntilIdle()

        assertEquals(0, repository.importCount)
        assertTrue(viewModel.state.value.showGateSheet)
        assertEquals(Plan.PLUS, viewModel.state.value.gateRequiredPlan)
        assertFalse(viewModel.state.value.showCoinTopUpSheet)
        assertFalse(viewModel.state.value.isImporting)
    }

    @Test
    fun `compare when CoinTopUpRequired for plus tier shows coin top-up sheet and blocks import`() = runTest(dispatcher) {
        val repository = FakeAtsRepository().apply {
            userProfile.value = UserProfile(cv = CvInfo(cvUrl = "https://cdn.example.com/cv.pdf"))
        }
        val accessRepo = createAccessRepository(coins = 0, features = setOf(FeatureKey.JobParse), plan = Plan.PLUS)
        val viewModel = createViewModel(repository, accessRepo)
        viewModel.onIntent(AtsEntryIntent.Initial)
        runCurrent()
        viewModel.onIntent(
            AtsEntryIntent.JobUrlChanged("https://www.linkedin.com/jobs/view/123456789"),
        )

        viewModel.onIntent(AtsEntryIntent.CompareClicked)
        advanceUntilIdle()

        assertEquals(0, repository.importCount)
        assertTrue(viewModel.state.value.showCoinTopUpSheet)
        assertTrue(viewModel.state.value.hasInsufficientCoins)
        assertEquals(1, viewModel.state.value.coinTopUpRequiredCost)
        assertFalse(viewModel.state.value.isImporting)
    }

    @Test
    fun `compare when StaleCacheBlocked calls refreshAccess and blocks import`() = runTest(dispatcher) {
        val repository = FakeAtsRepository().apply {
            userProfile.value = UserProfile(cv = CvInfo(cvUrl = "https://cdn.example.com/cv.pdf"))
        }
        // lastSyncedAt = null → StaleCacheBlocked
        val accessRepo = FakeAccessRepository(initialState = AccessState.Free)
        val viewModel = createViewModel(repository, accessRepo)
        viewModel.onIntent(AtsEntryIntent.Initial)
        runCurrent()
        viewModel.onIntent(
            AtsEntryIntent.JobUrlChanged("https://www.linkedin.com/jobs/view/123456789"),
        )

        viewModel.onIntent(AtsEntryIntent.CompareClicked)
        advanceUntilIdle()

        assertEquals(1, accessRepo.refreshCount)
        assertEquals(0, repository.importCount)
    }

    @Test
    fun `compare when Granted proceeds with import`() = runTest(dispatcher) {
        val repository = FakeAtsRepository().apply {
            userProfile.value = UserProfile(cv = CvInfo(cvUrl = "https://cdn.example.com/cv.pdf"))
        }
        val accessRepo = createAccessRepository(coins = 10, features = setOf(FeatureKey.JobParse))
        val viewModel = createViewModel(repository, accessRepo)
        viewModel.onIntent(AtsEntryIntent.Initial)
        runCurrent()
        viewModel.onIntent(
            AtsEntryIntent.JobUrlChanged("https://www.linkedin.com/jobs/view/123456789"),
        )

        viewModel.onIntent(AtsEntryIntent.CompareClicked)
        advanceUntilIdle()

        assertEquals(1, repository.importCount)
        assertFalse(viewModel.state.value.showGateSheet)
        assertFalse(viewModel.state.value.showCoinTopUpSheet)
    }

    @Test
    fun `DismissCoinTopUpSheet clears coin top-up sheet flag`() = runTest(dispatcher) {
        val repository = FakeAtsRepository().apply {
            userProfile.value = UserProfile(cv = CvInfo(cvUrl = "https://cdn.example.com/cv.pdf"))
        }
        val accessRepo = createAccessRepository(coins = 0, features = setOf(FeatureKey.JobParse), plan = Plan.PLUS)
        val viewModel = createViewModel(repository, accessRepo)
        viewModel.onIntent(AtsEntryIntent.Initial)
        runCurrent()
        viewModel.onIntent(
            AtsEntryIntent.JobUrlChanged("https://www.linkedin.com/jobs/view/123456789"),
        )
        viewModel.onIntent(AtsEntryIntent.CompareClicked)
        advanceUntilIdle()
        assertTrue(viewModel.state.value.showCoinTopUpSheet)

        viewModel.onIntent(AtsEntryIntent.DismissCoinTopUpSheet)
        assertFalse(viewModel.state.value.showCoinTopUpSheet)
    }

    @Test
    fun `DismissGateSheet clears gate sheet flag`() = runTest(dispatcher) {
        val repository = FakeAtsRepository().apply {
            userProfile.value = UserProfile(cv = CvInfo(cvUrl = "https://cdn.example.com/cv.pdf"))
        }
        val accessRepo = createAccessRepository(coins = 0, features = emptySet(), plan = Plan.FREE)
        val viewModel = createViewModel(repository, accessRepo)
        viewModel.onIntent(AtsEntryIntent.Initial)
        runCurrent()
        viewModel.onIntent(
            AtsEntryIntent.JobUrlChanged("https://www.linkedin.com/jobs/view/123456789"),
        )
        viewModel.onIntent(AtsEntryIntent.CompareClicked)
        advanceUntilIdle()
        assertTrue(viewModel.state.value.showGateSheet)

        viewModel.onIntent(AtsEntryIntent.DismissGateSheet)
        assertFalse(viewModel.state.value.showGateSheet)
    }

    @Test
    fun `UpgradeFromGate clears gate sheet flag and emits NavigateToPaywall effect`() = runTest(dispatcher) {
        val repository = FakeAtsRepository().apply {
            userProfile.value = UserProfile(cv = CvInfo(cvUrl = "https://cdn.example.com/cv.pdf"))
        }
        val accessRepo = createAccessRepository(coins = 0, features = emptySet(), plan = Plan.FREE)
        val viewModel = createViewModel(repository, accessRepo)
        var receivedEffect: AtsEntryEffect? = null
        val job = launch(dispatcher) {
            viewModel.effects.collect { receivedEffect = it }
        }
        viewModel.onIntent(AtsEntryIntent.Initial)
        runCurrent()
        viewModel.onIntent(
            AtsEntryIntent.JobUrlChanged("https://www.linkedin.com/jobs/view/123456789"),
        )
        viewModel.onIntent(AtsEntryIntent.CompareClicked)
        advanceUntilIdle()
        assertTrue(viewModel.state.value.showGateSheet)

        viewModel.onIntent(AtsEntryIntent.UpgradeFromGate)
        runCurrent()

        assertFalse(viewModel.state.value.showGateSheet)
        assertEquals(AtsEntryEffect.NavigateToPaywall, receivedEffect)
        job.cancel()
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
        repository: FakeAtsRepository,
        accessRepo: FakeAccessRepository = createAccessRepository(
            coins = 10,
            features = setOf(FeatureKey.JobParse),
        ),
    ) = AtsEntryViewModel(
        observeCurrentProfile = ObserveCurrentProfileUseCase(repository),
        importJob = ImportJobUseCase(repository),
        checkFeatureAccess = CheckFeatureAccessUseCase(accessRepo),
        refreshAccess = RefreshAccessUseCase(accessRepo),
    )
}

private class FakeAtsRepository : AtsRepository {
    override val userProfile = MutableStateFlow(UserProfile())
    var importCount = 0
    var holdImport: CompletableDeferred<Unit>? = null

    override suspend fun importJob(url: String): CareerPilotResult<JobWorkspace, NetworkError> {
        importCount++
        holdImport?.await()
        return CareerPilotResult.Success(WORKSPACE)
    }

    override suspend fun getWorkspace(workspaceId: Long) = CareerPilotResult.Success(WORKSPACE)
    override suspend fun scoreCv(workspaceId: Long): CareerPilotResult<AtsScore, NetworkError> =
        CareerPilotResult.Error(NetworkError.UNKNOWN)
    override suspend fun optimizeCv(workspaceId: Long): CareerPilotResult<AiJob, NetworkError> =
        CareerPilotResult.Error(NetworkError.UNKNOWN)
    override suspend fun getAiJob(jobId: Long): CareerPilotResult<AiJob, NetworkError> =
        CareerPilotResult.Error(NetworkError.UNKNOWN)
    override suspend fun generateCoverLetter(workspaceId: Long): CareerPilotResult<CoverLetter, NetworkError> =
        CareerPilotResult.Error(NetworkError.UNKNOWN)

    private companion object {
        val WORKSPACE = JobWorkspace(
            id = 1L,
            job = JobListing(
                id = 2L,
                title = "Engineer",
                companyName = "Example",
                location = "Remote",
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
    }
}
