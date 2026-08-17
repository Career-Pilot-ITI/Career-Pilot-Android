package com.iti.careerpilot.challengedashboard.presentation.viewmodel

import com.iti.careerpilot.challengedashboard.domain.repository.ChallengeDashboardRepository
import com.iti.careerpilot.challengedashboard.presentation.action.ChallengeDashboardAction
import com.iti.careerpilot.challengedashboard.presentation.event.ChallengeDashboardEvent
import com.iti.careerpilot.challengedashboard.presentation.state.DashboardTab
import com.iti.careerpilot.challengefirestore.Challenge
import com.iti.careerpilot.challengefirestore.ChallengeSession
import com.iti.careerpilot.challengefirestore.ChallengeType
import com.iti.careerpilot.challengefirestore.ChallengeVisibility
import com.iti.careerpilot.challengefirestore.SeniorityLevel
import com.iti.common.error.FirebaseError
import com.iti.common.result.CareerPilotResult
import com.iti.core.datastore.models.AccountInfo
import com.iti.core.datastore.models.PersonalInfo
import com.iti.core.datastore.models.UserProfile
import com.iti.core.datastore.repo.UserProfileRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.toList
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChallengeDashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val dummyUserProfile = UserProfile(
        id = 123L,
        account = AccountInfo(username = "johndoe"),
        personal = PersonalInfo(displayName = "John Doe")
    )

    private val dummyChallenge = Challenge(
        id = "ch_1",
        creatorId = 123L,
        creatorUsername = "johndoe",
        creatorName = "John Doe",
        trackId = 1L,
        trackName = "Android",
        visibility = ChallengeVisibility.PRIVATE,
        seniorityLevel = SeniorityLevel.MID_LEVEL,
        creationDate = 1700000000L,
        invitationCode = "INV123",
        questions = emptyList(),
        type = ChallengeType.AUDIO_ONLY,
    )

    private val dummySession = ChallengeSession(
        sessionId = "sess_1",
        challengeId = "ch_1",
        challengeTitle = "Android Mid-Level Challenge",
        participantId = 123L,
        participantName = "John Doe",
        participantEmail = "john@example.com",
        timestamp = 1700000000L,
        maxQuestions = 5,
        answeredCount = 5,
        overallScore = 85,
        status = "COMPLETED"
    )

    private class FakeChallengeDashboardRepository : ChallengeDashboardRepository {
        var createdChallengesResult: CareerPilotResult<List<Challenge>, FirebaseError> =
            CareerPilotResult.Success(emptyList())
        var takenChallengesResult: CareerPilotResult<List<ChallengeSession>, FirebaseError> =
            CareerPilotResult.Success(emptyList())
        var challengeSessionsResult: CareerPilotResult<List<ChallengeSession>, FirebaseError> =
            CareerPilotResult.Success(emptyList())
        var deleteChallengeResult: CareerPilotResult<Unit, FirebaseError> =
            CareerPilotResult.Success(Unit)

        var lastDeletedChallengeId: String? = null
        var lastDeletedVisibility: ChallengeVisibility? = null

        override suspend fun getCreatedChallenges(creatorId: Long): CareerPilotResult<List<Challenge>, FirebaseError> {
            return createdChallengesResult
        }

        override suspend fun getTakenChallenges(participantId: Long): CareerPilotResult<List<ChallengeSession>, FirebaseError> {
            return takenChallengesResult
        }

        override suspend fun getChallengeSessions(challengeId: String): CareerPilotResult<List<ChallengeSession>, FirebaseError> {
            return challengeSessionsResult
        }

        override suspend fun deleteChallenge(
            challengeId: String,
            visibility: ChallengeVisibility
        ): CareerPilotResult<Unit, FirebaseError> {
            lastDeletedChallengeId = challengeId
            lastDeletedVisibility = visibility
            return deleteChallengeResult
        }
    }

    private class FakeUserProfileRepo(private val userProfileValue: UserProfile) : UserProfileRepo {
        private val _userProfile = MutableStateFlow(userProfileValue)
        override val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

        override suspend fun readUserProfile(): UserProfile = userProfileValue
        override suspend fun updateUserProfile(updateBlock: (UserProfile) -> UserProfile) {}
        override suspend fun clearUserProfile() {}
        override suspend fun setBodyLanguageConsent(given: Boolean) {}
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
        repository: FakeChallengeDashboardRepository = FakeChallengeDashboardRepository(),
        userProfile: UserProfile = dummyUserProfile
    ): Pair<ChallengeDashboardViewModel, FakeChallengeDashboardRepository> {
        val userProfileRepo = FakeUserProfileRepo(userProfile)
        return Pair(
            ChallengeDashboardViewModel(
                repository = repository,
                userProfileRepo = userProfileRepo
            ),
            repository
        )
    }

    @Test
    fun `OnShareChallengeClicked sets isShareDialogVisible to true and updates challengeToShare`() = runTest {
        val (viewModel, _) = createViewModel()

        assertFalse(viewModel.state.value.isShareDialogVisible)
        assertNull(viewModel.state.value.challengeToShare)

        viewModel.onAction(ChallengeDashboardAction.OnShareChallengeClicked(dummyChallenge))

        assertTrue(viewModel.state.value.isShareDialogVisible)
        assertEquals(dummyChallenge, viewModel.state.value.challengeToShare)
        assertEquals(dummyChallenge, viewModel.state.value.challenge)
    }

    @Test
    fun `OnDismissShareDialog sets isShareDialogVisible to false and clears challengeToShare`() = runTest {
        val (viewModel, _) = createViewModel()

        viewModel.onAction(ChallengeDashboardAction.OnShareChallengeClicked(dummyChallenge))
        assertTrue(viewModel.state.value.isShareDialogVisible)

        viewModel.onAction(ChallengeDashboardAction.OnDismissShareDialog)
        assertFalse(viewModel.state.value.isShareDialogVisible)
        assertNull(viewModel.state.value.challengeToShare)
        assertNull(viewModel.state.value.challenge)
    }

    @Test
    fun `Initial action loads created and taken challenges successfully`() = runTest {
        val repo = FakeChallengeDashboardRepository().apply {
            createdChallengesResult = CareerPilotResult.Success(listOf(dummyChallenge))
            takenChallengesResult = CareerPilotResult.Success(listOf(dummySession))
        }
        val (viewModel, _) = createViewModel(repository = repo)

        viewModel.onAction(ChallengeDashboardAction.Initial)
        testScheduler.advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
        assertEquals(1, viewModel.state.value.createdChallenges.size)
        assertEquals(dummyChallenge, viewModel.state.value.createdChallenges[0])
        assertEquals(1, viewModel.state.value.takenChallenges.size)
        assertEquals(dummySession, viewModel.state.value.takenChallenges[0])
    }

    @Test
    fun `OnTabSelected updates selectedTab state`() = runTest {
        val (viewModel, _) = createViewModel()

        assertEquals(DashboardTab.MY_CHALLENGES, viewModel.state.value.selectedTab)

        viewModel.onAction(ChallengeDashboardAction.OnTabSelected(DashboardTab.HISTORY))
        assertEquals(DashboardTab.HISTORY, viewModel.state.value.selectedTab)
    }

    @Test
    fun `OnDeleteChallenge and OnConfirmDelete deletes challenge and reloads`() = runTest {
        val repo = FakeChallengeDashboardRepository().apply {
            createdChallengesResult = CareerPilotResult.Success(listOf(dummyChallenge))
        }
        val (viewModel, _) = createViewModel(repository = repo)

        viewModel.onAction(ChallengeDashboardAction.OnDeleteChallenge(dummyChallenge))
        assertEquals(dummyChallenge, viewModel.state.value.challengeToDelete)

        viewModel.onAction(ChallengeDashboardAction.OnConfirmDelete)
        testScheduler.advanceUntilIdle()

        assertEquals("ch_1", repo.lastDeletedChallengeId)
        assertEquals(ChallengeVisibility.PRIVATE, repo.lastDeletedVisibility)
        assertNull(viewModel.state.value.challengeToDelete)
    }

    @Test
    fun `OnDismissDeleteConfirmation clears challengeToDelete`() = runTest {
        val (viewModel, _) = createViewModel()

        viewModel.onAction(ChallengeDashboardAction.OnDeleteChallenge(dummyChallenge))
        assertEquals(dummyChallenge, viewModel.state.value.challengeToDelete)

        viewModel.onAction(ChallengeDashboardAction.OnDismissDeleteConfirmation)
        assertNull(viewModel.state.value.challengeToDelete)
    }

    @Test
    fun `OnEditChallenge emits NavigateToEditChallenge event`() = runTest {
        val (viewModel, _) = createViewModel()
        val events = mutableListOf<ChallengeDashboardEvent>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(events)
        }

        viewModel.onAction(ChallengeDashboardAction.OnEditChallenge("ch_1"))
        testScheduler.advanceUntilIdle()

        assertEquals(listOf(ChallengeDashboardEvent.NavigateToEditChallenge("ch_1")), events)
        job.cancel()
    }

    @Test
    fun `OnBackClicked emits NavigateBack event`() = runTest {
        val (viewModel, _) = createViewModel()
        val events = mutableListOf<ChallengeDashboardEvent>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(events)
        }

        viewModel.onAction(ChallengeDashboardAction.OnBackClicked)
        testScheduler.advanceUntilIdle()

        assertEquals(listOf(ChallengeDashboardEvent.NavigateBack), events)
        job.cancel()
    }
}
