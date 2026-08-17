package com.iti.careerpilot.createchallenge.presentation.viewmodel

import com.iti.careerpilot.challengefirestore.Challenge
import com.iti.careerpilot.challengefirestore.ChallengeType
import com.iti.careerpilot.challengefirestore.ChallengeVisibility
import com.iti.careerpilot.challengefirestore.SeniorityLevel
import com.iti.careerpilot.core.access.PlanAccessMap
import com.iti.careerpilot.core.access.domain.usecase.CheckFeatureAccessUseCase
import com.iti.careerpilot.core.access.testing.FakeAccessRepository
import com.iti.careerpilot.createchallenge.domain.repository.CreateChallengeRepository
import com.iti.careerpilot.createchallenge.presentation.action.CreateChallengeAction
import com.iti.careerpilot.createchallenge.presentation.event.CreateChallengeEvent
import com.iti.common.error.FirebaseError
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import com.iti.core.datastore.models.AccountInfo
import com.iti.core.datastore.models.PersonalInfo
import com.iti.core.datastore.models.UserProfile
import com.iti.core.datastore.repo.UserProfileRepo
import com.iti.core.model.AccessState
import com.iti.core.model.FeatureAccess
import com.iti.core.model.FeatureKey
import com.iti.core.model.Plan
import com.iti.core.model.Track
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
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
class CreateChallengeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val dummyUserProfile = UserProfile(
        id = 123L,
        account = AccountInfo(username = "johndoe"),
        personal = PersonalInfo(displayName = "John Doe")
    )

    private val dummyChallenge = Challenge(
        id = "c1",
        creatorId = 123L,
        creatorUsername = "johndoe",
        creatorName = "John Doe",
        trackId = 1L,
        trackName = "Android Developer",
        visibility = ChallengeVisibility.PUBLIC,
        seniorityLevel = SeniorityLevel.MID_LEVEL,
        creationDate = 1700000000L,
        invitationCode = "c1",
        questions = emptyList(),
        type = ChallengeType.AUDIO_ONLY,
    )

    private class FakeUserProfileRepo(
        private val initialProfile: UserProfile = UserProfile(
            id = 123L,
            account = AccountInfo(username = "johndoe"),
            personal = PersonalInfo(displayName = "John Doe")
        )
    ) : UserProfileRepo {
        private val _userProfile = MutableStateFlow(initialProfile)
        override val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()
        override suspend fun readUserProfile(): UserProfile = _userProfile.value
        override suspend fun updateUserProfile(updateBlock: (UserProfile) -> UserProfile) {
            _userProfile.update(updateBlock)
        }
        override suspend fun clearUserProfile() {
            _userProfile.value = UserProfile()
        }
        override suspend fun setBodyLanguageConsent(given: Boolean) {
            _userProfile.update { it.copy(account = it.account.copy(bodyLanguageConsentGiven = given)) }
        }
    }

    private class FakeCreateChallengeRepository : CreateChallengeRepository {
        var tracksResult: CareerPilotResult<List<Track>, NetworkError> = CareerPilotResult.Success(
            listOf(Track(1, "Android Developer"), Track(2, "Frontend Developer"))
        )
        var validateQuestionsResult: CareerPilotResult<Boolean, FirebaseError> = CareerPilotResult.Success(true)
        var createChallengeResult: CareerPilotResult<Unit, FirebaseError> = CareerPilotResult.Success(Unit)
        var getChallengeResult: CareerPilotResult<Challenge, FirebaseError> = CareerPilotResult.Success(
            Challenge(
                id = "c1",
                creatorId = 123L,
                creatorUsername = "johndoe",
                creatorName = "John Doe",
                trackId = 1L,
                trackName = "Android Developer",
                visibility = ChallengeVisibility.PUBLIC,
                seniorityLevel = SeniorityLevel.MID_LEVEL,
                creationDate = 1700000000L,
                invitationCode = "c1",
                questions = emptyList(),
                type = ChallengeType.AUDIO_ONLY
            )
        )
        var deleteChallengeResult: CareerPilotResult<Unit, FirebaseError> = CareerPilotResult.Success(Unit)
        var generatedId: String = "generated_c_123"

        var getTracksCallCount = 0
        var validateQuestionsCallCount = 0
        var createChallengeCallCount = 0
        var lastValidatedQuestions: List<String>? = null
        var lastCreatedChallenge: Challenge? = null

        override suspend fun getTracks(): CareerPilotResult<List<Track>, NetworkError> {
            getTracksCallCount++
            return tracksResult
        }

        override suspend fun validateQuestions(questions: List<String>): CareerPilotResult<Boolean, FirebaseError> {
            validateQuestionsCallCount++
            lastValidatedQuestions = questions
            return validateQuestionsResult
        }

        override suspend fun createChallenge(challenge: Challenge): CareerPilotResult<Unit, FirebaseError> {
            createChallengeCallCount++
            lastCreatedChallenge = challenge
            return createChallengeResult
        }

        override suspend fun getChallenge(challengeId: String): CareerPilotResult<Challenge, FirebaseError> {
            return getChallengeResult
        }

        override suspend fun deleteChallenge(
            challengeId: String,
            visibility: ChallengeVisibility
        ): CareerPilotResult<Unit, FirebaseError> {
            return deleteChallengeResult
        }

        override fun generateChallengeId(visibility: ChallengeVisibility): String {
            return generatedId
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        repository: FakeCreateChallengeRepository = FakeCreateChallengeRepository(),
        userProfileRepo: FakeUserProfileRepo = FakeUserProfileRepo(),
        accessRepository: FakeAccessRepository = FakeAccessRepository(
            AccessState(
                plan = Plan.MAX,
                features = PlanAccessMap.featuresFor(Plan.MAX),
                quotas = emptyMap(),
                expiresAt = null,
                lastSyncedAt = Clock.System.now(),
                coinBalance = 50,
            )
        )
    ): CreateChallengeViewModel {
        return CreateChallengeViewModel(
            repository = repository,
            userProfileRepo = userProfileRepo,
            checkFeatureAccess = CheckFeatureAccessUseCase(accessRepository)
        )
    }

    private fun setValidQuestions(viewModel: CreateChallengeViewModel) {
        val tenQuestions = (1..10).map { "Technical Question $it" }
        tenQuestions.forEachIndexed { index, q ->
            viewModel.onAction(CreateChallengeAction.OnQuestionTextChange(index, q))
        }
    }

    @Test
    fun `Submit challenge when user is on Plan MAX creates challenge successfully`() = runTest {
        val fakeRepo = FakeCreateChallengeRepository()
        val viewModel = createViewModel(repository = fakeRepo)

        viewModel.onAction(CreateChallengeAction.Initial(null))
        testScheduler.advanceUntilIdle()

        setValidQuestions(viewModel)

        viewModel.onAction(CreateChallengeAction.OnSubmit)
        testScheduler.advanceUntilIdle()

        assertEquals(1, fakeRepo.validateQuestionsCallCount)
        assertEquals(1, fakeRepo.createChallengeCallCount)
        assertNotNull(fakeRepo.lastCreatedChallenge)
        assertEquals("generated_c_123", fakeRepo.lastCreatedChallenge?.id)
        assertTrue(viewModel.state.value.isSuccessDialogVisible)
        assertFalse(viewModel.state.value.isSubmitting)
    }

    @Test
    fun `Submit challenge when user is on Plan FREE is blocked by feature access guard`() = runTest {
        val accessRepo = FakeAccessRepository(
            AccessState(
                plan = Plan.FREE,
                features = PlanAccessMap.featuresFor(Plan.FREE),
                quotas = emptyMap(),
                expiresAt = null,
                lastSyncedAt = Clock.System.now(),
                coinBalance = 0,
            )
        )
        val fakeRepo = FakeCreateChallengeRepository()
        val viewModel = createViewModel(repository = fakeRepo, accessRepository = accessRepo)

        viewModel.onAction(CreateChallengeAction.Initial(null))
        testScheduler.advanceUntilIdle()

        setValidQuestions(viewModel)

        viewModel.onAction(CreateChallengeAction.OnSubmit)
        testScheduler.advanceUntilIdle()

        assertEquals(0, fakeRepo.validateQuestionsCallCount)
        assertEquals(0, fakeRepo.createChallengeCallCount)
        assertFalse(viewModel.state.value.isSuccessDialogVisible)
        assertFalse(viewModel.state.value.isSubmitting)
    }

    @Test
    fun `Submit challenge when user is on Plan PLUS is blocked by feature access guard`() = runTest {
        val accessRepo = FakeAccessRepository(
            AccessState(
                plan = Plan.PLUS,
                features = PlanAccessMap.featuresFor(Plan.PLUS),
                quotas = emptyMap(),
                expiresAt = null,
                lastSyncedAt = Clock.System.now(),
                coinBalance = 0,
            )
        )
        val fakeRepo = FakeCreateChallengeRepository()
        val viewModel = createViewModel(repository = fakeRepo, accessRepository = accessRepo)

        viewModel.onAction(CreateChallengeAction.Initial(null))
        testScheduler.advanceUntilIdle()

        setValidQuestions(viewModel)

        viewModel.onAction(CreateChallengeAction.OnSubmit)
        testScheduler.advanceUntilIdle()

        assertEquals(0, fakeRepo.validateQuestionsCallCount)
        assertEquals(0, fakeRepo.createChallengeCallCount)
        assertFalse(viewModel.state.value.isSuccessDialogVisible)
        assertFalse(viewModel.state.value.isSubmitting)
    }

    @Test
    fun `Submit challenge when cache is stale is blocked`() = runTest {
        val accessRepo = FakeAccessRepository(
            AccessState(
                plan = Plan.MAX,
                features = PlanAccessMap.featuresFor(Plan.MAX),
                quotas = emptyMap(),
                expiresAt = null,
                lastSyncedAt = Clock.System.now() - 25.hours,
                coinBalance = 50,
            )
        )
        val fakeRepo = FakeCreateChallengeRepository()
        val viewModel = createViewModel(repository = fakeRepo, accessRepository = accessRepo)

        viewModel.onAction(CreateChallengeAction.Initial(null))
        testScheduler.advanceUntilIdle()

        setValidQuestions(viewModel)

        viewModel.onAction(CreateChallengeAction.OnSubmit)
        testScheduler.advanceUntilIdle()

        assertEquals(0, fakeRepo.validateQuestionsCallCount)
        assertEquals(0, fakeRepo.createChallengeCallCount)
        assertFalse(viewModel.state.value.isSuccessDialogVisible)
    }

    @Test
    fun `Submit challenge with fewer than 10 non-blank questions blocks submission`() = runTest {
        val fakeRepo = FakeCreateChallengeRepository()
        val viewModel = createViewModel(repository = fakeRepo)

        viewModel.onAction(CreateChallengeAction.Initial(null))
        testScheduler.advanceUntilIdle()

        // Provide only 5 valid questions
        (1..5).forEachIndexed { index, q ->
            viewModel.onAction(CreateChallengeAction.OnQuestionTextChange(index, "Question $q"))
        }

        viewModel.onAction(CreateChallengeAction.OnSubmit)
        testScheduler.advanceUntilIdle()

        assertEquals(0, fakeRepo.validateQuestionsCallCount)
        assertEquals(0, fakeRepo.createChallengeCallCount)
        assertFalse(viewModel.state.value.isSuccessDialogVisible)
    }

    @Test
    fun `Submit challenge with duplicate questions blocks submission`() = runTest {
        val fakeRepo = FakeCreateChallengeRepository()
        val viewModel = createViewModel(repository = fakeRepo)

        viewModel.onAction(CreateChallengeAction.Initial(null))
        testScheduler.advanceUntilIdle()

        // 10 questions but duplicates
        (1..10).forEachIndexed { index, _ ->
            viewModel.onAction(CreateChallengeAction.OnQuestionTextChange(index, "Duplicate Question"))
        }

        viewModel.onAction(CreateChallengeAction.OnSubmit)
        testScheduler.advanceUntilIdle()

        assertEquals(0, fakeRepo.validateQuestionsCallCount)
        assertEquals(0, fakeRepo.createChallengeCallCount)
        assertFalse(viewModel.state.value.isSuccessDialogVisible)
    }

    @Test
    fun `Add question increases question count in state`() = runTest {
        val viewModel = createViewModel()
        val initialCount = viewModel.state.value.questions.size

        viewModel.onAction(CreateChallengeAction.OnAddQuestion)

        assertEquals(initialCount + 1, viewModel.state.value.questions.size)
    }

    @Test
    fun `Remove question with confirmation removes the question when count is greater than 10`() = runTest {
        val viewModel = createViewModel()
        // Default questions size is 10. Add one more.
        viewModel.onAction(CreateChallengeAction.OnAddQuestion)
        assertEquals(11, viewModel.state.value.questions.size)

        viewModel.onAction(CreateChallengeAction.OnQuestionTextChange(10, "Custom Question 11"))
        viewModel.onAction(CreateChallengeAction.OnRemoveQuestion(10))

        assertEquals(10, viewModel.state.value.questionToDeleteIndex)

        viewModel.onAction(CreateChallengeAction.OnConfirmDeleteQuestion)

        assertEquals(10, viewModel.state.value.questions.size)
        assertNull(viewModel.state.value.questionToDeleteIndex)
    }

    @Test
    fun `Dismissing success dialog emits NavigateToDashboard`() = runTest {
        val viewModel = createViewModel()
        val events = mutableListOf<CreateChallengeEvent>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(events)
        }

        viewModel.onAction(CreateChallengeAction.OnDismissSuccess)
        testScheduler.advanceUntilIdle()

        assertEquals(1, events.size)
        assertTrue(events.first() is CreateChallengeEvent.NavigateToDashboard)
        assertFalse(viewModel.state.value.isSuccessDialogVisible)
        job.cancel()
    }

    @Test
    fun `OnBackClicked emits NavigateBack`() = runTest {
        val viewModel = createViewModel()
        val events = mutableListOf<CreateChallengeEvent>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(events)
        }

        viewModel.onAction(CreateChallengeAction.OnBackClicked)
        testScheduler.advanceUntilIdle()

        assertEquals(1, events.size)
        assertTrue(events.first() is CreateChallengeEvent.NavigateBack)
        job.cancel()
    }

    @Test
    fun `OnShareSuccessChallenge sets isShareDialogVisible to true`() = runTest {
        val viewModel = createViewModel()
        assertFalse(viewModel.state.value.isShareDialogVisible)

        viewModel.onAction(CreateChallengeAction.OnShareSuccessChallenge)

        assertTrue(viewModel.state.value.isShareDialogVisible)
    }

    @Test
    fun `OnDismissShareDialog sets isShareDialogVisible to false`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(CreateChallengeAction.OnShareSuccessChallenge)
        assertTrue(viewModel.state.value.isShareDialogVisible)

        viewModel.onAction(CreateChallengeAction.OnDismissShareDialog)

        assertFalse(viewModel.state.value.isShareDialogVisible)
    }
}

