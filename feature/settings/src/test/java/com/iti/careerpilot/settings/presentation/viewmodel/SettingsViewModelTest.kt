package com.iti.careerpilot.settings.presentation.viewmodel

import com.iti.careerpilot.core.access.testing.FakeAccessRepository
import com.iti.careerpilot.settings.presentation.action.SettingsIntent
import com.iti.careerpilot.settings.presentation.event.SettingsEffect
import com.iti.core.datastore.settings.domain.UserSettingsRepo
import com.iti.core.datastore.settings.domain.models.ThemeSetting
import com.iti.core.datastore.settings.domain.models.UserSettings
import com.iti.core.model.AccessState
import com.iti.core.model.Plan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
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
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeUserSettingsRepo(initialSettings: UserSettings = UserSettings()) : UserSettingsRepo {
        private val _settingsFlow = MutableStateFlow(initialSettings)
        override val settingsFlow: Flow<UserSettings> = _settingsFlow.asStateFlow()

        override suspend fun updateUserSettings(updateBlock: (UserSettings) -> UserSettings) {
            _settingsFlow.value = updateBlock(_settingsFlow.value)
        }

        fun currentSettings(): UserSettings = _settingsFlow.value
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
        userSettingsRepo: UserSettingsRepo = FakeUserSettingsRepo(),
        accessRepository: FakeAccessRepository = FakeAccessRepository(
            AccessState(
                plan = Plan.FREE,
                features = emptySet(),
                quotas = emptyMap(),
                expiresAt = null,
                lastSyncedAt = null,
                coinBalance = 0,
            )
        ),
    ): SettingsViewModel {
        return SettingsViewModel(
            settingsRepo = userSettingsRepo,
            accessRepository = accessRepository,
        )
    }

    @Test
    fun `observing accessState updates planDisplayName, isMaxPlan, and coinBalance for FREE tier`() = runTest {
        val fakeAccessRepo = FakeAccessRepository(
            AccessState(
                plan = Plan.FREE,
                features = emptySet(),
                quotas = emptyMap(),
                expiresAt = null,
                lastSyncedAt = null,
                coinBalance = 0,
            )
        )
        val viewModel = createViewModel(accessRepository = fakeAccessRepo)
        testScheduler.advanceUntilIdle()

        assertEquals("Free", viewModel.state.value.planDisplayName)
        assertFalse(viewModel.state.value.isMaxPlan)
        assertEquals(0, viewModel.state.value.coinBalance)
    }

    @Test
    fun `observing accessState updates planDisplayName, isMaxPlan, and coinBalance for PLUS tier`() = runTest {
        val fakeAccessRepo = FakeAccessRepository(
            AccessState(
                plan = Plan.PLUS,
                features = emptySet(),
                quotas = emptyMap(),
                expiresAt = null,
                lastSyncedAt = null,
                coinBalance = 150,
            )
        )
        val viewModel = createViewModel(accessRepository = fakeAccessRepo)
        testScheduler.advanceUntilIdle()

        assertEquals("Plus", viewModel.state.value.planDisplayName)
        assertFalse(viewModel.state.value.isMaxPlan)
        assertEquals(150, viewModel.state.value.coinBalance)
    }

    @Test
    fun `observing accessState updates planDisplayName, isMaxPlan, and coinBalance for MAX tier`() = runTest {
        val fakeAccessRepo = FakeAccessRepository(
            AccessState(
                plan = Plan.MAX,
                features = emptySet(),
                quotas = emptyMap(),
                expiresAt = null,
                lastSyncedAt = null,
                coinBalance = 500,
            )
        )
        val viewModel = createViewModel(accessRepository = fakeAccessRepo)
        testScheduler.advanceUntilIdle()

        assertEquals("Max", viewModel.state.value.planDisplayName)
        assertTrue(viewModel.state.value.isMaxPlan)
        assertEquals(500, viewModel.state.value.coinBalance)
    }

    @Test
    fun `dynamic accessState update updates SettingsState accordingly`() = runTest {
        val fakeAccessRepo = FakeAccessRepository(
            AccessState(
                plan = Plan.FREE,
                features = emptySet(),
                quotas = emptyMap(),
                expiresAt = null,
                lastSyncedAt = null,
                coinBalance = 0,
            )
        )
        val viewModel = createViewModel(accessRepository = fakeAccessRepo)
        testScheduler.advanceUntilIdle()

        assertEquals("Free", viewModel.state.value.planDisplayName)
        assertFalse(viewModel.state.value.isMaxPlan)
        assertEquals(0, viewModel.state.value.coinBalance)

        fakeAccessRepo.mutableState.value = AccessState(
                plan = Plan.MAX,
                features = emptySet(),
                quotas = emptyMap(),
                expiresAt = null,
                lastSyncedAt = null,
                coinBalance = 320,
            )
        testScheduler.advanceUntilIdle()

        assertEquals("Max", viewModel.state.value.planDisplayName)
        assertTrue(viewModel.state.value.isMaxPlan)
        assertEquals(320, viewModel.state.value.coinBalance)
    }

    @Test
    fun `ManageSubscriptionClicked when tier is FREE emits NavigateToPaywall with showMySubscription false`() = runTest {
        val fakeAccessRepo = FakeAccessRepository(
            AccessState(
                plan = Plan.FREE,
                features = emptySet(),
                quotas = emptyMap(),
                expiresAt = null,
                lastSyncedAt = null,
                coinBalance = 0,
            )
        )
        val viewModel = createViewModel(accessRepository = fakeAccessRepo)
        testScheduler.advanceUntilIdle()

        val events = mutableListOf<SettingsEffect>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(events)
        }

        viewModel.onIntent(SettingsIntent.ManageSubscriptionClicked)
        testScheduler.advanceUntilIdle()

        val event = events.filterIsInstance<SettingsEffect.NavigateToPaywall>().firstOrNull()
        assertTrue("Expected NavigateToPaywall event", event != null)
        assertFalse(event!!.showGetCoins)
        assertFalse(event.showMySubscription)
        job.cancel()
    }

    @Test
    fun `ManageSubscriptionClicked when tier is PLUS emits NavigateToPaywall with showMySubscription false`() = runTest {
        val fakeAccessRepo = FakeAccessRepository(
            AccessState(
                plan = Plan.PLUS,
                features = emptySet(),
                quotas = emptyMap(),
                expiresAt = null,
                lastSyncedAt = null,
                coinBalance = 100,
            )
        )
        val viewModel = createViewModel(accessRepository = fakeAccessRepo)
        testScheduler.advanceUntilIdle()

        val events = mutableListOf<SettingsEffect>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(events)
        }

        viewModel.onIntent(SettingsIntent.ManageSubscriptionClicked)
        testScheduler.advanceUntilIdle()

        val event = events.filterIsInstance<SettingsEffect.NavigateToPaywall>().firstOrNull()
        assertTrue("Expected NavigateToPaywall event", event != null)
        assertFalse(event!!.showGetCoins)
        assertFalse(event.showMySubscription)
        job.cancel()
    }

    @Test
    fun `ManageSubscriptionClicked when tier is MAX emits NavigateToPaywall with showMySubscription true`() = runTest {
        val fakeAccessRepo = FakeAccessRepository(
            AccessState(
                plan = Plan.MAX,
                features = emptySet(),
                quotas = emptyMap(),
                expiresAt = null,
                lastSyncedAt = null,
                coinBalance = 500,
            )
        )
        val viewModel = createViewModel(accessRepository = fakeAccessRepo)
        testScheduler.advanceUntilIdle()

        val events = mutableListOf<SettingsEffect>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(events)
        }

        viewModel.onIntent(SettingsIntent.ManageSubscriptionClicked)
        testScheduler.advanceUntilIdle()

        val event = events.filterIsInstance<SettingsEffect.NavigateToPaywall>().firstOrNull()
        assertTrue("Expected NavigateToPaywall event", event != null)
        assertFalse(event!!.showGetCoins)
        assertTrue(event.showMySubscription)
        job.cancel()
    }

    @Test
    fun `CoinsClicked emits NavigateToPaywall with showGetCoins true and showMySubscription false`() = runTest {
        val viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        val events = mutableListOf<SettingsEffect>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(events)
        }

        viewModel.onIntent(SettingsIntent.CoinsClicked)
        testScheduler.advanceUntilIdle()

        val event = events.filterIsInstance<SettingsEffect.NavigateToPaywall>().firstOrNull()
        assertTrue("Expected NavigateToPaywall event", event != null)
        assertTrue(event!!.showGetCoins)
        assertFalse(event.showMySubscription)
        job.cancel()
    }

    @Test
    fun `LanguageDialogToggle updates showLanguageDialog state`() = runTest {
        val viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        assertFalse(viewModel.state.value.showLanguageDialog)

        viewModel.onIntent(SettingsIntent.LanguageDialogToggle(open = true))
        testScheduler.advanceUntilIdle()
        assertTrue(viewModel.state.value.showLanguageDialog)

        viewModel.onIntent(SettingsIntent.LanguageDialogToggle(open = false))
        testScheduler.advanceUntilIdle()
        assertFalse(viewModel.state.value.showLanguageDialog)
    }

    @Test
    fun `ThemeDialogToggle updates showThemeDialog state`() = runTest {
        val viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        assertFalse(viewModel.state.value.showThemeDialog)

        viewModel.onIntent(SettingsIntent.ThemeDialogToggle(open = true))
        testScheduler.advanceUntilIdle()
        assertTrue(viewModel.state.value.showThemeDialog)

        viewModel.onIntent(SettingsIntent.ThemeDialogToggle(open = false))
        testScheduler.advanceUntilIdle()
        assertFalse(viewModel.state.value.showThemeDialog)
    }

    @Test
    fun `UpdateTheme updates UserSettingsRepo`() = runTest {
        val fakeUserSettingsRepo = FakeUserSettingsRepo()
        val viewModel = createViewModel(userSettingsRepo = fakeUserSettingsRepo)
        testScheduler.advanceUntilIdle()

        viewModel.onIntent(SettingsIntent.UpdateTheme(ThemeSetting.DARK))
        testScheduler.advanceUntilIdle()

        assertEquals(ThemeSetting.DARK, fakeUserSettingsRepo.currentSettings().theme)
    }
}
