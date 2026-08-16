package com.iti.careerpilot.ats.presentation.entry

import android.net.Uri
import com.iti.careerpilot.ats.domain.model.AtsScore
import com.iti.careerpilot.ats.domain.model.AiJob
import com.iti.careerpilot.ats.domain.model.CoverLetter
import com.iti.careerpilot.ats.domain.model.JobListing
import com.iti.careerpilot.ats.domain.model.JobWorkspace
import com.iti.careerpilot.ats.domain.repository.AtsRepository
import com.iti.careerpilot.ats.domain.usecase.ImportJobUseCase
import com.iti.careerpilot.ats.domain.usecase.ObserveCurrentProfileUseCase
import com.iti.careerpilot.ats.domain.usecase.ReplaceCurrentCvUseCase
import com.iti.careerpilot.ats.presentation.entry.state.AtsEntryAction
import com.iti.careerpilot.ats.presentation.entry.viewmodel.AtsEntryViewModel
import com.iti.careerpilot.core.access.domain.usecase.CheckFeatureAccessUseCase
import com.iti.careerpilot.core.access.domain.usecase.RefreshAccessUseCase
import com.iti.careerpilot.core.access.testing.FakeAccessRepository
import com.iti.common.error.NetworkError
import com.iti.common.error.StorageError
import com.iti.common.media.pdfpicker.PdfOperations
import com.iti.common.result.CareerPilotResult
import com.iti.core.datastore.models.CvInfo
import com.iti.core.datastore.models.UserProfile
import com.iti.core.model.AccessState
import com.iti.core.model.FeatureKey
import com.iti.core.model.FeatureQuota
import com.iti.core.model.PdfFile
import com.iti.core.model.PdfFileMetadata
import com.iti.core.model.Plan
import kotlin.time.Clock
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
        viewModel.onAction(AtsEntryAction.Initial)
        runCurrent()

        viewModel.onAction(
            AtsEntryAction.JobUrlChanged("https://www.linkedin.com/jobs/view/123456789"),
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

        viewModel.onAction(
            AtsEntryAction.SharedTextReceived(
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
        viewModel.onAction(AtsEntryAction.Initial)
        runCurrent()
        viewModel.onAction(
            AtsEntryAction.JobUrlChanged("https://www.linkedin.com/jobs/view/123456789"),
        )

        viewModel.onAction(AtsEntryAction.CompareClicked)
        viewModel.onAction(AtsEntryAction.CompareClicked)
        runCurrent()

        assertEquals(1, repository.importCount)
        repository.holdImport?.complete(Unit)
        advanceUntilIdle()
    }

    // ----- gate path tests -----

    /**
     * JobParse costs 1 coin. With 0 coins and no plan access (FREE plan without JobParse feature),
     * the access result is CoinTopUpRequired — never Locked — since cost > 0 always takes coin path.
     * showCoinTopUpSheet is set and import is blocked.
     */
    @Test
    fun `compare when CoinTopUpRequired shows coin top-up sheet and blocks import`() = runTest(dispatcher) {
        val repository = FakeAtsRepository().apply {
            userProfile.value = UserProfile(cv = CvInfo(cvUrl = "https://cdn.example.com/cv.pdf"))
        }
        // JobParse costs 1 coin; user has 0 → CoinTopUpRequired
        val accessRepo = createAccessRepository(coins = 0, features = emptySet(), plan = Plan.FREE)
        val viewModel = createViewModel(repository, accessRepo)
        viewModel.onAction(AtsEntryAction.Initial)
        runCurrent()
        viewModel.onAction(
            AtsEntryAction.JobUrlChanged("https://www.linkedin.com/jobs/view/123456789"),
        )

        viewModel.onAction(AtsEntryAction.CompareClicked)
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
        viewModel.onAction(AtsEntryAction.Initial)
        runCurrent()
        viewModel.onAction(
            AtsEntryAction.JobUrlChanged("https://www.linkedin.com/jobs/view/123456789"),
        )

        viewModel.onAction(AtsEntryAction.CompareClicked)
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
        viewModel.onAction(AtsEntryAction.Initial)
        runCurrent()
        viewModel.onAction(
            AtsEntryAction.JobUrlChanged("https://www.linkedin.com/jobs/view/123456789"),
        )

        viewModel.onAction(AtsEntryAction.CompareClicked)
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
        val accessRepo = createAccessRepository(coins = 0, features = emptySet(), plan = Plan.FREE)
        val viewModel = createViewModel(repository, accessRepo)
        viewModel.onAction(AtsEntryAction.Initial)
        runCurrent()
        viewModel.onAction(
            AtsEntryAction.JobUrlChanged("https://www.linkedin.com/jobs/view/123456789"),
        )
        viewModel.onAction(AtsEntryAction.CompareClicked)
        advanceUntilIdle()
        assertTrue(viewModel.state.value.showCoinTopUpSheet)

        viewModel.onAction(AtsEntryAction.DismissCoinTopUpSheet)
        assertFalse(viewModel.state.value.showCoinTopUpSheet)
    }

    @Test
    fun `DismissGateSheet clears gate sheet flag`() = runTest(dispatcher) {
        val viewModel = createViewModel(FakeAtsRepository())

        // DismissGateSheet is safe to call even when showGateSheet is already false
        viewModel.onAction(AtsEntryAction.DismissGateSheet)
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
        repository: FakeAtsRepository,
        accessRepo: FakeAccessRepository = createAccessRepository(
            coins = 10,
            features = setOf(FeatureKey.JobParse),
        ),
    ) = AtsEntryViewModel(
        observeCurrentProfile = ObserveCurrentProfileUseCase(repository),
        replaceCurrentCv = ReplaceCurrentCvUseCase(repository),
        importJob = ImportJobUseCase(repository),
        pdfOperations = FakePdfOperations,
        checkFeatureAccess = CheckFeatureAccessUseCase(accessRepo),
        refreshAccess = RefreshAccessUseCase(accessRepo),
    )
}

private class FakeAtsRepository : AtsRepository {
    override val userProfile = MutableStateFlow(UserProfile())
    var importCount = 0
    var holdImport: CompletableDeferred<Unit>? = null

    override suspend fun replaceCurrentCv(
        file: PdfFile,
        onProgress: (Int) -> Unit,
    ): CareerPilotResult<Unit, NetworkError> {
        onProgress(100)
        userProfile.value = userProfile.value.copy(
            cv = CvInfo(
                cvUrl = "https://cdn.example.com/${file.name}",
                cvFileName = file.name,
                cvSizeBytes = file.sizeBytes,
            ),
        )
        return CareerPilotResult.Success(Unit)
    }

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

private object FakePdfOperations : PdfOperations {
    override suspend fun readPdf(uri: String) = CareerPilotResult.Success(
        PdfFile("resume.pdf", "application/pdf", 3, byteArrayOf(1, 2, 3)),
    )
    override suspend fun getPdfMetaData(uri: Uri): CareerPilotResult<PdfFileMetadata, StorageError> =
        CareerPilotResult.Error(StorageError.UNKNOWN)
    override suspend fun storePdfInternally(file: PdfFile): CareerPilotResult<String, StorageError> =
        CareerPilotResult.Success("file:///cv/${file.name}")
}
