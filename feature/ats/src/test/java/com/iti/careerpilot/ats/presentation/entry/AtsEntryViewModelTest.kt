package com.iti.careerpilot.ats.presentation.entry

import com.iti.careerpilot.ats.domain.model.AtsScore
import com.iti.careerpilot.ats.domain.model.AiJob
import com.iti.careerpilot.ats.domain.model.CoverLetter
import com.iti.careerpilot.ats.domain.model.JobListing
import com.iti.careerpilot.ats.domain.model.JobWorkspace
import com.iti.careerpilot.ats.domain.repository.AtsRepository
import com.iti.careerpilot.ats.domain.usecase.ImportJobUseCase
import com.iti.careerpilot.ats.domain.usecase.ObserveCurrentProfileUseCase
import com.iti.careerpilot.ats.presentation.entry.state.AtsEntryAction
import com.iti.careerpilot.ats.presentation.entry.state.AtsEntryEffect
import com.iti.careerpilot.ats.presentation.entry.viewmodel.AtsEntryViewModel
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import com.iti.core.datastore.models.CvInfo
import com.iti.core.datastore.models.UserProfile
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CompletableDeferred
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

    @Test
    fun `missing CV edit action navigates to edit profile`() = runTest(dispatcher) {
        val viewModel = createViewModel(FakeAtsRepository())
        val effect = async { viewModel.effects.first() }

        viewModel.onAction(AtsEntryAction.EditProfileClicked)
        runCurrent()

        assertEquals(AtsEntryEffect.NavigateToEditProfile, effect.await())
    }

    private fun createViewModel(repository: FakeAtsRepository) = AtsEntryViewModel(
        observeCurrentProfile = ObserveCurrentProfileUseCase(repository),
        importJob = ImportJobUseCase(repository),
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
