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
import com.iti.careerpilot.ats.presentation.optimizedcv.state.OptimizedCvAction
import com.iti.careerpilot.ats.presentation.optimizedcv.viewmodel.OptimizedCvViewModel
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import com.iti.core.datastore.models.UserProfile
import com.iti.core.model.PdfFile
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
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OptimizedCvViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `completed job exposes section improvements`() = runTest(dispatcher) {
        val viewModel = OptimizedCvViewModel(
            getAiJob = GetAiJobUseCase(OptimizationRepository(COMPLETED_JOB)),
        )

        viewModel.onAction(OptimizedCvAction.Initial(42L))
        advanceUntilIdle()

        assertEquals("Experience", viewModel.state.value.sections.single().name)
        assertEquals("Delivered 12 APIs.", viewModel.state.value.sections.single().improvements.single().improved)
    }

    @Test
    fun `pending job exposes a retryable not-ready message`() = runTest(dispatcher) {
        val viewModel = OptimizedCvViewModel(
            getAiJob = GetAiJobUseCase(OptimizationRepository(PENDING_JOB)),
        )

        viewModel.onAction(OptimizedCvAction.Initial(42L))
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.error)
    }
}

private class OptimizationRepository(
    private val job: AiJob,
) : AtsRepository {
    override val userProfile = MutableStateFlow(UserProfile())

    override suspend fun replaceCurrentCv(file: PdfFile, onProgress: (Int) -> Unit) =
        CareerPilotResult.Success(Unit)
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
        sections = listOf(
            CvOptimizationSection(
                name = "Experience",
                score = 82,
                improvements = listOf(
                    CvSectionImprovement(
                        original = "Worked on APIs.",
                        improved = "Delivered 12 APIs.",
                        reason = "Adds measurable impact.",
                    ),
                ),
            ),
        ),
        recommendedTracks = listOf("Backend Development"),
        coinCost = 50,
    ),
)
