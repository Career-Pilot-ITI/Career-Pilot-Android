package com.iti.careerpilot.profile.presentation.viewmodel

import com.iti.careerpilot.core.network.auth.SessionManager
import com.iti.careerpilot.profile.domain.repo.ProfileRepo
import com.iti.careerpilot.profile.presentation.action.ProfileAction
import com.iti.careerpilot.profile.presentation.event.ProfileEvent
import com.iti.common.error.NetworkError
import com.iti.common.model.ProfileEditSection
import com.iti.common.result.CareerPilotResult
import com.iti.core.datastore.models.AccountInfo
import com.iti.core.datastore.models.CareerInfo
import com.iti.core.datastore.models.PersonalInfo
import com.iti.core.datastore.models.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
class ProfileViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeProfileRepo : ProfileRepo {
        val _userProfile = MutableStateFlow(UserProfile())
        override val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

        var refreshProfileCallCount = 0
        var downloadMissingFilesCallCount = 0
        var clearUserProfileCallCount = 0

        var refreshProfileResult: CareerPilotResult<Unit, NetworkError> = CareerPilotResult.Success(Unit)

        override suspend fun refreshProfile(): CareerPilotResult<Unit, NetworkError> {
            refreshProfileCallCount++
            return refreshProfileResult
        }

        override suspend fun downloadMissingFiles() {
            downloadMissingFilesCallCount++
        }

        override suspend fun clearUserProfile() {
            clearUserProfileCallCount++
            _userProfile.value = UserProfile()
        }
    }

    private class FakeSessionManager : SessionManager {
        var clearSessionCallCount = 0

        override suspend fun onAuthenticated(accessToken: String, refreshToken: String) {}

        override suspend fun clearSession() {
            clearSessionCallCount++
        }
    }

    private lateinit var profileRepo: FakeProfileRepo
    private lateinit var sessionManager: FakeSessionManager

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        profileRepo = FakeProfileRepo()
        sessionManager = FakeSessionManager()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): ProfileViewModel {
        return ProfileViewModel(
            profileRepo = profileRepo,
            sessionManager = sessionManager,
            dispatcherDefault = testDispatcher
        )
    }

    @Test
    fun `init downloads missing files and refreshes profile when profile id is 0`() = runTest {
        val viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        assertEquals(1, profileRepo.downloadMissingFilesCallCount)
        assertEquals(1, profileRepo.refreshProfileCallCount)
    }

    @Test
    fun `init does not refresh profile when profile id is non-zero`() = runTest {
        profileRepo._userProfile.value = UserProfile(id = 42L)

        val viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        assertEquals(1, profileRepo.downloadMissingFilesCallCount)
        assertEquals(0, profileRepo.refreshProfileCallCount)
    }

    @Test
    fun `onAction OnSubscriptionClick emits NavigateToSubscription event`() = runTest {
        val viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        val emittedEvents = mutableListOf<ProfileEvent>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(emittedEvents)
        }

        viewModel.onAction(ProfileAction.OnSubscriptionClick)
        testScheduler.advanceUntilIdle()

        assertEquals(listOf(ProfileEvent.NavigateToSubscription), emittedEvents)
        job.cancel()
    }

    @Test
    fun `onAction OnEditProfileClick emits NavigateToEditProfile event with correct section`() = runTest {
        val viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        val emittedEvents = mutableListOf<ProfileEvent>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(emittedEvents)
        }

        viewModel.onAction(ProfileAction.OnEditProfileClick(ProfileEditSection.CAREER))
        testScheduler.advanceUntilIdle()

        assertEquals(listOf(ProfileEvent.NavigateToEditProfile(ProfileEditSection.CAREER)), emittedEvents)
        job.cancel()
    }

    @Test
    fun `onAction OnSettingsClick emits NavigateToSettings event`() = runTest {
        val viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        val emittedEvents = mutableListOf<ProfileEvent>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(emittedEvents)
        }

        viewModel.onAction(ProfileAction.OnSettingsClick)
        testScheduler.advanceUntilIdle()

        assertEquals(listOf(ProfileEvent.NavigateToSettings), emittedEvents)
        job.cancel()
    }

    @Test
    fun `onAction OnLogoutClick and OnLogoutDismiss update showLogoutDialog state`() = runTest {
        val viewModel = createViewModel()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }
        testScheduler.advanceUntilIdle()

        assertFalse(viewModel.state.value.showLogoutDialog)

        viewModel.onAction(ProfileAction.OnLogoutClick)
        testScheduler.advanceUntilIdle()
        assertTrue(viewModel.state.value.showLogoutDialog)

        viewModel.onAction(ProfileAction.OnLogoutDismiss)
        testScheduler.advanceUntilIdle()
        assertFalse(viewModel.state.value.showLogoutDialog)

        job.cancel()
    }

    @Test
    fun `onAction OnLogoutConfirm clears session, clears profile, and emits NavigateToLogout event`() = runTest {
        val viewModel = createViewModel()
        val stateJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }
        val emittedEvents = mutableListOf<ProfileEvent>()
        val eventJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(emittedEvents)
        }
        testScheduler.advanceUntilIdle()

        viewModel.onAction(ProfileAction.OnLogoutClick)
        testScheduler.advanceUntilIdle()
        assertTrue(viewModel.state.value.showLogoutDialog)

        viewModel.onAction(ProfileAction.OnLogoutConfirm)
        testScheduler.advanceUntilIdle()

        assertFalse(viewModel.state.value.showLogoutDialog)
        assertEquals(1, sessionManager.clearSessionCallCount)
        assertEquals(1, profileRepo.clearUserProfileCallCount)
        assertEquals(listOf(ProfileEvent.NavigateToLogout), emittedEvents)

        stateJob.cancel()
        eventJob.cancel()
    }

    @Test
    fun `onAction OnCVClick emits OpenCV event with provided url`() = runTest {
        val viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        val emittedEvents = mutableListOf<ProfileEvent>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(emittedEvents)
        }

        val testUrl = "https://example.com/cv.pdf"
        viewModel.onAction(ProfileAction.OnCVClick(testUrl))
        testScheduler.advanceUntilIdle()

        assertEquals(listOf(ProfileEvent.OpenCV(testUrl)), emittedEvents)
        job.cancel()
    }
}
