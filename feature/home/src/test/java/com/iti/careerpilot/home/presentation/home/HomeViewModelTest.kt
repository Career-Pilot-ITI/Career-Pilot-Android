package com.iti.careerpilot.home.presentation.home

import com.iti.careerpilot.core.access.PlanAccessMap
import com.iti.careerpilot.core.access.domain.usecase.RefreshAccessUseCase
import com.iti.careerpilot.core.access.testing.FakeAccessRepository
import com.iti.careerpilot.core.interviews.domain.model.InterviewSession
import com.iti.careerpilot.core.interviews.domain.model.InterviewSessionPage
import com.iti.careerpilot.core.interviews.domain.model.SessionStatus
import com.iti.careerpilot.core.interviews.domain.repository.InterviewSessionRepository
import com.iti.careerpilot.home.domain.model.InterviewTrack
import com.iti.careerpilot.home.domain.repository.InterviewRepository
import com.iti.careerpilot.home.domain.usecase.GetInterviewSessionsUseCase
import com.iti.careerpilot.home.domain.usecase.GetScoreSummaryUseCase
import com.iti.careerpilot.home.domain.usecase.GetTracksUseCase
import com.iti.careerpilot.home.domain.usecase.GetUserProfileUseCase
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import com.iti.core.datastore.models.AccountInfo
import com.iti.core.datastore.models.CareerInfo
import com.iti.core.datastore.models.PersonalInfo
import com.iti.core.datastore.models.UserProfile
import com.iti.core.datastore.repo.UserProfileRepo
import com.iti.core.model.AccessState
import com.iti.core.model.Plan
import kotlin.time.Clock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeUserProfileRepo(initialProfile: UserProfile = UserProfile()) : UserProfileRepo {
        private val _userProfile = MutableStateFlow(initialProfile)
        override val userProfile = _userProfile.asStateFlow()

        override suspend fun updateUserProfile(updateBlock: (UserProfile) -> UserProfile) {
            _userProfile.value = updateBlock(_userProfile.value)
        }

        override suspend fun readUserProfile(): UserProfile = _userProfile.value

        override suspend fun clearUserProfile() {
            _userProfile.value = UserProfile()
        }

        override suspend fun setBodyLanguageConsent(given: Boolean) {
            updateUserProfile { profile ->
                profile.copy(
                    account = profile.account.copy(bodyLanguageConsentGiven = given)
                )
            }
        }
    }

    private class FakeInterviewRepository(
        var tracksResult: CareerPilotResult<List<InterviewTrack>, NetworkError> = CareerPilotResult.Success(emptyList()),
    ) : InterviewRepository {
        override suspend fun getTracks(): CareerPilotResult<List<InterviewTrack>, NetworkError> = tracksResult
    }

    private class FakeInterviewSessionRepository(
        var sessionsResult: CareerPilotResult<InterviewSessionPage, NetworkError> = CareerPilotResult.Success(
            InterviewSessionPage(
                sessions = emptyList(),
                pageNumber = 0,
                totalPages = 0,
                totalElements = 0L,
                isFirst = true,
                isLast = true,
            )
        ),
        var sessionResult: CareerPilotResult<InterviewSession, NetworkError> = CareerPilotResult.Error(NetworkError.NOT_FOUND),
    ) : InterviewSessionRepository {
        override suspend fun getSessions(
            page: Int,
            size: Int,
        ): CareerPilotResult<InterviewSessionPage, NetworkError> = sessionsResult

        override suspend fun getSession(
            sessionId: Long,
        ): CareerPilotResult<InterviewSession, NetworkError> = sessionResult
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
        userProfileRepo: UserProfileRepo = FakeUserProfileRepo(),
        interviewRepository: FakeInterviewRepository = FakeInterviewRepository(),
        interviewSessionRepository: FakeInterviewSessionRepository = FakeInterviewSessionRepository(),
        accessRepository: FakeAccessRepository = FakeAccessRepository(
            AccessState(
                plan = Plan.FREE,
                features = PlanAccessMap.featuresFor(Plan.FREE),
                quotas = emptyMap(),
                expiresAt = null,
                lastSyncedAt = Clock.System.now(),
                coinBalance = 0,
            )
        ),
    ): HomeViewModel {
        return HomeViewModel(
            getUserProfile = GetUserProfileUseCase(userProfileRepo),
            getInterviewSessions = GetInterviewSessionsUseCase(interviewSessionRepository),
            getInterviewTracks = GetTracksUseCase(interviewRepository),
            getScoreSummary = GetScoreSummaryUseCase(),
            refreshAccessUseCase = RefreshAccessUseCase(accessRepository),
            accessRepository = accessRepository,
        )
    }

    @Test
    fun `observing accessState updates plan and coinBalance for FREE tier`() = runTest {
        val fakeAccessRepo = FakeAccessRepository(
            AccessState(
                plan = Plan.FREE,
                features = PlanAccessMap.featuresFor(Plan.FREE),
                quotas = emptyMap(),
                expiresAt = null,
                lastSyncedAt = Clock.System.now(),
                coinBalance = 0,
            )
        )
        val viewModel = createViewModel(accessRepository = fakeAccessRepo)
        testScheduler.advanceUntilIdle()

        assertEquals("FREE", viewModel.state.value.subscriptionTier)
        assertEquals("Free", viewModel.state.value.planLabel)
        assertEquals(0, viewModel.state.value.coins)
        assertFalse(viewModel.state.value.isSubscribed)
    }

    @Test
    fun `observing accessState updates plan and coinBalance for PLUS tier`() = runTest {
        val fakeAccessRepo = FakeAccessRepository(
            AccessState(
                plan = Plan.PLUS,
                features = PlanAccessMap.featuresFor(Plan.PLUS),
                quotas = emptyMap(),
                expiresAt = null,
                lastSyncedAt = Clock.System.now(),
                coinBalance = 150,
            )
        )
        val viewModel = createViewModel(accessRepository = fakeAccessRepo)
        testScheduler.advanceUntilIdle()

        assertEquals("PLUS", viewModel.state.value.subscriptionTier)
        assertEquals("Plus", viewModel.state.value.planLabel)
        assertEquals(150, viewModel.state.value.coins)
        assertTrue(viewModel.state.value.isSubscribed)
    }

    @Test
    fun `observing accessState updates plan and coinBalance for MAX tier`() = runTest {
        val fakeAccessRepo = FakeAccessRepository(
            AccessState(
                plan = Plan.MAX,
                features = PlanAccessMap.featuresFor(Plan.MAX),
                quotas = emptyMap(),
                expiresAt = null,
                lastSyncedAt = Clock.System.now(),
                coinBalance = 500,
            )
        )
        val viewModel = createViewModel(accessRepository = fakeAccessRepo)
        testScheduler.advanceUntilIdle()

        assertEquals("MAX", viewModel.state.value.subscriptionTier)
        assertEquals("Max", viewModel.state.value.planLabel)
        assertEquals(500, viewModel.state.value.coins)
        assertTrue(viewModel.state.value.isSubscribed)
    }

    @Test
    fun `UpgradeClicked when tier is FREE emits NavigateToPlansPaywall with showMySubscription false`() = runTest {
        val fakeAccessRepo = FakeAccessRepository(
            AccessState(
                plan = Plan.FREE,
                features = PlanAccessMap.featuresFor(Plan.FREE),
                quotas = emptyMap(),
                expiresAt = null,
                lastSyncedAt = Clock.System.now(),
                coinBalance = 0,
            )
        )
        val viewModel = createViewModel(accessRepository = fakeAccessRepo)
        testScheduler.advanceUntilIdle()

        val events = mutableListOf<HomeEffect>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(events)
        }

        viewModel.onIntent(HomeIntent.UpgradeClicked)
        testScheduler.advanceUntilIdle()

        val event = events.filterIsInstance<HomeEffect.NavigateToPlansPaywall>().firstOrNull()
        assertTrue("Expected NavigateToPlansPaywall event", event != null)
        assertFalse(event!!.showMySubscription)
        job.cancel()
    }

    @Test
    fun `UpgradeClicked when tier is PLUS emits NavigateToPlansPaywall with showMySubscription false`() = runTest {
        val fakeAccessRepo = FakeAccessRepository(
            AccessState(
                plan = Plan.PLUS,
                features = PlanAccessMap.featuresFor(Plan.PLUS),
                quotas = emptyMap(),
                expiresAt = null,
                lastSyncedAt = Clock.System.now(),
                coinBalance = 100,
            )
        )
        val viewModel = createViewModel(accessRepository = fakeAccessRepo)
        testScheduler.advanceUntilIdle()

        val events = mutableListOf<HomeEffect>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(events)
        }

        viewModel.onIntent(HomeIntent.UpgradeClicked)
        testScheduler.advanceUntilIdle()

        val event = events.filterIsInstance<HomeEffect.NavigateToPlansPaywall>().firstOrNull()
        assertTrue("Expected NavigateToPlansPaywall event", event != null)
        assertFalse(event!!.showMySubscription)
        job.cancel()
    }

    @Test
    fun `UpgradeClicked when tier is MAX emits NavigateToPlansPaywall with showMySubscription true`() = runTest {
        val fakeAccessRepo = FakeAccessRepository(
            AccessState(
                plan = Plan.MAX,
                features = PlanAccessMap.featuresFor(Plan.MAX),
                quotas = emptyMap(),
                expiresAt = null,
                lastSyncedAt = Clock.System.now(),
                coinBalance = 500,
            )
        )
        val viewModel = createViewModel(accessRepository = fakeAccessRepo)
        testScheduler.advanceUntilIdle()

        val events = mutableListOf<HomeEffect>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(events)
        }

        viewModel.onIntent(HomeIntent.UpgradeClicked)
        testScheduler.advanceUntilIdle()

        val event = events.filterIsInstance<HomeEffect.NavigateToPlansPaywall>().firstOrNull()
        assertTrue("Expected NavigateToPlansPaywall event", event != null)
        assertTrue(event!!.showMySubscription)
        job.cancel()
    }

    @Test
    fun `AtsJobMatchClicked when tier is FREE emits NavigateToPlansPaywall with showMySubscription false`() = runTest {
        val fakeAccessRepo = FakeAccessRepository(
            AccessState(
                plan = Plan.FREE,
                features = PlanAccessMap.featuresFor(Plan.FREE),
                quotas = emptyMap(),
                expiresAt = null,
                lastSyncedAt = Clock.System.now(),
                coinBalance = 0,
            )
        )
        val viewModel = createViewModel(accessRepository = fakeAccessRepo)
        testScheduler.advanceUntilIdle()

        val events = mutableListOf<HomeEffect>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(events)
        }

        viewModel.onIntent(HomeIntent.AtsJobMatchClicked)
        testScheduler.advanceUntilIdle()

        val event = events.filterIsInstance<HomeEffect.NavigateToPlansPaywall>().firstOrNull()
        assertTrue("Expected NavigateToPlansPaywall event", event != null)
        assertFalse(event!!.showMySubscription)
        job.cancel()
    }

    @Test
    fun `AtsJobMatchClicked when tier is PLUS emits NavigateToAts`() = runTest {
        val fakeAccessRepo = FakeAccessRepository(
            AccessState(
                plan = Plan.PLUS,
                features = PlanAccessMap.featuresFor(Plan.PLUS),
                quotas = emptyMap(),
                expiresAt = null,
                lastSyncedAt = Clock.System.now(),
                coinBalance = 100,
            )
        )
        val viewModel = createViewModel(accessRepository = fakeAccessRepo)
        testScheduler.advanceUntilIdle()

        val events = mutableListOf<HomeEffect>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(events)
        }

        viewModel.onIntent(HomeIntent.AtsJobMatchClicked)
        testScheduler.advanceUntilIdle()

        val event = events.filterIsInstance<HomeEffect.NavigateToAts>().firstOrNull()
        assertTrue("Expected NavigateToAts event", event != null)
        job.cancel()
    }

    @Test
    fun `AtsJobMatchClicked when tier is MAX emits NavigateToAts`() = runTest {
        val fakeAccessRepo = FakeAccessRepository(
            AccessState(
                plan = Plan.MAX,
                features = PlanAccessMap.featuresFor(Plan.MAX),
                quotas = emptyMap(),
                expiresAt = null,
                lastSyncedAt = Clock.System.now(),
                coinBalance = 500,
            )
        )
        val viewModel = createViewModel(accessRepository = fakeAccessRepo)
        testScheduler.advanceUntilIdle()

        val events = mutableListOf<HomeEffect>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(events)
        }

        viewModel.onIntent(HomeIntent.AtsJobMatchClicked)
        testScheduler.advanceUntilIdle()

        val event = events.filterIsInstance<HomeEffect.NavigateToAts>().firstOrNull()
        assertTrue("Expected NavigateToAts event", event != null)
        job.cancel()
    }

    @Test
    fun `CoinsClicked emits NavigateToCoinsPaywall event`() = runTest {
        val viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        val events = mutableListOf<HomeEffect>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(events)
        }

        viewModel.onIntent(HomeIntent.CoinsClicked)
        testScheduler.advanceUntilIdle()

        assertTrue(events.any { it is HomeEffect.NavigateToCoinsPaywall })
        job.cancel()
    }

    @Test
    fun `PracticeInterviewClicked emits NavigateToReadyToPractice`() = runTest {
        val userProfileRepo = FakeUserProfileRepo(
            UserProfile(
                personal = PersonalInfo(displayName = "Hazem"),
                career = CareerInfo(trackId = 5L, trackName = "Kotlin Multiplatform"),
            )
        )
        val viewModel = createViewModel(userProfileRepo = userProfileRepo)
        testScheduler.advanceUntilIdle()

        val events = mutableListOf<HomeEffect>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(events)
        }

        viewModel.onIntent(HomeIntent.PracticeInterviewClicked)
        testScheduler.advanceUntilIdle()

        val event = events.filterIsInstance<HomeEffect.NavigateToReadyToPractice>().firstOrNull()
        assertTrue("Expected NavigateToReadyToPractice event", event != null)
        assertEquals(5L, event!!.trackId)
        assertEquals("Kotlin Multiplatform", event.trackName)
        job.cancel()
    }

    @Test
    fun `LessonClicked emits NavigateToQuiz`() = runTest {
        val userProfileRepo = FakeUserProfileRepo(
            UserProfile(
                career = CareerInfo(trackId = 7L, trackName = "Backend Go"),
            )
        )
        val viewModel = createViewModel(userProfileRepo = userProfileRepo)
        testScheduler.advanceUntilIdle()

        val events = mutableListOf<HomeEffect>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(events)
        }

        viewModel.onIntent(HomeIntent.LessonClicked)
        testScheduler.advanceUntilIdle()

        val event = events.filterIsInstance<HomeEffect.NavigateToQuiz>().firstOrNull()
        assertTrue("Expected NavigateToQuiz event", event != null)
        assertEquals(7L, event!!.trackId)
        assertEquals("Backend Go", event.trackName)
        job.cancel()
    }

    @Test
    fun `ScoreCardClicked and SeeAllSessionsClicked emit NavigateToReports`() = runTest {
        val viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        val events = mutableListOf<HomeEffect>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(events)
        }

        viewModel.onIntent(HomeIntent.ScoreCardClicked)
        viewModel.onIntent(HomeIntent.SeeAllSessionsClicked)
        testScheduler.advanceUntilIdle()

        assertEquals(2, events.count { it is HomeEffect.NavigateToReports })
        job.cancel()
    }

    @Test
    fun `SeeAllInterviewsClicked emits NavigateToInterviews`() = runTest {
        val viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        val events = mutableListOf<HomeEffect>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(events)
        }

        viewModel.onIntent(HomeIntent.SeeAllInterviewsClicked)
        testScheduler.advanceUntilIdle()

        assertTrue(events.any { it is HomeEffect.NavigateToInterviews })
        job.cancel()
    }

    @Test
    fun `InterviewTrackClicked emits NavigateToReadyToPractice`() = runTest {
        val viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        val events = mutableListOf<HomeEffect>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(events)
        }

        viewModel.onIntent(HomeIntent.InterviewTrackClicked(trackId = 12L, trackName = "AI Engineer"))
        testScheduler.advanceUntilIdle()

        val event = events.filterIsInstance<HomeEffect.NavigateToReadyToPractice>().firstOrNull()
        assertTrue("Expected NavigateToReadyToPractice", event != null)
        assertEquals(12L, event!!.trackId)
        assertEquals("AI Engineer", event.trackName)
        job.cancel()
    }

    @Test
    fun `SessionClicked emits NavigateToSessionDetails`() = runTest {
        val viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        val events = mutableListOf<HomeEffect>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(events)
        }

        viewModel.onIntent(HomeIntent.SessionClicked(sessionId = 42L))
        testScheduler.advanceUntilIdle()

        val event = events.filterIsInstance<HomeEffect.NavigateToSessionDetails>().firstOrNull()
        assertTrue("Expected NavigateToSessionDetails", event != null)
        assertEquals(42L, event!!.sessionId)
        job.cancel()
    }

    @Test
    fun `ResumeSessionClicked emits NavigateToPracticeSession`() = runTest {
        val session = InterviewSession(
            id = 42L,
            trackId = 99L,
            trackName = "Android Developer",
            score = 85,
            durationMinutes = 15,
            occurredAt = null,
            status = SessionStatus.IN_PROGRESS,
            questionCount = 5,
        )
        val sessionPage = InterviewSessionPage(
            sessions = listOf(session),
            pageNumber = 0,
            totalPages = 1,
            totalElements = 1L,
            isFirst = true,
            isLast = true,
        )
        val repo = FakeInterviewSessionRepository(
            sessionsResult = CareerPilotResult.Success(sessionPage)
        )
        val viewModel = createViewModel(interviewSessionRepository = repo)
        testScheduler.advanceUntilIdle()

        val events = mutableListOf<HomeEffect>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(events)
        }

        viewModel.onIntent(HomeIntent.ResumeSessionClicked(sessionId = 42L))
        testScheduler.advanceUntilIdle()

        val event = events.filterIsInstance<HomeEffect.NavigateToPracticeSession>().firstOrNull()
        assertTrue("Expected NavigateToPracticeSession", event != null)
        assertEquals(99L, event!!.trackId)
        assertEquals(42L, event.sessionId)
        job.cancel()
    }
}
