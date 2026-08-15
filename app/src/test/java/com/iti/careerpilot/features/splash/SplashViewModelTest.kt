package com.iti.careerpilot.features.splash

import androidx.datastore.core.DataStore
import com.iti.careerpilot.core.access.domain.AccessRepository
import com.iti.careerpilot.core.access.domain.usecase.RefreshAccessUseCase
import com.iti.core.datastore.UserTokens
import com.iti.core.datastore.UserTokensRepo
import com.iti.core.datastore.models.UserProfile
import com.iti.core.datastore.repo.UserProfileRepo
import com.iti.core.model.AccessState
import com.iti.core.model.FeatureKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeDataStore<T>(initialValue: T) : DataStore<T> {
        private val _data = MutableStateFlow(initialValue)
        override val data: Flow<T> = _data

        override suspend fun updateData(transform: suspend (t: T) -> T): T {
            val updated = transform(_data.value)
            _data.value = updated
            return updated
        }
    }

    private class FakeUserProfileRepo : UserProfileRepo {
        val _userProfile = MutableStateFlow(UserProfile())
        override val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

        override suspend fun readUserProfile(): UserProfile = _userProfile.value

        override suspend fun updateUserProfile(updateBlock: (UserProfile) -> UserProfile) {
            _userProfile.value = updateBlock(_userProfile.value)
        }

        override suspend fun clearUserProfile() {
            _userProfile.value = UserProfile()
        }

        override suspend fun setBodyLanguageConsent(given: Boolean) {}
    }

    private class FakeAccessRepository : AccessRepository {
        var refreshCallCount = 0
        var refreshResult: Result<Unit> = Result.success(Unit)

        private val _accessState = MutableStateFlow(AccessState.Free)
        override val accessState: StateFlow<AccessState> = _accessState.asStateFlow()

        override suspend fun refresh(): Result<Unit> {
            refreshCallCount++
            return refreshResult
        }

        override suspend fun deductCoins(amount: Int) {}
        override fun hasAccess(feature: FeatureKey): Boolean = true
        override suspend fun clear() {}
    }

    private lateinit var dataStore: FakeDataStore<UserTokens>
    private lateinit var userTokensRepo: UserTokensRepo
    private lateinit var userProfileRepo: FakeUserProfileRepo
    private lateinit var accessRepository: FakeAccessRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        dataStore = FakeDataStore(UserTokens())
        userTokensRepo = UserTokensRepo(dataStore, CoroutineScope(testDispatcher))
        userProfileRepo = FakeUserProfileRepo()
        accessRepository = FakeAccessRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): SplashViewModel {
        return SplashViewModel(
            userProfileRepo = userProfileRepo,
            userTokensRepo = userTokensRepo,
            refreshAccessUseCase = RefreshAccessUseCase(accessRepository),
        )
    }

    @Test
    fun `when no accessToken present, navigates to Login and does not refresh access`() = runTest {
        val viewModel = createViewModel()
        val emittedEvents = mutableListOf<SplashEvent>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.navigationEvent.toList(emittedEvents)
        }

        viewModel.onIntent(SplashIntent.Initial)
        testScheduler.advanceUntilIdle()

        assertEquals(0, accessRepository.refreshCallCount)
        assertEquals(listOf(SplashEvent.NavigateToLogin), emittedEvents)
        job.cancel()
    }

    @Test
    fun `when accessToken present and onboarding completed, refreshes access and navigates to Home`() = runTest {
        dataStore.updateData { it.copy(accessToken = "valid_token", refreshToken = "refresh_token") }
        userProfileRepo._userProfile.value = UserProfile(onboardingCompleted = true)

        val viewModel = createViewModel()
        val emittedEvents = mutableListOf<SplashEvent>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.navigationEvent.toList(emittedEvents)
        }

        viewModel.onIntent(SplashIntent.Initial)
        testScheduler.advanceUntilIdle()

        assertEquals(1, accessRepository.refreshCallCount)
        assertEquals(listOf(SplashEvent.NavigateToHome), emittedEvents)
        job.cancel()
    }

    @Test
    fun `when accessToken present and onboarding not completed, refreshes access and navigates to Onboarding`() = runTest {
        dataStore.updateData { it.copy(accessToken = "valid_token", refreshToken = "refresh_token") }
        userProfileRepo._userProfile.value = UserProfile(onboardingCompleted = false)

        val viewModel = createViewModel()
        val emittedEvents = mutableListOf<SplashEvent>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.navigationEvent.toList(emittedEvents)
        }

        viewModel.onIntent(SplashIntent.Initial)
        testScheduler.advanceUntilIdle()

        assertEquals(1, accessRepository.refreshCallCount)
        assertEquals(listOf(SplashEvent.NavigateToOnboarding), emittedEvents)
        job.cancel()
    }

    @Test
    fun `when accessToken present and refresh access fails, still navigates gracefully`() = runTest {
        dataStore.updateData { it.copy(accessToken = "valid_token", refreshToken = "refresh_token") }
        userProfileRepo._userProfile.value = UserProfile(onboardingCompleted = true)
        accessRepository.refreshResult = Result.failure(Exception("Network failure"))

        val viewModel = createViewModel()
        val emittedEvents = mutableListOf<SplashEvent>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.navigationEvent.toList(emittedEvents)
        }

        viewModel.onIntent(SplashIntent.Initial)
        testScheduler.advanceUntilIdle()

        assertEquals(1, accessRepository.refreshCallCount)
        assertEquals(listOf(SplashEvent.NavigateToHome), emittedEvents)
        job.cancel()
    }
}
