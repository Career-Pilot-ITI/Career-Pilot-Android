package com.iti.careerpilot.features.paywall.presentation.viewmodel

import com.iti.careerpilot.core.access.PlanAccessMap
import com.iti.careerpilot.core.access.domain.AccessRepository
import com.iti.careerpilot.core.access.domain.usecase.RefreshAccessUseCase
import com.iti.careerpilot.features.paywall.domain.repository.PaymentRepository
import com.iti.careerpilot.features.paywall.domain.usecase.CancelSubscriptionUseCase
import com.iti.careerpilot.features.paywall.domain.usecase.GetCurrentSubscriptionUseCase
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import com.iti.core.model.AccessState
import com.iti.core.model.CheckoutSession
import com.iti.core.model.FeatureKey
import com.iti.core.model.Plan
import com.iti.core.model.SubscriptionInfo
import com.iti.core.model.WalletBalance
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MySubscriptionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakePaymentRepository : PaymentRepository {
        var currentSubscriptionResult: CareerPilotResult<SubscriptionInfo, NetworkError> =
            CareerPilotResult.Success(
                SubscriptionInfo(
                    tier = "PLUS",
                    isActive = true,
                    startedAt = "2026-01-01",
                    renewalDate = "2026-02-01",
                    cancelledAt = null,
                    pendingTier = null
                )
            )

        var cancelSubscriptionResult: CareerPilotResult<Unit, NetworkError> =
            CareerPilotResult.Success(Unit)

        var getCurrentSubscriptionCallCount = 0
        var cancelSubscriptionCallCount = 0

        override suspend fun getWalletBalance(): CareerPilotResult<WalletBalance, NetworkError> =
            CareerPilotResult.Success(WalletBalance(balance = 100))

        override suspend fun topUpWallet(
            coinPackSize: Int,
            currency: String,
            method: String
        ): CareerPilotResult<CheckoutSession, NetworkError> =
            CareerPilotResult.Error(NetworkError.UNKNOWN)

        override suspend fun initiatePayment(
            amount: Double,
            currency: String,
            method: String,
            provider: String,
            purchaseType: String,
            coinPackSize: Int?,
            tier: String?
        ): CareerPilotResult<CheckoutSession, NetworkError> =
            CareerPilotResult.Error(NetworkError.UNKNOWN)

        override suspend fun getCurrentSubscription(): CareerPilotResult<SubscriptionInfo, NetworkError> {
            getCurrentSubscriptionCallCount++
            return currentSubscriptionResult
        }

        override suspend fun upgradeSubscription(
            tier: String,
            currency: String,
            method: String
        ): CareerPilotResult<CheckoutSession, NetworkError> =
            CareerPilotResult.Error(NetworkError.UNKNOWN)

        override suspend fun downgradeSubscription(tier: String): CareerPilotResult<Unit, NetworkError> =
            CareerPilotResult.Error(NetworkError.UNKNOWN)

        override suspend fun cancelSubscription(): CareerPilotResult<Unit, NetworkError> {
            cancelSubscriptionCallCount++
            return cancelSubscriptionResult
        }

        override suspend fun getSubscriptionTiers(): CareerPilotResult<Map<String, Double>, NetworkError> =
            CareerPilotResult.Success(emptyMap())

        override suspend fun getCoinPacks(): CareerPilotResult<Map<Int, Double>, NetworkError> =
            CareerPilotResult.Success(emptyMap())

        override suspend fun confirmPayment(merchantOrderId: String): CareerPilotResult<Unit, NetworkError> =
            CareerPilotResult.Success(Unit)
    }

    private class FakeAccessRepository : AccessRepository {
        var refreshResult: Result<Unit> = Result.success(Unit)
        var refreshCallCount = 0

        private val _accessState = MutableStateFlow(AccessState.Free)
        override val accessState: StateFlow<AccessState> = _accessState.asStateFlow()

        override suspend fun refresh(): Result<Unit> {
            refreshCallCount++
            return refreshResult
        }

        override fun hasAccess(feature: FeatureKey): Boolean = true
        override suspend fun clear() {}
    }

    private lateinit var paymentRepository: FakePaymentRepository
    private lateinit var accessRepository: FakeAccessRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        paymentRepository = FakePaymentRepository()
        accessRepository = FakeAccessRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): MySubscriptionViewModel {
        return MySubscriptionViewModel(
            getCurrentSubscriptionUseCase = GetCurrentSubscriptionUseCase(paymentRepository),
            cancelSubscriptionUseCase = CancelSubscriptionUseCase(paymentRepository),
            refreshAccessUseCase = RefreshAccessUseCase(accessRepository),
        )
    }

    @Test
    fun `init loads subscription and maps FREE tier to Plan FREE features`() = runTest {
        paymentRepository.currentSubscriptionResult = CareerPilotResult.Success(
            SubscriptionInfo(
                tier = "FREE",
                isActive = true,
                startedAt = null,
                renewalDate = null,
                cancelledAt = null,
                pendingTier = null
            )
        )

        val viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals("FREE", state.subscriptionInfo?.tier)

        val expectedFeatures = PlanAccessMap.featuresFor(Plan.FREE).map { it.displayName() }
        assertEquals(expectedFeatures, state.planFeatures)
    }

    @Test
    fun `init loads subscription and maps PLUS tier to Plan PLUS features`() = runTest {
        paymentRepository.currentSubscriptionResult = CareerPilotResult.Success(
            SubscriptionInfo(
                tier = "PLUS",
                isActive = true,
                startedAt = "2026-01-01",
                renewalDate = "2026-02-01",
                cancelledAt = null,
                pendingTier = null
            )
        )

        val viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals("PLUS", state.subscriptionInfo?.tier)

        val expectedFeatures = PlanAccessMap.featuresFor(Plan.PLUS).map { it.displayName() }
        assertEquals(expectedFeatures, state.planFeatures)
        assertTrue(state.planFeatures.contains(FeatureKey.Quizzes.displayName()))
    }

    @Test
    fun `init loads subscription and maps MAX tier to Plan MAX features`() = runTest {
        paymentRepository.currentSubscriptionResult = CareerPilotResult.Success(
            SubscriptionInfo(
                tier = "MAX",
                isActive = true,
                startedAt = "2026-01-01",
                renewalDate = "2026-02-01",
                cancelledAt = null,
                pendingTier = null
            )
        )

        val viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals("MAX", state.subscriptionInfo?.tier)

        val expectedFeatures = PlanAccessMap.featuresFor(Plan.MAX).map { it.displayName() }
        assertEquals(expectedFeatures, state.planFeatures)
    }

    @Test
    fun `init loads subscription and maps PRO tier to Plan MAX features`() = runTest {
        paymentRepository.currentSubscriptionResult = CareerPilotResult.Success(
            SubscriptionInfo(
                tier = "PRO",
                isActive = true,
                startedAt = "2026-01-01",
                renewalDate = "2026-02-01",
                cancelledAt = null,
                pendingTier = null
            )
        )

        val viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.error)

        val expectedFeatures = PlanAccessMap.featuresFor(Plan.MAX).map { it.displayName() }
        assertEquals(expectedFeatures, state.planFeatures)
    }

    @Test
    fun `init load subscription error updates state with error message and isLoading false`() = runTest {
        paymentRepository.currentSubscriptionResult = CareerPilotResult.Error(NetworkError.SERVER)

        val viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("Failed to load subscription details", state.error)
        assertNull(state.subscriptionInfo)
    }

    @Test
    fun `onIntent LoadSubscription reloads subscription info`() = runTest {
        paymentRepository.currentSubscriptionResult = CareerPilotResult.Error(NetworkError.SERVER)
        val viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        assertEquals("Failed to load subscription details", viewModel.state.value.error)

        // Change to success and reload
        paymentRepository.currentSubscriptionResult = CareerPilotResult.Success(
            SubscriptionInfo(
                tier = "PLUS",
                isActive = true,
                startedAt = "2026-01-01",
                renewalDate = "2026-02-01",
                cancelledAt = null,
                pendingTier = null
            )
        )

        viewModel.onIntent(MySubscriptionIntent.LoadSubscription)
        testScheduler.advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
        assertNull(viewModel.state.value.error)
        assertEquals("PLUS", viewModel.state.value.subscriptionInfo?.tier)
        assertEquals(2, paymentRepository.getCurrentSubscriptionCallCount)
    }

    @Test
    fun `onIntent ShowCancelDialog sets showCancelConfirmDialog to true`() = runTest {
        val viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        assertFalse(viewModel.state.value.showCancelConfirmDialog)

        viewModel.onIntent(MySubscriptionIntent.ShowCancelDialog)

        assertTrue(viewModel.state.value.showCancelConfirmDialog)
    }

    @Test
    fun `onIntent DismissCancelDialog sets showCancelConfirmDialog to false`() = runTest {
        val viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        viewModel.onIntent(MySubscriptionIntent.ShowCancelDialog)
        assertTrue(viewModel.state.value.showCancelConfirmDialog)

        viewModel.onIntent(MySubscriptionIntent.DismissCancelDialog)
        assertFalse(viewModel.state.value.showCancelConfirmDialog)
    }

    @Test
    fun `onIntent ConfirmCancelSubscription on success refreshes access and emits ShowSnackbar`() = runTest {
        val viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        val emittedEffects = mutableListOf<MySubscriptionEffect>()
        val effectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.toList(emittedEffects)
        }

        viewModel.onIntent(MySubscriptionIntent.ShowCancelDialog)
        assertTrue(viewModel.state.value.showCancelConfirmDialog)

        viewModel.onIntent(MySubscriptionIntent.ConfirmCancelSubscription)
        testScheduler.advanceUntilIdle()

        assertFalse(viewModel.state.value.showCancelConfirmDialog)
        assertEquals(1, paymentRepository.cancelSubscriptionCallCount)
        assertEquals(1, accessRepository.refreshCallCount)
        assertEquals(2, paymentRepository.getCurrentSubscriptionCallCount)

        assertEquals(1, emittedEffects.size)
        val effect = emittedEffects.first() as MySubscriptionEffect.ShowSnackbar
        assertEquals("Subscription cancelled successfully", effect.message)

        effectJob.cancel()
    }

    @Test
    fun `onIntent ConfirmCancelSubscription on failure sets isCancelling false and emits failure snackbar`() = runTest {
        paymentRepository.cancelSubscriptionResult = CareerPilotResult.Error(NetworkError.SERVER)

        val viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        val emittedEffects = mutableListOf<MySubscriptionEffect>()
        val effectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.toList(emittedEffects)
        }

        viewModel.onIntent(MySubscriptionIntent.ConfirmCancelSubscription)
        testScheduler.advanceUntilIdle()

        assertFalse(viewModel.state.value.isCancelling)
        assertFalse(viewModel.state.value.showCancelConfirmDialog)
        assertEquals(1, paymentRepository.cancelSubscriptionCallCount)
        assertEquals(0, accessRepository.refreshCallCount)

        assertEquals(1, emittedEffects.size)
        val effect = emittedEffects.first() as MySubscriptionEffect.ShowSnackbar
        assertEquals("Failed to cancel subscription", effect.message)

        effectJob.cancel()
    }

    @Test
    fun `onIntent UpgradePlanClicked emits NavigateToChoosePlan effect`() = runTest {
        val viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        val emittedEffects = mutableListOf<MySubscriptionEffect>()
        val effectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.toList(emittedEffects)
        }

        viewModel.onIntent(MySubscriptionIntent.UpgradePlanClicked)
        testScheduler.advanceUntilIdle()

        assertEquals(listOf(MySubscriptionEffect.NavigateToChoosePlan), emittedEffects)

        effectJob.cancel()
    }

    @Test
    fun `onIntent TopUpCoinsClicked emits NavigateToGetCoins effect`() = runTest {
        val viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        val emittedEffects = mutableListOf<MySubscriptionEffect>()
        val effectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.toList(emittedEffects)
        }

        viewModel.onIntent(MySubscriptionIntent.TopUpCoinsClicked)
        testScheduler.advanceUntilIdle()

        assertEquals(listOf(MySubscriptionEffect.NavigateToGetCoins), emittedEffects)

        effectJob.cancel()
    }

    @Test
    fun `onIntent BackClicked emits NavigateBack effect`() = runTest {
        val viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        val emittedEffects = mutableListOf<MySubscriptionEffect>()
        val effectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.toList(emittedEffects)
        }

        viewModel.onIntent(MySubscriptionIntent.BackClicked)
        testScheduler.advanceUntilIdle()

        assertEquals(listOf(MySubscriptionEffect.NavigateBack), emittedEffects)

        effectJob.cancel()
    }
}
