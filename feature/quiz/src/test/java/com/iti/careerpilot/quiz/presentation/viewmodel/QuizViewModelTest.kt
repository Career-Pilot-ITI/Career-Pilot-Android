package com.iti.careerpilot.quiz.presentation.viewmodel

import com.iti.careerpilot.core.access.PlanAccessMap
import com.iti.careerpilot.core.access.domain.usecase.CheckFeatureAccessUseCase
import com.iti.careerpilot.core.access.domain.usecase.RefreshAccessUseCase
import com.iti.careerpilot.core.access.testing.FakeAccessRepository
import com.iti.careerpilot.quiz.domain.model.LearningPoint
import com.iti.careerpilot.quiz.domain.model.LearningPointResponse
import com.iti.careerpilot.quiz.domain.model.LearningQuiz
import com.iti.careerpilot.quiz.domain.model.QuizQuestion
import com.iti.careerpilot.quiz.domain.model.StudyTopic
import com.iti.careerpilot.quiz.domain.repository.QuizRepository
import com.iti.careerpilot.quiz.presentation.action.QuizAction
import com.iti.careerpilot.quiz.presentation.event.QuizEvent
import com.iti.careerpilot.quiz.presentation.state.QuizStep
import com.iti.careerpilot.quiz.presentation.state.RetryType
import com.iti.common.error.FirebaseError
import com.iti.common.result.CareerPilotResult
import com.iti.core.model.AccessState
import com.iti.core.model.FeatureAccess
import com.iti.core.model.FeatureKey
import com.iti.core.model.FeatureQuota
import com.iti.core.model.Plan
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QuizViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val dummyTopics = listOf(
        StudyTopic(id = "1", title = "Kotlin Coroutines", description = "Asynchronous programming"),
        StudyTopic(id = "2", title = "Jetpack Compose", description = "Modern UI toolkit"),
    )

    private val dummyLearningPoint = LearningPoint(
        title = "Flows in Coroutines",
        explanation = "Cold asynchronous data streams",
        example = "val flow = flow { emit(1) }",
        isCode = true
    )

    private val dummyQuiz = LearningQuiz(
        questions = listOf(
            QuizQuestion(
                id = "q1",
                question = "Are Flows hot or cold by default?",
                options = listOf("Hot", "Cold", "Warm"),
                correctAnswerIndex = 1,
                explanation = "Flows are cold streams."
            ),
            QuizQuestion(
                id = "q2",
                question = "Which operator transforms values?",
                options = listOf("map", "filter", "collect"),
                correctAnswerIndex = 0,
                explanation = "map transforms values."
            )
        )
    )

    private class FakeQuizRepo : QuizRepository {
        var topicsResult: CareerPilotResult<List<StudyTopic>, FirebaseError> = CareerPilotResult.Success(emptyList())
        var learningPointResult: CareerPilotResult<LearningPointResponse, FirebaseError> =
            CareerPilotResult.Success(LearningPointResponse(topicCompleted = false, coveredConcept = null, learningPoint = null))
        var quizResult: CareerPilotResult<LearningQuiz, FirebaseError> = CareerPilotResult.Success(LearningQuiz(emptyList()))

        var generateTopicsCallCount = 0
        var generateLearningPointCallCount = 0
        var generateQuizCallCount = 0

        override suspend fun generateTopics(
            track: String,
            seniority: String
        ): CareerPilotResult<List<StudyTopic>, FirebaseError> {
            generateTopicsCallCount++
            return topicsResult
        }

        override suspend fun generateNextLearningPoint(
            track: String,
            seniority: String,
            topic: String,
            coveredConcepts: List<String>
        ): CareerPilotResult<LearningPointResponse, FirebaseError> {
            generateLearningPointCallCount++
            return learningPointResult
        }

        override suspend fun generateQuiz(
            topic: String,
            learningPoint: LearningPoint
        ): CareerPilotResult<LearningQuiz, FirebaseError> {
            generateQuizCallCount++
            return quizResult
        }
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        quizRepo: FakeQuizRepo = FakeQuizRepo(),
        accessRepository: FakeAccessRepository = FakeAccessRepository(
            AccessState(
                plan = Plan.MAX,
                features = PlanAccessMap.featuresFor(Plan.MAX),
                quotas = emptyMap(),
                expiresAt = null,
                lastSyncedAt = Clock.System.now(),
                coinBalance = 50,
            )
        ),
    ): QuizViewModel {
        return QuizViewModel(
            quizRepo = quizRepo,
            checkFeatureAccess = CheckFeatureAccessUseCase(accessRepository),
            refreshAccess = RefreshAccessUseCase(accessRepository),
            accessRepository = accessRepository,
        )
    }

    @Test
    fun `Init action sets trackName in state`() = runTest {
        val viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        viewModel.onAction(QuizAction.Init("Android Engineer"))
        assertEquals("Android Engineer", viewModel.state.value.trackName)
    }

    @Test
    fun `observing accessState updates planDisplayName and coinBalance`() = runTest {
        val fakeRepo = FakeAccessRepository(
            AccessState(
                plan = Plan.PLUS,
                features = PlanAccessMap.featuresFor(Plan.PLUS),
                quotas = emptyMap(),
                expiresAt = null,
                lastSyncedAt = Clock.System.now(),
                coinBalance = 150,
            )
        )
        val viewModel = createViewModel(accessRepository = fakeRepo)
        testScheduler.advanceUntilIdle()

        assertEquals("Plus", viewModel.state.value.planDisplayName)
        assertEquals(150, viewModel.state.value.coinBalance)
    }

    @Test
    fun `FREE user with Locked access selecting seniority shows gate sheet with dynamic features and does not generate topics`() = runTest {
        val fakeRepo = FakeAccessRepository(
            AccessState(
                plan = Plan.FREE,
                features = PlanAccessMap.featuresFor(Plan.FREE),
                quotas = emptyMap(),
                expiresAt = null,
                lastSyncedAt = Clock.System.now(),
                coinBalance = 0,
            )
        )
        val fakeQuizRepo = FakeQuizRepo()
        val viewModel = createViewModel(quizRepo = fakeQuizRepo, accessRepository = fakeRepo)
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.state.value.quizAccess is FeatureAccess.Locked)

        viewModel.onAction(QuizAction.SenioritySelected("Junior"))
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.state.value.showGateSheet)
        assertEquals(Plan.MAX, viewModel.state.value.gateRequiredPlan)
        val expectedFeatures = PlanAccessMap.featuresFor(Plan.MAX).map { it.displayName() }
        assertEquals(expectedFeatures, viewModel.state.value.gatePlanFeatures)
        assertEquals(0, fakeQuizRepo.generateTopicsCallCount)
        assertEquals(QuizStep.SelectSeniority, viewModel.state.value.currentStep)
    }

    @Test
    fun `User with CoinTopUpRequired selecting seniority shows coin top up sheet`() = runTest {
        val quota = FeatureQuota(
            feature = FeatureKey.Quizzes,
            remaining = 0,
            max = 5,
            coinCost = 20,
        )
        val fakeRepo = FakeAccessRepository(
            AccessState(
                plan = Plan.MAX,
                features = PlanAccessMap.featuresFor(Plan.MAX),
                quotas = mapOf(FeatureKey.Quizzes to quota),
                expiresAt = null,
                lastSyncedAt = Clock.System.now(),
                coinBalance = 5,
            )
        )
        val fakeQuizRepo = FakeQuizRepo()
        val viewModel = createViewModel(quizRepo = fakeQuizRepo, accessRepository = fakeRepo)
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.state.value.quizAccess is FeatureAccess.CoinTopUpRequired)

        viewModel.onAction(QuizAction.SenioritySelected("Mid-level"))
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.state.value.showCoinTopUpSheet)
        assertEquals(20, viewModel.state.value.coinTopUpRequiredCost)
        assertEquals(0, fakeQuizRepo.generateTopicsCallCount)
    }

    @Test
    fun `StaleCacheBlocked access triggers refresh on init and on SenioritySelected`() = runTest {
        val fakeRepo = FakeAccessRepository(
            AccessState(
                plan = Plan.MAX,
                features = PlanAccessMap.featuresFor(Plan.MAX),
                quotas = emptyMap(),
                expiresAt = null,
                lastSyncedAt = Clock.System.now() - 25.hours,
                coinBalance = 50,
            )
        )
        val fakeQuizRepo = FakeQuizRepo()
        val viewModel = createViewModel(quizRepo = fakeQuizRepo, accessRepository = fakeRepo)
        testScheduler.advanceUntilIdle()

        val refreshAfterInit = fakeRepo.refreshCount
        assertTrue(refreshAfterInit > 0)

        viewModel.onAction(QuizAction.SenioritySelected("Senior"))
        testScheduler.advanceUntilIdle()

        assertTrue(fakeRepo.refreshCount > refreshAfterInit)
        assertEquals(0, fakeQuizRepo.generateTopicsCallCount)
    }

    @Test
    fun `MAX user with Granted access successfully selects seniority and generates topics`() = runTest {
        val fakeRepo = FakeAccessRepository(
            AccessState(
                plan = Plan.MAX,
                features = PlanAccessMap.featuresFor(Plan.MAX),
                quotas = emptyMap(),
                expiresAt = null,
                lastSyncedAt = Clock.System.now(),
                coinBalance = 50,
            )
        )
        val fakeQuizRepo = FakeQuizRepo().apply {
            topicsResult = CareerPilotResult.Success(dummyTopics)
        }
        val viewModel = createViewModel(quizRepo = fakeQuizRepo, accessRepository = fakeRepo)
        testScheduler.advanceUntilIdle()

        viewModel.onAction(QuizAction.Init("Android Engineer"))
        viewModel.onAction(QuizAction.SenioritySelected("Senior"))
        testScheduler.advanceUntilIdle()

        assertEquals("Senior", viewModel.state.value.seniority)
        assertEquals(1, fakeQuizRepo.generateTopicsCallCount)
        assertEquals(dummyTopics, viewModel.state.value.topics)
        assertEquals(QuizStep.Topics, viewModel.state.value.currentStep)
        assertFalse(viewModel.state.value.isLoading)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `Generate topics failure updates error state and retryType`() = runTest {
        val fakeQuizRepo = FakeQuizRepo().apply {
            topicsResult = CareerPilotResult.Error(FirebaseError.BAD_RESPONSE)
        }
        val viewModel = createViewModel(quizRepo = fakeQuizRepo)
        testScheduler.advanceUntilIdle()

        viewModel.onAction(QuizAction.Init("Android Engineer"))
        viewModel.onAction(QuizAction.SenioritySelected("Senior"))
        testScheduler.advanceUntilIdle()

        assertEquals(QuizStep.Error, viewModel.state.value.currentStep)
        assertEquals(RetryType.GENERATE_TOPICS, viewModel.state.value.retryType)
        assertNotNull(viewModel.state.value.error)
    }

    @Test
    fun `Retry action retries generate topics when error occurred`() = runTest {
        val fakeQuizRepo = FakeQuizRepo().apply {
            topicsResult = CareerPilotResult.Error(FirebaseError.TIMEOUT)
        }
        val viewModel = createViewModel(quizRepo = fakeQuizRepo)
        testScheduler.advanceUntilIdle()

        viewModel.onAction(QuizAction.Init("Android Engineer"))
        viewModel.onAction(QuizAction.SenioritySelected("Senior"))
        testScheduler.advanceUntilIdle()

        assertEquals(1, fakeQuizRepo.generateTopicsCallCount)

        // Change response to success and retry
        fakeQuizRepo.topicsResult = CareerPilotResult.Success(dummyTopics)
        viewModel.onAction(QuizAction.Retry)
        testScheduler.advanceUntilIdle()

        assertEquals(2, fakeQuizRepo.generateTopicsCallCount)
        assertEquals(dummyTopics, viewModel.state.value.topics)
        assertEquals(QuizStep.Topics, viewModel.state.value.currentStep)
    }

    @Test
    fun `DismissGateSheet hides gate sheet`() = runTest {
        val fakeRepo = FakeAccessRepository(
            AccessState(
                plan = Plan.FREE,
                features = PlanAccessMap.featuresFor(Plan.FREE),
                quotas = emptyMap(),
                expiresAt = null,
                lastSyncedAt = Clock.System.now(),
                coinBalance = 0,
            )
        )
        val viewModel = createViewModel(accessRepository = fakeRepo)
        testScheduler.advanceUntilIdle()

        viewModel.onAction(QuizAction.SenioritySelected("Junior"))
        testScheduler.advanceUntilIdle()
        assertTrue(viewModel.state.value.showGateSheet)

        viewModel.onAction(QuizAction.DismissGateSheet)
        assertFalse(viewModel.state.value.showGateSheet)
    }

    @Test
    fun `DismissCoinTopUpSheet hides top up sheet`() = runTest {
        val quota = FeatureQuota(
            feature = FeatureKey.Quizzes,
            remaining = 0,
            max = 5,
            coinCost = 20,
        )
        val fakeRepo = FakeAccessRepository(
            AccessState(
                plan = Plan.MAX,
                features = PlanAccessMap.featuresFor(Plan.MAX),
                quotas = mapOf(FeatureKey.Quizzes to quota),
                expiresAt = null,
                lastSyncedAt = Clock.System.now(),
                coinBalance = 5,
            )
        )
        val viewModel = createViewModel(accessRepository = fakeRepo)
        testScheduler.advanceUntilIdle()

        viewModel.onAction(QuizAction.SenioritySelected("Mid-level"))
        testScheduler.advanceUntilIdle()
        assertTrue(viewModel.state.value.showCoinTopUpSheet)

        viewModel.onAction(QuizAction.DismissCoinTopUpSheet)
        assertFalse(viewModel.state.value.showCoinTopUpSheet)
    }

    @Test
    fun `UpgradeFromGate closes gate sheet and emits NavigateToPaywall with showGetCoins false`() = runTest {
        val fakeRepo = FakeAccessRepository(
            AccessState(
                plan = Plan.FREE,
                features = PlanAccessMap.featuresFor(Plan.FREE),
                quotas = emptyMap(),
                expiresAt = null,
                lastSyncedAt = Clock.System.now(),
                coinBalance = 0,
            )
        )
        val viewModel = createViewModel(accessRepository = fakeRepo)
        testScheduler.advanceUntilIdle()

        viewModel.onAction(QuizAction.SenioritySelected("Junior"))
        testScheduler.advanceUntilIdle()
        assertTrue(viewModel.state.value.showGateSheet)

        val events = mutableListOf<QuizEvent>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(events)
        }

        viewModel.onAction(QuizAction.UpgradeFromGate)
        testScheduler.advanceUntilIdle()

        assertFalse(viewModel.state.value.showGateSheet)
        val paywallEvent = events.filterIsInstance<QuizEvent.NavigateToPaywall>().firstOrNull()
        assertNotNull("Expected NavigateToPaywall event", paywallEvent)
        assertFalse(paywallEvent!!.showGetCoins)
        job.cancel()
    }

    @Test
    fun `BuyCoinsClicked closes coin top up sheet and emits NavigateToPaywall with showGetCoins true`() = runTest {
        val quota = FeatureQuota(
            feature = FeatureKey.Quizzes,
            remaining = 0,
            max = 5,
            coinCost = 20,
        )
        val fakeRepo = FakeAccessRepository(
            AccessState(
                plan = Plan.MAX,
                features = PlanAccessMap.featuresFor(Plan.MAX),
                quotas = mapOf(FeatureKey.Quizzes to quota),
                expiresAt = null,
                lastSyncedAt = Clock.System.now(),
                coinBalance = 5,
            )
        )
        val viewModel = createViewModel(accessRepository = fakeRepo)
        testScheduler.advanceUntilIdle()

        viewModel.onAction(QuizAction.SenioritySelected("Mid-level"))
        testScheduler.advanceUntilIdle()
        assertTrue(viewModel.state.value.showCoinTopUpSheet)

        val events = mutableListOf<QuizEvent>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(events)
        }

        viewModel.onAction(QuizAction.BuyCoinsClicked)
        testScheduler.advanceUntilIdle()

        assertFalse(viewModel.state.value.showCoinTopUpSheet)
        val paywallEvent = events.filterIsInstance<QuizEvent.NavigateToPaywall>().firstOrNull()
        assertNotNull("Expected NavigateToPaywall event", paywallEvent)
        assertTrue(paywallEvent!!.showGetCoins)
        job.cancel()
    }

    @Test
    fun `TopicSelected generates next learning point`() = runTest {
        val fakeQuizRepo = FakeQuizRepo().apply {
            learningPointResult = CareerPilotResult.Success(
                LearningPointResponse(
                    topicCompleted = false,
                    coveredConcept = "Flow basics",
                    learningPoint = dummyLearningPoint
                )
            )
        }
        val viewModel = createViewModel(quizRepo = fakeQuizRepo)
        testScheduler.advanceUntilIdle()

        val topic = dummyTopics.first()
        viewModel.onAction(QuizAction.TopicSelected(topic))
        testScheduler.advanceUntilIdle()

        assertEquals(1, fakeQuizRepo.generateLearningPointCallCount)
        assertEquals(topic, viewModel.state.value.selectedTopic)
        assertEquals(dummyLearningPoint, viewModel.state.value.currentLearningPoint)
        assertEquals(QuizStep.LearningPoint, viewModel.state.value.currentStep)
    }

    @Test
    fun `StartQuiz generates next quiz`() = runTest {
        val fakeQuizRepo = FakeQuizRepo().apply {
            learningPointResult = CareerPilotResult.Success(
                LearningPointResponse(
                    topicCompleted = false,
                    coveredConcept = "Flow basics",
                    learningPoint = dummyLearningPoint
                )
            )
            quizResult = CareerPilotResult.Success(dummyQuiz)
        }
        val viewModel = createViewModel(quizRepo = fakeQuizRepo)
        testScheduler.advanceUntilIdle()

        val topic = dummyTopics.first()
        viewModel.onAction(QuizAction.TopicSelected(topic))
        testScheduler.advanceUntilIdle()

        viewModel.onAction(QuizAction.StartQuiz)
        testScheduler.advanceUntilIdle()

        assertEquals(1, fakeQuizRepo.generateQuizCallCount)
        assertEquals(dummyQuiz, viewModel.state.value.currentQuiz)
        assertEquals(QuizStep.Quiz, viewModel.state.value.currentStep)
    }

    @Test
    fun `AnswerSelected and SubmitQuiz calculates score and updates currentStep to QuizResult`() = runTest {
        val fakeQuizRepo = FakeQuizRepo().apply {
            learningPointResult = CareerPilotResult.Success(
                LearningPointResponse(
                    topicCompleted = false,
                    coveredConcept = "Flow basics",
                    learningPoint = dummyLearningPoint
                )
            )
            quizResult = CareerPilotResult.Success(dummyQuiz)
        }
        val viewModel = createViewModel(quizRepo = fakeQuizRepo)
        testScheduler.advanceUntilIdle()

        val topic = dummyTopics.first()
        viewModel.onAction(QuizAction.TopicSelected(topic))
        testScheduler.advanceUntilIdle()

        viewModel.onAction(QuizAction.StartQuiz)
        testScheduler.advanceUntilIdle()

        // Question 1 correct answer is index 1 -> select 1 (correct)
        viewModel.onAction(QuizAction.AnswerSelected(questionIndex = 0, optionIndex = 1))
        // Question 2 correct answer is index 0 -> select 1 (wrong)
        viewModel.onAction(QuizAction.AnswerSelected(questionIndex = 1, optionIndex = 1))

        viewModel.onAction(QuizAction.SubmitQuiz)
        testScheduler.advanceUntilIdle()

        assertEquals(1, viewModel.state.value.quizScore)
        assertEquals(QuizStep.QuizResult, viewModel.state.value.currentStep)
    }

    @Test
    fun `BackToTopics resets currentStep to Topics and clears selectedTopic`() = runTest {
        val viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        viewModel.onAction(QuizAction.TopicSelected(dummyTopics.first()))
        viewModel.onAction(QuizAction.BackToTopics)

        assertEquals(QuizStep.Topics, viewModel.state.value.currentStep)
        assertNull(viewModel.state.value.selectedTopic)
    }

    @Test
    fun `BackToSeniority resets currentStep to SelectSeniority and clears topics`() = runTest {
        val viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        viewModel.onAction(QuizAction.BackToSeniority)

        assertEquals(QuizStep.SelectSeniority, viewModel.state.value.currentStep)
        assertTrue(viewModel.state.value.topics.isEmpty())
    }
}
