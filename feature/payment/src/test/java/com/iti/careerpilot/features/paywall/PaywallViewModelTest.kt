package com.iti.careerpilot.features.paywall

import com.iti.careerpilot.features.paywall.data.remote.PaymentRemoteDataSource
import com.iti.careerpilot.features.paywall.data.remote.UserSyncManager
import com.iti.careerpilot.features.paywall.data.remote.dto.CheckoutResponseDto
import com.iti.careerpilot.features.paywall.data.remote.dto.CoinBalanceResponseDto
import com.iti.careerpilot.features.paywall.data.remote.dto.DowngradeSubscriptionRequestDto
import com.iti.careerpilot.features.paywall.data.remote.dto.PaymentHistoryPageDto
import com.iti.careerpilot.features.paywall.data.remote.dto.PaymentInitiateRequestDto
import com.iti.careerpilot.features.paywall.data.remote.dto.SubscriptionResponseDto
import com.iti.careerpilot.features.paywall.data.remote.dto.TopUpRequestDto
import com.iti.careerpilot.features.paywall.data.remote.dto.UpgradeSubscriptionRequestDto
import com.iti.careerpilot.features.paywall.domain.repository.PaymentRepository
import com.iti.careerpilot.features.paywall.domain.usecase.DowngradeSubscriptionUseCase
import com.iti.careerpilot.features.paywall.domain.usecase.PollPaymentStatusUseCase
import com.iti.careerpilot.features.paywall.domain.usecase.TopUpWalletUseCase
import com.iti.careerpilot.features.paywall.domain.usecase.UpgradeSubscriptionUseCase
import com.iti.careerpilot.features.paywall.presentation.viewmodel.PaywallEffect
import com.iti.careerpilot.features.paywall.presentation.viewmodel.PaywallIntent
import com.iti.careerpilot.features.paywall.presentation.viewmodel.PaywallViewModel
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import com.iti.core.datastore.models.UserProfile
import com.iti.core.datastore.repo.UserProfileRepo
import com.iti.core.model.CheckoutSession
import com.iti.core.model.SubscriptionInfo
import com.iti.core.model.WalletBalance
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PaywallViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val fakeCheckoutSession = CheckoutSession(
        checkoutUrl = "https://paymob.com/checkout/test-token",
        merchantOrderId = "order-123",
    )

    private val fakeWalletBalance = WalletBalance(balance = 200)

    private val fakeSubscriptionInfo = SubscriptionInfo(
        tier = "FREE",
        isActive = true,
        startedAt = null,
        renewalDate = null,
        cancelledAt = null,
        pendingTier = null,
    )

    private fun successRepo() = object : PaymentRepository {
        override suspend fun getWalletBalance() = CareerPilotResult.Success(fakeWalletBalance)
        override suspend fun getCurrentSubscription() = CareerPilotResult.Success(fakeSubscriptionInfo)
        override suspend fun topUpWallet(coinPackSize: Int, currency: String, method: String) =
            CareerPilotResult.Success(fakeCheckoutSession)
        override suspend fun initiatePayment(
            amount: Double, currency: String, method: String,
            provider: String, purchaseType: String, coinPackSize: Int?, tier: String?
        ) = CareerPilotResult.Success(fakeCheckoutSession)
        override suspend fun upgradeSubscription(tier: String, currency: String, method: String) =
            CareerPilotResult.Success(fakeCheckoutSession)
        override suspend fun downgradeSubscription(tier: String) =
            CareerPilotResult.Success(Unit)
        override suspend fun cancelSubscription() =
            CareerPilotResult.Success(Unit)
        override suspend fun getSubscriptionTiers() =
            CareerPilotResult.Success(mapOf("PLUS" to 199.0, "PRO" to 499.0))
        override suspend fun getCoinPacks() =
            CareerPilotResult.Success(mapOf(100 to 50.0, 500 to 200.0, 1000 to 350.0))
        override suspend fun confirmPayment(merchantOrderId: String) =
            CareerPilotResult.Success(Unit)
    }

    private fun errorRepo() = object : PaymentRepository {
        override suspend fun getWalletBalance() = CareerPilotResult.Error(NetworkError.SERVER)
        override suspend fun getCurrentSubscription() = CareerPilotResult.Error(NetworkError.SERVER)
        override suspend fun topUpWallet(coinPackSize: Int, currency: String, method: String) =
            CareerPilotResult.Error(NetworkError.SERVER)
        override suspend fun initiatePayment(
            amount: Double, currency: String, method: String,
            provider: String, purchaseType: String, coinPackSize: Int?, tier: String?
        ) = CareerPilotResult.Error(NetworkError.SERVER)
        override suspend fun upgradeSubscription(tier: String, currency: String, method: String) =
            CareerPilotResult.Error(NetworkError.SERVER)
        override suspend fun downgradeSubscription(tier: String) =
            CareerPilotResult.Error(NetworkError.SERVER)
        override suspend fun cancelSubscription() =
            CareerPilotResult.Error(NetworkError.SERVER)
        override suspend fun getSubscriptionTiers() =
            CareerPilotResult.Error(NetworkError.SERVER)
        override suspend fun getCoinPacks() =
            CareerPilotResult.Error(NetworkError.SERVER)
        override suspend fun confirmPayment(merchantOrderId: String) =
            CareerPilotResult.Error(NetworkError.SERVER)
    }

    class FakeUserProfileRepo : UserProfileRepo {
        private val _userProfile = MutableStateFlow(UserProfile())
        override val userProfile = _userProfile.asStateFlow()
        override suspend fun updateUserProfile(updateBlock: (UserProfile) -> UserProfile) {
            _userProfile.value = updateBlock(_userProfile.value)
        }
        override suspend fun readUserProfile(): UserProfile = _userProfile.value
        override suspend fun clearUserProfile() {
            _userProfile.value = UserProfile()
        }
    }

    private fun successApi() = object : PaymentRemoteDataSource {
        override suspend fun getWalletBalance() = CoinBalanceResponseDto(balance = 200)
        override suspend fun topUpWallet(request: TopUpRequestDto) = CheckoutResponseDto(checkoutUrl = "url", merchantOrderId = "id")
        override suspend fun initiatePayment(request: PaymentInitiateRequestDto) = CheckoutResponseDto(checkoutUrl = "url", merchantOrderId = "id")
        override suspend fun getCurrentSubscription() = SubscriptionResponseDto(tier = "FREE", isActive = true)
        override suspend fun upgradeSubscription(request: UpgradeSubscriptionRequestDto) = CheckoutResponseDto(checkoutUrl = "url", merchantOrderId = "id")
        override suspend fun downgradeSubscription(request: DowngradeSubscriptionRequestDto) {}
        override suspend fun cancelSubscription() {}
        override suspend fun getPaymentHistory() = PaymentHistoryPageDto()
        override suspend fun getSubscriptionTiers() = mapOf("PLUS" to 199.0, "PRO" to 499.0)
        override suspend fun getCoinPacks() = mapOf(100 to 50.0, 500 to 200.0, 1000 to 350.0)
        override suspend fun confirmPayment(merchantOrderId: String) {}
    }

    private fun errorApi() = object : PaymentRemoteDataSource {
        override suspend fun getWalletBalance(): CoinBalanceResponseDto { throw Exception() }
        override suspend fun topUpWallet(request: TopUpRequestDto): CheckoutResponseDto { throw Exception() }
        override suspend fun initiatePayment(request: PaymentInitiateRequestDto): CheckoutResponseDto { throw Exception() }
        override suspend fun getCurrentSubscription(): SubscriptionResponseDto { throw Exception() }
        override suspend fun upgradeSubscription(request: UpgradeSubscriptionRequestDto): CheckoutResponseDto { throw Exception() }
        override suspend fun downgradeSubscription(request: DowngradeSubscriptionRequestDto) { throw Exception() }
        override suspend fun cancelSubscription() { throw Exception() }
        override suspend fun getPaymentHistory(): PaymentHistoryPageDto { throw Exception() }
        override suspend fun getSubscriptionTiers(): Map<String, Double> { throw Exception() }
        override suspend fun getCoinPacks(): Map<Int, Double> { throw Exception() }
        override suspend fun confirmPayment(merchantOrderId: String) { throw Exception() }
    }

    private fun makeViewModel(
        repo: PaymentRepository = successRepo(),
        api: PaymentRemoteDataSource = successApi(),
        userRepo: UserProfileRepo = FakeUserProfileRepo()
    ): PaywallViewModel {
        val userSyncManager = UserSyncManager(api, userRepo)
        return PaywallViewModel(
            topUpWalletUseCase = TopUpWalletUseCase(repo),
            upgradeSubscriptionUseCase = UpgradeSubscriptionUseCase(repo),
            downgradeSubscriptionUseCase = DowngradeSubscriptionUseCase(repo),
            pollPaymentStatusUseCase = PollPaymentStatusUseCase(userSyncManager),
            getSubscriptionTiersUseCase = com.iti.careerpilot.features.paywall.domain.usecase.GetSubscriptionTiersUseCase(repo),
            getCoinPacksUseCase = com.iti.careerpilot.features.paywall.domain.usecase.GetCoinPacksUseCase(repo),
            confirmPaymentUseCase = com.iti.careerpilot.features.paywall.domain.usecase.ConfirmPaymentUseCase(repo),
            userProfileRepo = userRepo,
            userSyncManager = userSyncManager,
            ioDispatcher = testDispatcher
        )
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `LoadCoinPacks intent loads wallet balance from backend`() = runTest {
        val viewModel = makeViewModel()
        viewModel.onIntent(PaywallIntent.LoadCoinPacks)
        testScheduler.advanceUntilIdle()

        assertEquals(fakeWalletBalance.balance, viewModel.state.value.coinBalance)
        assertEquals(false, viewModel.state.value.isLoadingBalance)
    }

    @Test
    fun `LoadSubscriptionPlans intent loads subscription tier from backend`() = runTest {
        val viewModel = makeViewModel()
        viewModel.onIntent(PaywallIntent.LoadSubscriptionPlans)
        testScheduler.advanceUntilIdle()

        assertEquals(fakeSubscriptionInfo.tier, viewModel.state.value.currentSubscriptionTier)
        assertEquals(false, viewModel.state.value.isLoadingSubscription)
    }

    @Test
    fun `init with error does not crash and handles silently`() = runTest {
        val viewModel = makeViewModel(repo = errorRepo(), api = errorApi())
        testScheduler.advanceUntilIdle()

        assertEquals(0, viewModel.state.value.coinBalance)
        assertEquals("", viewModel.state.value.currentSubscriptionTier)
    }

    @Test
    fun `SelectPlan updates selectedPlanId in state`() = runTest {
        val viewModel = makeViewModel()
        viewModel.onIntent(PaywallIntent.SelectPlan("pro"))
        assertEquals("pro", viewModel.state.value.selectedPlanId)
    }

    @Test
    fun `SelectCoinPack updates selectedCoinPackId in state`() = runTest {
        val viewModel = makeViewModel()
        viewModel.onIntent(PaywallIntent.SelectCoinPack("coins_1000"))
        assertEquals("coins_1000", viewModel.state.value.selectedCoinPackId)
    }

    @Test
    fun `BuyCoinsRequested on success emits NavigateToWebView with checkout url`() = runTest {
        val viewModel = makeViewModel()
        viewModel.onIntent(PaywallIntent.LoadCoinPacks)
        testScheduler.advanceUntilIdle()
        viewModel.onIntent(PaywallIntent.SelectCoinPack("coins_100"))

        val effects = mutableListOf<PaywallEffect>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effectFlow.toList(effects)
        }

        viewModel.onIntent(PaywallIntent.BuyCoinsRequested)
        testScheduler.advanceUntilIdle()

        val webViewEffect = effects.filterIsInstance<PaywallEffect.NavigateToWebView>().firstOrNull()
        assertTrue("Expected NavigateToWebView", webViewEffect != null)
        assertEquals(fakeCheckoutSession.checkoutUrl, webViewEffect!!.checkoutUrl)

        job.cancel()
    }

    @Test
    fun `BuyCoinsRequested on error emits ShowSnackbar`() = runTest {
        val customRepo = object : PaymentRepository by errorRepo() {
            override suspend fun getCoinPacks() = CareerPilotResult.Success(mapOf(100 to 50.0))
        }
        val viewModel = makeViewModel(repo = customRepo, api = errorApi())
        viewModel.onIntent(PaywallIntent.LoadCoinPacks)
        viewModel.onIntent(PaywallIntent.SelectCoinPack("coins_100"))
        testScheduler.advanceUntilIdle()

        val effects = mutableListOf<PaywallEffect>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effectFlow.toList(effects)
        }

        viewModel.onIntent(PaywallIntent.BuyCoinsRequested)
        testScheduler.advanceUntilIdle()

        assertTrue(effects.any { it is PaywallEffect.ShowSnackbar })
        job.cancel()
    }

    @Test
    fun `ConfirmUpgradeRequested on success emits NavigateToWebView`() = runTest {
        val viewModel = makeViewModel()
        viewModel.onIntent(PaywallIntent.SelectPlan("plus"))

        val effects = mutableListOf<PaywallEffect>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effectFlow.toList(effects)
        }

        viewModel.onIntent(PaywallIntent.ConfirmUpgradeRequested)
        testScheduler.advanceUntilIdle()

        val webViewEffect = effects.filterIsInstance<PaywallEffect.NavigateToWebView>().firstOrNull()
        assertTrue("Expected NavigateToWebView", webViewEffect != null)
        assertEquals(fakeCheckoutSession.checkoutUrl, webViewEffect!!.checkoutUrl)

        job.cancel()
    }

    @Test
    fun `ConfirmUpgradeRequested on error emits ShowSnackbar`() = runTest {
        val viewModel = makeViewModel(repo = errorRepo(), api = errorApi())
        viewModel.onIntent(PaywallIntent.SelectPlan("plus"))
        testScheduler.advanceUntilIdle()

        val effects = mutableListOf<PaywallEffect>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effectFlow.toList(effects)
        }

        viewModel.onIntent(PaywallIntent.ConfirmUpgradeRequested)
        testScheduler.advanceUntilIdle()

        assertTrue(effects.any { it is PaywallEffect.ShowSnackbar })
        job.cancel()
    }

    @Test
    fun `UpgradeNowRequested emits NavigateToChoosePlan`() = runTest {
        val viewModel = makeViewModel()

        val effects = mutableListOf<PaywallEffect>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effectFlow.toList(effects)
        }

        viewModel.onIntent(PaywallIntent.UpgradeNowRequested)
        testScheduler.advanceUntilIdle()

        assertTrue(effects.any { it is PaywallEffect.NavigateToChoosePlan })
        job.cancel()
    }

    @Test
    fun `NavigateBackRequested emits NavigateBack`() = runTest {
        val viewModel = makeViewModel()

        val effects = mutableListOf<PaywallEffect>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effectFlow.toList(effects)
        }

        viewModel.onIntent(PaywallIntent.NavigateBackRequested)
        testScheduler.advanceUntilIdle()

        assertTrue(effects.any { it is PaywallEffect.NavigateBack })
        job.cancel()
    }

    @Test
    fun `ConfirmUpgradeRequested on downgrade executes downgrade without payment`() = runTest {
        var downgradeCalled = false
        val customRepo = object : PaymentRepository by successRepo() {
            override suspend fun downgradeSubscription(tier: String): CareerPilotResult<Unit, NetworkError> {
                downgradeCalled = true
                assertEquals("FREE", tier)
                return CareerPilotResult.Success(Unit)
            }
        }
        val customApi = object : PaymentRemoteDataSource by successApi() {
            override suspend fun getCurrentSubscription() = SubscriptionResponseDto(tier = "PLUS", isActive = true)
        }
        val userRepo = FakeUserProfileRepo()
        userRepo.updateUserProfile { current -> current.copy(account = current.account.copy(subscriptionTier = "PLUS")) }

        val viewModel = makeViewModel(repo = customRepo, api = customApi, userRepo = userRepo)
        testScheduler.advanceUntilIdle()

        viewModel.onIntent(PaywallIntent.SelectPlan("free"))
        testScheduler.advanceUntilIdle()

        val effects = mutableListOf<PaywallEffect>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effectFlow.toList(effects)
        }

        viewModel.onIntent(PaywallIntent.ConfirmUpgradeRequested)
        testScheduler.advanceUntilIdle()

        assertTrue("Expected downgradeSubscription to be called", downgradeCalled)
        assertTrue(effects.any { it is PaywallEffect.NavigateBack })
        job.cancel()
    }

    @Test
    fun `ConfirmUpgradeRequested on downgrade to paid plan executes payment checkout`() = runTest {
        val userRepo = FakeUserProfileRepo()
        userRepo.updateUserProfile { current -> current.copy(account = current.account.copy(subscriptionTier = "PRO")) }

        val viewModel = makeViewModel(userRepo = userRepo)
        testScheduler.advanceUntilIdle()

        viewModel.onIntent(PaywallIntent.SelectPlan("plus"))
        testScheduler.advanceUntilIdle()

        val effects = mutableListOf<PaywallEffect>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effectFlow.toList(effects)
        }

        viewModel.onIntent(PaywallIntent.ConfirmUpgradeRequested)
        testScheduler.advanceUntilIdle()

        assertTrue(effects.any { it is PaywallEffect.NavigateToWebView })
        job.cancel()
    }

    @Test
    fun `TryAgainClicked re-initiates checkout for selected plan`() = runTest {
        val viewModel = makeViewModel()
        testScheduler.advanceUntilIdle()

        viewModel.onIntent(PaywallIntent.SelectPlan("plus"))
        viewModel.onIntent(PaywallIntent.ConfirmUpgradeRequested)
        testScheduler.advanceUntilIdle()

        val effects = mutableListOf<PaywallEffect>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effectFlow.toList(effects)
        }

        viewModel.onIntent(PaywallIntent.TryAgainClicked)
        testScheduler.advanceUntilIdle()

        assertTrue(effects.any { it is PaywallEffect.NavigateToWebView })
        job.cancel()
    }

    @Test
    fun `init does not load subscription tier prices or coin packs`() = runTest {
        val viewModel = makeViewModel()
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.state.value.coinPacks.isEmpty())
        assertEquals(false, viewModel.state.value.isLoadingTierPrices)
        assertEquals(false, viewModel.state.value.isLoadingCoinPacks)
    }

    @Test
    fun `LoadSubscriptionPlans and LoadCoinPacks load prices from backend`() = runTest {
        val customRepo = object : PaymentRepository by successRepo() {
            override suspend fun getSubscriptionTiers() = CareerPilotResult.Success(mapOf("PLUS" to 250.0, "PRO" to 600.0))
            override suspend fun getCoinPacks() = CareerPilotResult.Success(mapOf(100 to 60.0, 500 to 220.0, 1000 to 400.0))
        }
        val viewModel = makeViewModel(repo = customRepo)
        viewModel.onIntent(PaywallIntent.LoadSubscriptionPlans)
        viewModel.onIntent(PaywallIntent.LoadCoinPacks)
        testScheduler.advanceUntilIdle()

        val plusPlan = viewModel.state.value.subscriptionPlans.find { it.id == "plus" }
        val proPlan = viewModel.state.value.subscriptionPlans.find { it.id == "pro" }
        val pack100 = viewModel.state.value.coinPacks.find { it.coins == 100 }

        assertEquals(250, plusPlan?.priceEgp)
        assertEquals(600, proPlan?.priceEgp)
        assertEquals(60, pack100?.priceEgp)
    }

    @Test
    fun `LoadSubscriptionPlans intent updates subscription plan prices and sets isLoadingTierPrices to false`() = runTest {
        val customRepo = object : PaymentRepository by successRepo() {
            override suspend fun getSubscriptionTiers() = CareerPilotResult.Success(mapOf("PLUS" to 299.0))
        }
        val viewModel = makeViewModel(repo = customRepo)

        viewModel.onIntent(PaywallIntent.LoadSubscriptionPlans)
        testScheduler.advanceUntilIdle()

        val plusPlan = viewModel.state.value.subscriptionPlans.find { it.id == "plus" }
        assertEquals(299, plusPlan?.priceEgp)
        assertEquals(false, viewModel.state.value.isLoadingTierPrices)
    }

    @Test
    fun `LoadSubscriptionPlans intent on error sets isLoadingTierPrices to false`() = runTest {
        val viewModel = makeViewModel(repo = errorRepo(), api = errorApi())

        viewModel.onIntent(PaywallIntent.LoadSubscriptionPlans)
        testScheduler.advanceUntilIdle()

        assertEquals(false, viewModel.state.value.isLoadingTierPrices)
    }

    @Test
    fun `LoadCoinPacks intent populates coin packs, sets default selected pack, and sets isLoadingCoinPacks to false`() = runTest {
        val viewModel = makeViewModel()

        viewModel.onIntent(PaywallIntent.LoadCoinPacks)
        testScheduler.advanceUntilIdle()

        assertEquals(3, viewModel.state.value.coinPacks.size)
        assertEquals("coins_500", viewModel.state.value.selectedCoinPackId)
        assertEquals(false, viewModel.state.value.isLoadingCoinPacks)
    }

    @Test
    fun `LoadCoinPacks intent on error sets isLoadingCoinPacks to false`() = runTest {
        val viewModel = makeViewModel(repo = errorRepo(), api = errorApi())

        viewModel.onIntent(PaywallIntent.LoadCoinPacks)
        testScheduler.advanceUntilIdle()

        assertEquals(false, viewModel.state.value.isLoadingCoinPacks)
    }
}
