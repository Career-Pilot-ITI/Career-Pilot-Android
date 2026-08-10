package com.iti.careerpilot.home.presentation.ready

import androidx.lifecycle.SavedStateHandle
import com.iti.careerpilot.home.domain.usecase.GetUserProfileUseCase
import com.iti.core.datastore.models.AccountInfo
import com.iti.core.datastore.models.UserProfile
import com.iti.core.datastore.repo.UserProfileRepo
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
    ): ReadyToPracticeViewModel {
        return ReadyToPracticeViewModel(
            savedStateHandle = savedStateHandle,
            getUserProfileUseCase = GetUserProfileUseCase(userProfileRepo),
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
    fun `paid subscriber updates isPaidPlan to true`() = runTest {
        val profile = UserProfile(account = AccountInfo(subscriptionTier = "PLUS"))
        val repo = FakeUserProfileRepo(profile)
        val viewModel = createViewModel(userProfileRepo = repo)
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.state.value.isPaidPlan)
    }

    @Test
    fun `free subscriber updates isPaidPlan to false and resets video mode`() = runTest {
        val profile = UserProfile(account = AccountInfo(subscriptionTier = "FREE"))
        val repo = FakeUserProfileRepo(profile)
        val viewModel = createViewModel(userProfileRepo = repo)
        testScheduler.advanceUntilIdle()

        assertFalse(viewModel.state.value.isPaidPlan)
        assertFalse(viewModel.state.value.isVideoMode)
    }

    @Test
    fun `SelectVideoMode for paid user enables video mode`() = runTest {
        val profile = UserProfile(account = AccountInfo(subscriptionTier = "PRO"))
        val repo = FakeUserProfileRepo(profile)
        val viewModel = createViewModel(userProfileRepo = repo)
        testScheduler.advanceUntilIdle()

        viewModel.onAction(ReadyToPracticeAction.SelectVideoMode)

        assertTrue(viewModel.state.value.isVideoMode)
    }

    @Test
    fun `SelectVideoMode for free user emits NavigateToPaywall event`() = runTest {
        val profile = UserProfile(account = AccountInfo(subscriptionTier = "FREE"))
        val repo = FakeUserProfileRepo(profile)
        val viewModel = createViewModel(userProfileRepo = repo)
        testScheduler.advanceUntilIdle()

        val events = mutableListOf<ReadyToPracticeEvent>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(events)
        }

        viewModel.onAction(ReadyToPracticeAction.SelectVideoMode)
        testScheduler.advanceUntilIdle()

        assertTrue(events.any { it is ReadyToPracticeEvent.NavigateToPaywall })
        assertFalse(viewModel.state.value.isVideoMode)
        job.cancel()
    }

    @Test
    fun `SelectAudioMode disables video mode`() = runTest {
        val profile = UserProfile(account = AccountInfo(subscriptionTier = "MAX"))
        val repo = FakeUserProfileRepo(profile)
        val viewModel = createViewModel(userProfileRepo = repo)
        testScheduler.advanceUntilIdle()

        viewModel.onAction(ReadyToPracticeAction.SelectVideoMode)
        assertTrue(viewModel.state.value.isVideoMode)

        viewModel.onAction(ReadyToPracticeAction.SelectAudioMode)
        assertFalse(viewModel.state.value.isVideoMode)
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
    fun `BeginInterviewClicked emits NavigateToPractice when canBegin is true`() = runTest {
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
        job.cancel()
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
