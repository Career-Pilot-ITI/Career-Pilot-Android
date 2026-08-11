package com.iti.careerpilot.practicesession.presentation.resultscreen

import androidx.lifecycle.SavedStateHandle
import com.iti.careerpilot.ai.cache.InMemorySessionCache
import com.iti.careerpilot.ai.domain.BodyLanguageAiFeatureToggle
import com.iti.careerpilot.ai.domain.EvaluateBodyLanguageUseCase
import com.iti.careerpilot.ai.evaluator.BodyLanguageAiEvaluator
import com.iti.careerpilot.ai.fallback.LocalBodyLanguageFallbackEngine
import com.iti.careerpilot.practicesession.domain.models.AnswerRequest
import com.iti.careerpilot.practicesession.domain.models.AnswerResponse
import com.iti.careerpilot.practicesession.domain.models.AudioAttachment
import com.iti.careerpilot.practicesession.domain.models.CreateSessionRequest
import com.iti.careerpilot.practicesession.domain.models.Session
import com.iti.careerpilot.practicesession.domain.models.SessionResult
import com.iti.careerpilot.practicesession.domain.repo.SessionRepo
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import com.iti.core.model.bodylanguage.BodyLanguageEvaluation
import com.iti.core.model.bodylanguage.BodyLanguageMetrics
import com.iti.core.model.bodylanguage.ConfidenceBand
import com.iti.core.model.bodylanguage.FallbackReason
import com.iti.core.model.bodylanguage.MetricEvaluation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class ResultViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var sessionCache: InMemorySessionCache

    private val sampleSessionResult = SessionResult(
        id = 1L,
        sessionId = 100L,
        overallScore = 85,
        clarityScore = 80,
        confidenceScore = 85,
        pacingScore = 90,
        fillerWordsScore = 80,
        contentRelevanceScore = 85,
        coachingTips = listOf("Great job", "Speak clearly"),
        generatedAt = "2026-08-11T00:00:00Z",
        createdAt = "2026-08-11T00:00:00Z",
        questions = emptyList()
    )

    private val sampleEvaluation = BodyLanguageEvaluation(
        overallScore = 88,
        eyeContact = MetricEvaluation(85, "Good eye contact", "Keep looking at camera"),
        posture = MetricEvaluation(90, "Great posture", "Keep back straight"),
        facialExpression = MetricEvaluation(85, "Smiled naturally", "Maintain warm expression"),
        handGestures = MetricEvaluation(80, "Controlled gestures", "Keep hands steady"),
        confidenceBand = ConfidenceBand.HIGH,
        summary = "Excellent body language",
        actionableTips = listOf("Maintain eye contact")
    )

    private val sampleMetrics = BodyLanguageMetrics.EMPTY.copy(
        eyeContactPercentage = 85f,
        averageSmile = 0.5f,
        slouchPercentage = 10f,
        fidgetScore = 0.2f
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        sessionCache = InMemorySessionCache()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        savedStateHandle: SavedStateHandle = SavedStateHandle(mapOf("sessionId" to 100L)),
        sessionRepo: SessionRepo = FakeSessionRepo(sampleSessionResult),
        evaluateBodyLanguageUseCase: EvaluateBodyLanguageUseCase = createFakeUseCase(),
    ): ResultViewModel {
        return ResultViewModel(
            savedStateHandle = savedStateHandle,
            sessionRepo = sessionRepo,
            evaluateBodyLanguageUseCase = evaluateBodyLanguageUseCase,
            sessionCache = sessionCache
        )
    }

    private fun createFakeUseCase(): EvaluateBodyLanguageUseCase {
        val fallbackEngine = LocalBodyLanguageFallbackEngine()
        val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true; isLenient = true }
        val evaluator = BodyLanguageAiEvaluator(
            contentGenerator = { throw Exception("No AI in test") },
            fallbackEngine = fallbackEngine,
            json = json,
            ioDispatcher = testDispatcher
        )
        val featureToggle = object : BodyLanguageAiFeatureToggle {
            override fun isAiEvaluationEnabled(): Boolean = false
        }
        return EvaluateBodyLanguageUseCase(evaluator, sessionCache, featureToggle)
    }

    @Test
    fun `init loads feedback from SessionRepo using sessionId from SavedStateHandle`() = runTest {
        val fakeRepo = FakeSessionRepo(sampleSessionResult)
        val viewModel = createViewModel(
            savedStateHandle = SavedStateHandle(mapOf("sessionId" to 100L)),
            sessionRepo = fakeRepo
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(100L, fakeRepo.getSessionFeedbackCalledWithId)
        assertEquals(sampleSessionResult, viewModel.state.value.sessionResult)
        assertEquals(false, viewModel.state.value.isLoading)
    }

    @Test
    fun `init with cached evaluation sets BodyLanguageUiState Success when fallbackReason is null`() = runTest {
        sessionCache.put(100L, sampleEvaluation, fallbackReason = null, metrics = sampleMetrics)

        val viewModel = createViewModel(
            savedStateHandle = SavedStateHandle(mapOf("sessionId" to 100L))
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.bodyLanguageUiState is BodyLanguageUiState.Success)
        assertEquals(sampleEvaluation, (state.bodyLanguageUiState as BodyLanguageUiState.Success).evaluation)
        assertEquals(sampleMetrics, state.bodyLanguageMetrics)
    }

    @Test
    fun `init with cached evaluation sets BodyLanguageUiState FallbackUsed when fallbackReason is present`() = runTest {
        sessionCache.put(100L, sampleEvaluation, fallbackReason = FallbackReason.OFFLINE, metrics = sampleMetrics)

        val viewModel = createViewModel(
            savedStateHandle = SavedStateHandle(mapOf("sessionId" to 100L))
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.bodyLanguageUiState is BodyLanguageUiState.FallbackUsed)
        val fallbackState = state.bodyLanguageUiState as BodyLanguageUiState.FallbackUsed
        assertEquals(sampleEvaluation, fallbackState.evaluation)
        assertEquals(FallbackReason.OFFLINE, fallbackState.reason)
    }

    @Test
    fun `init with cached metrics triggers evaluateBodyLanguageUseCase and updates BodyLanguageUiState`() = runTest {
        sessionCache.putMetrics(100L, sampleMetrics)

        val viewModel = createViewModel(
            savedStateHandle = SavedStateHandle(mapOf("sessionId" to 100L))
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.bodyLanguageUiState is BodyLanguageUiState.FallbackUsed)
        assertEquals(sampleMetrics, state.bodyLanguageMetrics)
    }

    @Test
    fun `init with no metrics or evaluation sets BodyLanguageUiState Idle`() = runTest {
        val viewModel = createViewModel(
            savedStateHandle = SavedStateHandle(mapOf("sessionId" to 100L))
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(BodyLanguageUiState.Idle, state.bodyLanguageUiState)
    }

    @Test
    fun `onAction RefreshResult re-loads feedback and body language`() = runTest {
        val viewModel = createViewModel(
            savedStateHandle = SavedStateHandle(mapOf("sessionId" to 100L))
        )
        testDispatcher.scheduler.advanceUntilIdle()

        sessionCache.put(100L, sampleEvaluation, fallbackReason = null, metrics = sampleMetrics)
        viewModel.onAction(ResultAction.RefreshResult)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.bodyLanguageUiState is BodyLanguageUiState.Success)
    }
}

private class FakeSessionRepo(
    private val result: SessionResult
) : SessionRepo {
    var getSessionFeedbackCalledWithId: Long? = null

    override suspend fun createNewSession(request: CreateSessionRequest): CareerPilotResult<Session, NetworkError> {
        throw NotImplementedError()
    }

    override suspend fun uploadAudio(file: File, onProgress: (Int) -> Unit): CareerPilotResult<AudioAttachment, NetworkError> {
        throw NotImplementedError()
    }

    override suspend fun submitAnswer(sessionId: Long, request: AnswerRequest): CareerPilotResult<AnswerResponse, NetworkError> {
        throw NotImplementedError()
    }

    override suspend fun getSessionFeedback(sessionId: Long): CareerPilotResult<SessionResult, NetworkError> {
        getSessionFeedbackCalledWithId = sessionId
        return CareerPilotResult.Success(result)
    }

    override suspend fun restartOldSession(sessionId: Long): CareerPilotResult<Session, NetworkError> {
        throw NotImplementedError()
    }
}
