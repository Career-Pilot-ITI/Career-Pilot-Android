package com.iti.careerpilot.ats.presentation.entry

import android.net.Uri
import com.iti.careerpilot.ats.domain.model.AtsScore
import com.iti.careerpilot.ats.domain.model.CoverLetter
import com.iti.careerpilot.ats.domain.model.CvOptimization
import com.iti.careerpilot.ats.domain.model.JobListing
import com.iti.careerpilot.ats.domain.model.JobWorkspace
import com.iti.careerpilot.ats.domain.repository.AtsRepository
import com.iti.careerpilot.ats.domain.usecase.ImportJobUseCase
import com.iti.careerpilot.ats.domain.usecase.ObserveCurrentProfileUseCase
import com.iti.careerpilot.ats.domain.usecase.ReplaceCurrentCvUseCase
import com.iti.careerpilot.ats.presentation.entry.state.AtsEntryAction
import com.iti.careerpilot.ats.presentation.entry.viewmodel.AtsEntryViewModel
import com.iti.common.error.NetworkError
import com.iti.common.error.StorageError
import com.iti.common.media.pdfpicker.PdfOperations
import com.iti.common.result.CareerPilotResult
import com.iti.core.datastore.models.CvInfo
import com.iti.core.datastore.models.UserProfile
import com.iti.core.model.PdfFile
import com.iti.core.model.PdfFileMetadata
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

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

    @Test
    fun `compare requires valid URL and backend synchronized CV`() = runTest(dispatcher) {
        val repository = FakeAtsRepository()
        val viewModel = createViewModel(repository)
        viewModel.onAction(AtsEntryAction.Initial)
        runCurrent()

        viewModel.onAction(AtsEntryAction.JobUrlChanged("https://jobs.example.com/42"))
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
            AtsEntryAction.SharedTextReceived("Apply: https://jobs.example.com/42?source=share"),
        )
        runCurrent()

        assertEquals("https://jobs.example.com/42?source=share", viewModel.state.value.jobUrl)
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
        viewModel.onAction(AtsEntryAction.JobUrlChanged("https://jobs.example.com/42"))

        viewModel.onAction(AtsEntryAction.CompareClicked)
        viewModel.onAction(AtsEntryAction.CompareClicked)
        runCurrent()

        assertEquals(1, repository.importCount)
        repository.holdImport?.complete(Unit)
        advanceUntilIdle()
    }

    private fun createViewModel(repository: FakeAtsRepository) = AtsEntryViewModel(
        observeCurrentProfile = ObserveCurrentProfileUseCase(repository),
        replaceCurrentCv = ReplaceCurrentCvUseCase(repository),
        importJob = ImportJobUseCase(repository),
        pdfOperations = FakePdfOperations,
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
    override suspend fun optimizeCv(workspaceId: Long): CareerPilotResult<CvOptimization, NetworkError> =
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
                requiredSkills = emptyList(),
                preferredSkills = emptyList(),
                responsibilities = emptyList(),
                qualifications = emptyList(),
                technologies = emptyList(),
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
