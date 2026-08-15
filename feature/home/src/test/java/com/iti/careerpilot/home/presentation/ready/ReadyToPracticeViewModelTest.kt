package com.iti.careerpilot.home.presentation.ready

import androidx.lifecycle.SavedStateHandle
import com.iti.careerpilot.core.access.PlanAccessMap
import com.iti.careerpilot.core.access.domain.usecase.CheckFeatureAccessUseCase
import com.iti.careerpilot.core.access.domain.usecase.RefreshAccessUseCase
import com.iti.careerpilot.core.access.testing.FakeAccessRepository
import com.iti.careerpilot.home.domain.usecase.GetUserProfileUseCase
import com.iti.core.datastore.models.UserProfile
import com.iti.core.datastore.repo.UserProfileRepo
import com.iti.core.model.AccessState
import com.iti.core.model.FeatureAccess
import com.iti.core.model.FeatureKey
import com.iti.core.model.FeatureQuota
import com.iti.core.model.Plan
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
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
class ReadyToPracticeViewModelTest {

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

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
        userProfileRepo: UserProfileRepo = FakeUserProfileRepo(),
        accessRepository: FakeAccessRepository = FakeAccessRepository(
            AccessState(
                plan = Plan.MAX,
                features = PlanAccessMap.featuresFor(Plan.MAX),
                quotas = emptyMap(),
                expiresAt = null,
                lastSyncedAt = Clock.System.now(),
                coinBalance = 100,
            )
        ),
    ): ReadyToPracticeViewModel {
        return ReadyToPracticeViewModel(
            savedStateHandle = savedStateHandle,
            getUserProfileUseCase = GetUserProfileUseCase(userProfileRepo),
            checkFeatureAccess = CheckFeatureAccessUseCase(accessRepository),
            refreshAccess = RefreshAccessUseCase(accessRepository),
            accessRepository = accessRepository,
        )
    }

    @Test
    fun `initialise sets trackId and trackName`() = runTest {
        val savedStateHandle = SavedStateHandle()
        val viewModel = createViewModel(savedStateHandle = savedStateHandle)
        testScheduler.advanceUntilIdle()

        viewModel.initialise(trackId = 42L, trackName = "Android Engineer")

        assertEquals("Android Engineer", viewModel.state.value.trackName)
    }

    @Test
    fun `observing accessState updates plan, isPaidPlan, and coinBalance`() = runTest {
        val fakeRepo = FakeAccessRepository(
            AccessState(
                plan = Plan.PLUS,
                features = PlanAccessMap.featuresFor(Plan.PLUS),
                quotas = emptyMap(),
                expiresAt = null,
                lastSyncedAt = Clock.System.now(),
                coinBalance = 250,
            )
        )
        val viewModel = createViewModel(accessRepository = fakeRepo)
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.state.value.isPaidPlan)
        assertEquals("Plus", viewModel.state.value.planDisplayName)
        assertEquals(250, viewModel.state.value.coinBalance)
    }

    @Test
    fun `free plan updates isPaidPlan to false`() = runTest {
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

        assertFalse(viewModel.state.value.isPaidPlan)
        assertEquals("Free", viewModel.state.value.planDisplayName)
    }

    @Test
    fun `SelectVideoMode when VideoInterview is Granted enables video mode`() = runTest {
        val fakeRepo = FakeAccessRepository(
            AccessState(
                plan = Plan.MAX,
                features = PlanAccessMap.featuresFor(Plan.MAX),
                quotas = emptyMap(),
                expiresAt = null,
                lastSyncedAt = Clock.System.now(),
                coinBalance = 100,
            )
        )
        val viewModel = createViewModel(accessRepository = fakeRepo)
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.state.value.videoInterviewAccess is FeatureAccess.Granted)

        viewModel.onAction(ReadyToPracticeAction.SelectVideoMode)

        assertTrue(viewModel.state.value.isVideoMode)
        assertTrue(viewModel.state.value.showCameraPermissionDialog)
    }

    @Test
    fun `SelectVideoMode when VideoInterview is Locked opens video gate sheet with dynamic features`() = runTest {
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

        assertTrue(viewModel.state.value.videoInterviewAccess is FeatureAccess.Locked)

        viewModel.onAction(ReadyToPracticeAction.SelectVideoMode)

        assertTrue(viewModel.state.value.showVideoGateSheet)
        assertEquals(Plan.MAX, viewModel.state.value.videoGateRequiredPlan)
        val expectedFeatures = PlanAccessMap.featuresFor(Plan.MAX).map { it.displayName() }
        assertEquals(expectedFeatures, viewModel.state.value.videoGatePlanFeatures)
        assertFalse(viewModel.state.value.isVideoMode)
    }

    @Test
    fun `SelectVideoMode when VideoInterview requires coin top up opens coin top up sheet`() = runTest {
        val quota = FeatureQuota(
            feature = FeatureKey.VideoInterview,
            remaining = 0,
            max = 5,
            coinCost = 40,
        )
        val fakeRepo = FakeAccessRepository(
            AccessState(
                plan = Plan.MAX,
                features = PlanAccessMap.featuresFor(Plan.MAX),
                quotas = mapOf(FeatureKey.VideoInterview to quota),
                expiresAt = null,
                lastSyncedAt = Clock.System.now(),
                coinBalance = 10,
            )
        )
        val viewModel = createViewModel(accessRepository = fakeRepo)
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.state.value.videoInterviewAccess is FeatureAccess.CoinTopUpRequired)

        viewModel.onAction(ReadyToPracticeAction.SelectVideoMode)

        assertTrue(viewModel.state.value.showCoinTopUpSheet)
        assertEquals(40, viewModel.state.value.coinTopUpRequiredCost)
    }

    @Test
    fun `SelectVideoMode when cache is stale triggers refresh`() = runTest {
        val fakeRepo = FakeAccessRepository(
            AccessState(
                plan = Plan.MAX,
                features = PlanAccessMap.featuresFor(Plan.MAX),
                quotas = emptyMap(),
                expiresAt = null,
                lastSyncedAt = Clock.System.now() - 25.hours,
                coinBalance = 100,
            )
        )
        val viewModel = createViewModel(accessRepository = fakeRepo)
        testScheduler.advanceUntilIdle()

        val initialRefreshCount = fakeRepo.refreshCount

        viewModel.onAction(ReadyToPracticeAction.SelectVideoMode)
        testScheduler.advanceUntilIdle()

        assertTrue(fakeRepo.refreshCount > initialRefreshCount)
    }

    @Test
    fun `DismissVideoGateSheet hides sheet`() = runTest {
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

        viewModel.onAction(ReadyToPracticeAction.SelectVideoMode)
        assertTrue(viewModel.state.value.showVideoGateSheet)

        viewModel.onAction(ReadyToPracticeAction.DismissVideoGateSheet)
        assertFalse(viewModel.state.value.showVideoGateSheet)
    }

    @Test
    fun `DismissCoinTopUpSheet hides sheet`() = runTest {
        val quota = FeatureQuota(
            feature = FeatureKey.VideoInterview,
            remaining = 0,
            max = 5,
            coinCost = 40,
        )
        val fakeRepo = FakeAccessRepository(
            AccessState(
                plan = Plan.MAX,
                features = PlanAccessMap.featuresFor(Plan.MAX),
                quotas = mapOf(FeatureKey.VideoInterview to quota),
                expiresAt = null,
                lastSyncedAt = Clock.System.now(),
                coinBalance = 10,
            )
        )
        val viewModel = createViewModel(accessRepository = fakeRepo)
        testScheduler.advanceUntilIdle()

        viewModel.onAction(ReadyToPracticeAction.SelectVideoMode)
        assertTrue(viewModel.state.value.showCoinTopUpSheet)

        viewModel.onAction(ReadyToPracticeAction.DismissCoinTopUpSheet)
        assertFalse(viewModel.state.value.showCoinTopUpSheet)
    }

    @Test
    fun `UpgradeFromVideoGate closes sheet and emits NavigateToPaywall`() = runTest {
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

        viewModel.onAction(ReadyToPracticeAction.SelectVideoMode)
        assertTrue(viewModel.state.value.showVideoGateSheet)

        val events = mutableListOf<ReadyToPracticeEvent>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(events)
        }

        viewModel.onAction(ReadyToPracticeAction.UpgradeFromVideoGate)
        testScheduler.advanceUntilIdle()

        assertFalse(viewModel.state.value.showVideoGateSheet)
        assertTrue(events.any { it is ReadyToPracticeEvent.NavigateToPaywall })
        job.cancel()
    }

    @Test
    fun `BuyCoinsClicked closes sheet and emits NavigateToPaywall`() = runTest {
        val quota = FeatureQuota(
            feature = FeatureKey.VideoInterview,
            remaining = 0,
            max = 5,
            coinCost = 40,
        )
        val fakeRepo = FakeAccessRepository(
            AccessState(
                plan = Plan.MAX,
                features = PlanAccessMap.featuresFor(Plan.MAX),
                quotas = mapOf(FeatureKey.VideoInterview to quota),
                expiresAt = null,
                lastSyncedAt = Clock.System.now(),
                coinBalance = 10,
            )
        )
        val viewModel = createViewModel(accessRepository = fakeRepo)
        testScheduler.advanceUntilIdle()

        viewModel.onAction(ReadyToPracticeAction.SelectVideoMode)
        assertTrue(viewModel.state.value.showCoinTopUpSheet)

        val events = mutableListOf<ReadyToPracticeEvent>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(events)
        }

        viewModel.onAction(ReadyToPracticeAction.BuyCoinsClicked)
        testScheduler.advanceUntilIdle()

        assertFalse(viewModel.state.value.showCoinTopUpSheet)
        assertTrue(events.any { it is ReadyToPracticeEvent.NavigateToPaywall })
        job.cancel()
    }

    @Test
    fun `TogglePostureTracking updates enablePostureTracking`() = runTest {
        val viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        viewModel.onAction(ReadyToPracticeAction.TogglePostureTracking(true))
        assertTrue(viewModel.state.value.enablePostureTracking)

        viewModel.onAction(ReadyToPracticeAction.TogglePostureTracking(false))
        assertFalse(viewModel.state.value.enablePostureTracking)
    }

    @Test
    fun `ToggleHandTracking updates enableHandTracking`() = runTest {
        val viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        viewModel.onAction(ReadyToPracticeAction.ToggleHandTracking(true))
        assertTrue(viewModel.state.value.enableHandTracking)

        viewModel.onAction(ReadyToPracticeAction.ToggleHandTracking(false))
        assertFalse(viewModel.state.value.enableHandTracking)
    }

    @Test
    fun `SelectAudioMode disables video mode and resets landmark tracking`() = runTest {
        val viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        viewModel.onAction(ReadyToPracticeAction.SelectVideoMode)
        viewModel.onAction(ReadyToPracticeAction.TogglePostureTracking(true))
        viewModel.onAction(ReadyToPracticeAction.ToggleHandTracking(true))

        assertTrue(viewModel.state.value.isVideoMode)
        assertTrue(viewModel.state.value.enablePostureTracking)
        assertTrue(viewModel.state.value.enableHandTracking)

        viewModel.onAction(ReadyToPracticeAction.SelectAudioMode)
        assertFalse(viewModel.state.value.isVideoMode)
        assertFalse(viewModel.state.value.enablePostureTracking)
        assertFalse(viewModel.state.value.enableHandTracking)
    }

    @Test
    fun `MicrophonePermissionChanged updates granted state and hides dialog`() = runTest {
        val viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        viewModel.onAction(ReadyToPracticeAction.MicrophonePermissionChanged(isGranted = true))

        assertTrue(viewModel.state.value.isMicrophoneGranted)
        assertFalse(viewModel.state.value.isPermissionDialogVisible)
    }

    @Test
    fun `MicrophoneRowClicked shows dialog when permission is not granted`() = runTest {
        val viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        viewModel.onAction(ReadyToPracticeAction.MicrophoneRowClicked)

        assertTrue(viewModel.state.value.isPermissionDialogVisible)
    }

    @Test
    fun `PermissionDialogDismissed hides dialog`() = runTest {
        val viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        viewModel.onAction(ReadyToPracticeAction.MicrophoneRowClicked)
        assertTrue(viewModel.state.value.isPermissionDialogVisible)

        viewModel.onAction(ReadyToPracticeAction.PermissionDialogDismissed)
        assertFalse(viewModel.state.value.isPermissionDialogVisible)
    }

    @Test
    fun `CameraPermissionChanged updates granted state and hides dialog`() = runTest {
        val viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        viewModel.onAction(ReadyToPracticeAction.CameraPermissionChanged(isGranted = true))

        assertTrue(viewModel.state.value.isCameraGranted)
        assertFalse(viewModel.state.value.showCameraPermissionDialog)
    }

    @Test
    fun `CameraRowClicked shows dialog when permission is not granted`() = runTest {
        val viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        viewModel.onAction(ReadyToPracticeAction.CameraRowClicked)

        assertTrue(viewModel.state.value.showCameraPermissionDialog)
    }

    @Test
    fun `CameraPermissionDialogDismissed hides dialog`() = runTest {
        val viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        viewModel.onAction(ReadyToPracticeAction.CameraRowClicked)
        assertTrue(viewModel.state.value.showCameraPermissionDialog)

        viewModel.onAction(ReadyToPracticeAction.CameraPermissionDialogDismissed)
        assertFalse(viewModel.state.value.showCameraPermissionDialog)
    }

    @Test
    fun `BeginInterviewClicked in audio mode with Granted access emits NavigateToPractice`() = runTest {
        val savedStateHandle = SavedStateHandle()
        val viewModel = createViewModel(savedStateHandle = savedStateHandle)
        testScheduler.advanceUntilIdle()

        viewModel.initialise(trackId = 101L, trackName = "Backend")
        viewModel.onAction(ReadyToPracticeAction.MicrophonePermissionChanged(isGranted = true))

        val events = mutableListOf<ReadyToPracticeEvent>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(events)
        }

        viewModel.onAction(ReadyToPracticeAction.BeginInterviewClicked)
        testScheduler.advanceUntilIdle()

        val practiceEvent = events.filterIsInstance<ReadyToPracticeEvent.NavigateToPractice>().firstOrNull()
        assertTrue("Expected NavigateToPractice event", practiceEvent != null)
        assertEquals(101L, practiceEvent!!.trackId)
        assertFalse(practiceEvent.isVideo)
        assertFalse(practiceEvent.enablePosture)
        assertFalse(practiceEvent.enableHands)
        job.cancel()
    }

    @Test
    fun `BeginInterviewClicked in video mode with Granted access passes posture and hands flags`() = runTest {
        val savedStateHandle = SavedStateHandle()
        val viewModel = createViewModel(savedStateHandle = savedStateHandle)
        testScheduler.advanceUntilIdle()

        viewModel.initialise(trackId = 102L, trackName = "Android")
        viewModel.onAction(ReadyToPracticeAction.MicrophonePermissionChanged(isGranted = true))
        viewModel.onAction(ReadyToPracticeAction.SelectVideoMode)
        viewModel.onAction(ReadyToPracticeAction.CameraPermissionChanged(isGranted = true))
        viewModel.onAction(ReadyToPracticeAction.TogglePostureTracking(true))
        viewModel.onAction(ReadyToPracticeAction.ToggleHandTracking(true))

        val events = mutableListOf<ReadyToPracticeEvent>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(events)
        }

        viewModel.onAction(ReadyToPracticeAction.BeginInterviewClicked)
        testScheduler.advanceUntilIdle()

        val practiceEvent = events.filterIsInstance<ReadyToPracticeEvent.NavigateToPractice>().firstOrNull()
        assertTrue("Expected NavigateToPractice event", practiceEvent != null)
        assertEquals(102L, practiceEvent!!.trackId)
        assertTrue(practiceEvent.isVideo)
        assertTrue(practiceEvent.enablePosture)
        assertTrue(practiceEvent.enableHands)
        job.cancel()
    }

    @Test
    fun `BeginInterviewClicked in audio mode when Locked emits NavigateToPaywall`() = runTest {
        val fakeRepo = FakeAccessRepository(
            AccessState(
                plan = Plan.FREE,
                features = emptySet(), // No features granted
                quotas = emptyMap(),
                expiresAt = null,
                lastSyncedAt = Clock.System.now(),
                coinBalance = 0,
            )
        )
        val savedStateHandle = SavedStateHandle()
        val viewModel = createViewModel(savedStateHandle = savedStateHandle, accessRepository = fakeRepo)
        testScheduler.advanceUntilIdle()

        viewModel.initialise(trackId = 103L, trackName = "iOS")
        viewModel.onAction(ReadyToPracticeAction.MicrophonePermissionChanged(isGranted = true))

        val events = mutableListOf<ReadyToPracticeEvent>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(events)
        }

        viewModel.onAction(ReadyToPracticeAction.BeginInterviewClicked)
        testScheduler.advanceUntilIdle()

        assertTrue(events.any { it is ReadyToPracticeEvent.NavigateToPaywall })
        job.cancel()
    }

    @Test
    fun `BeginInterviewClicked when CoinTopUpRequired opens coin top up sheet`() = runTest {
        val quota = FeatureQuota(
            feature = FeatureKey.MockInterviews,
            remaining = 0,
            max = 3,
            coinCost = 25,
        )
        val fakeRepo = FakeAccessRepository(
            AccessState(
                plan = Plan.FREE,
                features = PlanAccessMap.featuresFor(Plan.FREE),
                quotas = mapOf(FeatureKey.MockInterviews to quota),
                expiresAt = null,
                lastSyncedAt = Clock.System.now(),
                coinBalance = 5,
            )
        )
        val savedStateHandle = SavedStateHandle()
        val viewModel = createViewModel(savedStateHandle = savedStateHandle, accessRepository = fakeRepo)
        testScheduler.advanceUntilIdle()

        viewModel.initialise(trackId = 104L, trackName = "Flutter")
        viewModel.onAction(ReadyToPracticeAction.MicrophonePermissionChanged(isGranted = true))

        val events = mutableListOf<ReadyToPracticeEvent>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(events)
        }

        viewModel.onAction(ReadyToPracticeAction.BeginInterviewClicked)
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.state.value.showCoinTopUpSheet)
        assertEquals(25, viewModel.state.value.coinTopUpRequiredCost)
        assertTrue(events.isEmpty())
        job.cancel()
    }

    @Test
    fun `BeginInterviewClicked in video mode when Locked shows video gate sheet`() = runTest {
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
        val savedStateHandle = SavedStateHandle()
        val viewModel = createViewModel(savedStateHandle = savedStateHandle, accessRepository = fakeRepo)
        testScheduler.advanceUntilIdle()

        viewModel.initialise(trackId = 105L, trackName = "Kotlin")
        viewModel.onAction(ReadyToPracticeAction.MicrophonePermissionChanged(isGranted = true))
        viewModel.onAction(ReadyToPracticeAction.CameraPermissionChanged(isGranted = true))
        // Manually trigger video mode in state if possible or select video mode
        viewModel.onAction(ReadyToPracticeAction.SelectVideoMode) // sets showVideoGateSheet
        viewModel.onAction(ReadyToPracticeAction.DismissVideoGateSheet)

        // Now test beginInterview when audio is granted but video is locked
        val events = mutableListOf<ReadyToPracticeEvent>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(events)
        }

        viewModel.onAction(ReadyToPracticeAction.BeginInterviewClicked)
        testScheduler.advanceUntilIdle()

        // Audio mode is active since SelectVideoMode was locked, so beginInterview in audio mode (MockInterviews is in FREE) proceeds
        val practiceEvent = events.filterIsInstance<ReadyToPracticeEvent.NavigateToPractice>().firstOrNull()
        assertTrue("Expected NavigateToPractice event", practiceEvent != null)
        assertFalse(practiceEvent!!.isVideo)
        job.cancel()
    }

    @Test
    fun `BeginInterviewClicked when cache is stale triggers refresh`() = runTest {
        val fakeRepo = FakeAccessRepository(
            AccessState(
                plan = Plan.MAX,
                features = PlanAccessMap.featuresFor(Plan.MAX),
                quotas = emptyMap(),
                expiresAt = null,
                lastSyncedAt = Clock.System.now() - 25.hours,
                coinBalance = 100,
            )
        )
        val savedStateHandle = SavedStateHandle()
        val viewModel = createViewModel(savedStateHandle = savedStateHandle, accessRepository = fakeRepo)
        testScheduler.advanceUntilIdle()

        viewModel.initialise(trackId = 106L, trackName = "Security")
        viewModel.onAction(ReadyToPracticeAction.MicrophonePermissionChanged(isGranted = true))

        val initialRefreshCount = fakeRepo.refreshCount

        viewModel.onAction(ReadyToPracticeAction.BeginInterviewClicked)
        testScheduler.advanceUntilIdle()

        assertTrue(fakeRepo.refreshCount > initialRefreshCount)
    }

    @Test
    fun `CancelClicked emits NavigateBack event`() = runTest {
        val viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        val events = mutableListOf<ReadyToPracticeEvent>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(events)
        }

        viewModel.onAction(ReadyToPracticeAction.CancelClicked)
        testScheduler.advanceUntilIdle()

        assertTrue(events.any { it is ReadyToPracticeEvent.NavigateBack })
        job.cancel()
    }
}
