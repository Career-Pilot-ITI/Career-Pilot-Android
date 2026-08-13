package com.iti.careerpilot.ats.presentation.scoring

import androidx.lifecycle.SavedStateHandle
import com.iti.careerpilot.ats.domain.model.AtsScore
import com.iti.careerpilot.ats.domain.model.AtsSectionScore
import com.iti.careerpilot.ats.domain.model.CoverLetter
import com.iti.careerpilot.ats.domain.model.CvOptimization
import com.iti.careerpilot.ats.domain.model.JobListing
import com.iti.careerpilot.ats.domain.model.JobWorkspace
import com.iti.careerpilot.ats.domain.repository.AtsRepository
import com.iti.careerpilot.ats.domain.usecase.GetWorkspaceUseCase
import com.iti.careerpilot.ats.domain.usecase.ObserveCurrentProfileUseCase
import com.iti.careerpilot.ats.domain.usecase.ScoreCvUseCase
import com.iti.careerpilot.ats.presentation.scoring.state.ScoringAction
import com.iti.careerpilot.ats.presentation.scoring.state.ScoringEffect
import com.iti.careerpilot.ats.presentation.scoring.viewmodel.ScoringViewModel
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import com.iti.core.datastore.models.UserProfile
import com.iti.core.datastore.models.CareerInfo
import com.iti.core.model.PdfFile
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class ScoringViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `opening score loads workspace and scores automatically`() = runTest(dispatcher) {
        val repository = ScoringRepository()
        val viewModel = createViewModel(repository, SavedStateHandle())

        viewModel.onAction(ScoringAction.Initial(1L))
        advanceUntilIdle()

        assertEquals(1, repository.workspaceCalls)
        assertEquals(1, repository.scoreCalls)
        assertEquals(4, viewModel.state.value.score?.coinCost)
    }

    @Test
    fun `duplicate initial actions execute one paid score request`() = runTest(dispatcher) {
        val repository = ScoringRepository().apply { holdScore = CompletableDeferred() }
        val viewModel = createViewModel(repository, SavedStateHandle())
        viewModel.onAction(ScoringAction.Initial(1L))
        viewModel.onAction(ScoringAction.Initial(1L))
        runCurrent()

        assertEquals(1, repository.scoreCalls)
        repository.holdScore?.complete(Unit)
        advanceUntilIdle()
        assertEquals(4, viewModel.state.value.score?.coinCost)
    }

    @Test
    fun `insufficient coins exposes Paywall recovery`() = runTest(dispatcher) {
        val repository = ScoringRepository().apply {
            scoreResult = CareerPilotResult.Error(NetworkError.INSUFFICIENT_COINS)
        }
        val viewModel = createViewModel(repository, SavedStateHandle())
        viewModel.onAction(ScoringAction.Initial(1L))
        advanceUntilIdle()

        assertTrue(viewModel.state.value.hasInsufficientCoins)
        assertFalse(viewModel.state.value.wasInterrupted)
    }

    @Test
    fun `start practice forwards workspace through readiness navigation`() = runTest(dispatcher) {
        val repository = ScoringRepository().apply {
            userProfile.value = UserProfile(
                career = CareerInfo(trackId = 5L, trackName = "Android"),
            )
        }
        val viewModel = createViewModel(repository, SavedStateHandle())
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

    private fun createViewModel(repository: ScoringRepository, state: SavedStateHandle) = ScoringViewModel(
        getWorkspace = GetWorkspaceUseCase(repository),
        scoreCv = ScoreCvUseCase(repository),
        observeCurrentProfile = ObserveCurrentProfileUseCase(repository),
        savedStateHandle = state,
    )
}

private class ScoringRepository : AtsRepository {
    override val userProfile = MutableStateFlow(UserProfile())
    var workspaceCalls = 0
    var scoreCalls = 0
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
    override suspend fun optimizeCv(workspaceId: Long): CareerPilotResult<CvOptimization, NetworkError> =
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
            matchedSkills = listOf("Kotlin"),
            missingRequiredSkills = emptyList(),
            missingPreferredSkills = emptyList(),
            strengths = emptyList(),
            weaknesses = emptyList(),
            sections = listOf(AtsSectionScore("Projects", 60, "Add metrics")),
            recommendations = emptyList(),
            coinCost = 4,
            cvScoreUpdatedAt = null,
        )
    }
}
