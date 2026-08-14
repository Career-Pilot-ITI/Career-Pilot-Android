package com.iti.careerpilot.ats.presentation.coverletter

import androidx.lifecycle.SavedStateHandle
import com.iti.careerpilot.ats.domain.model.AtsScore
import com.iti.careerpilot.ats.domain.model.AiJob
import com.iti.careerpilot.ats.domain.model.CoverLetter
import com.iti.careerpilot.ats.domain.model.JobListing
import com.iti.careerpilot.ats.domain.model.JobWorkspace
import com.iti.careerpilot.ats.domain.repository.AtsRepository
import com.iti.careerpilot.ats.domain.usecase.GenerateCoverLetterUseCase
import com.iti.careerpilot.ats.domain.usecase.GetWorkspaceUseCase
import com.iti.careerpilot.ats.domain.usecase.ObserveCurrentProfileUseCase
import com.iti.careerpilot.ats.presentation.coverletter.state.CoverLetterAction
import com.iti.careerpilot.ats.presentation.coverletter.viewmodel.CoverLetterViewModel
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import com.iti.core.datastore.models.UserProfile
import com.iti.core.model.PdfFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CoverLetterViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `opening cover letter generates immediately when workspace has no letter`() =
        runTest(dispatcher) {
            val repository = CoverLetterRepository(coverLetterText = null)
            val viewModel = createViewModel(repository)

            viewModel.onAction(CoverLetterAction.Initial(1L))
            advanceUntilIdle()

            assertEquals(1, repository.generateCalls)
            assertEquals("Generated letter", viewModel.state.value.editedValue)
        }

    @Test
    fun `opening cover letter restores cached value without generating again`() = runTest(dispatcher) {
        val repository = CoverLetterRepository(coverLetterText = "Cached letter")
        val viewModel = createViewModel(repository)

        viewModel.onAction(CoverLetterAction.Initial(1L))
        advanceUntilIdle()

        assertEquals(0, repository.generateCalls)
        assertEquals("Cached letter", viewModel.state.value.editedValue)
    }

    private fun createViewModel(repository: CoverLetterRepository) = CoverLetterViewModel(
        getWorkspace = GetWorkspaceUseCase(repository),
        generateCoverLetter = GenerateCoverLetterUseCase(repository),
        observeCurrentProfile = ObserveCurrentProfileUseCase(repository),
        savedStateHandle = SavedStateHandle(),
    )
}

private class CoverLetterRepository(
    private val coverLetterText: String?,
) : AtsRepository {
    override val userProfile = MutableStateFlow(UserProfile())
    var generateCalls = 0

    override suspend fun replaceCurrentCv(file: PdfFile, onProgress: (Int) -> Unit) =
        CareerPilotResult.Success(Unit)
    override suspend fun importJob(url: String) = CareerPilotResult.Success(workspace())
    override suspend fun getWorkspace(workspaceId: Long) = CareerPilotResult.Success(workspace())
    override suspend fun scoreCv(workspaceId: Long): CareerPilotResult<AtsScore, NetworkError> =
        CareerPilotResult.Error(NetworkError.UNKNOWN)
    override suspend fun optimizeCv(workspaceId: Long): CareerPilotResult<AiJob, NetworkError> =
        CareerPilotResult.Error(NetworkError.UNKNOWN)
    override suspend fun getAiJob(jobId: Long): CareerPilotResult<AiJob, NetworkError> =
        CareerPilotResult.Error(NetworkError.UNKNOWN)
    override suspend fun generateCoverLetter(workspaceId: Long): CareerPilotResult<CoverLetter, NetworkError> {
        generateCalls++
        return CareerPilotResult.Success(
            CoverLetter(
                body = "Generated letter",
                approachTips = null,
                coinCost = 2,
            ),
        )
    }

    private fun workspace() = JobWorkspace(
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
        coverLetterText = coverLetterText,
        lastInterviewSessionId = null,
        createdAt = null,
        updatedAt = null,
    )
}
