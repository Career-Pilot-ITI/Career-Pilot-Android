package com.iti.careerpilot.features.paywall.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.features.paywall.data.remote.UserSyncManager
import com.iti.careerpilot.features.paywall.domain.model.PaymentFailureReason
import com.iti.careerpilot.features.paywall.domain.model.SubscriptionTier
import com.iti.careerpilot.features.paywall.domain.repository.PaymentRepository
import com.iti.careerpilot.features.paywall.domain.usecase.ConfirmPaymentUseCase
import com.iti.careerpilot.features.paywall.domain.usecase.DowngradeSubscriptionUseCase
import com.iti.careerpilot.features.paywall.domain.usecase.GetCoinPacksUseCase
import com.iti.careerpilot.features.paywall.domain.usecase.GetSubscriptionTiersUseCase
import com.iti.careerpilot.features.paywall.domain.usecase.PollPaymentStatusUseCase
import com.iti.careerpilot.features.paywall.domain.usecase.PollResult
import com.iti.careerpilot.features.paywall.domain.usecase.TopUpWalletUseCase
import com.iti.careerpilot.features.paywall.domain.usecase.UpgradeSubscriptionUseCase
import com.iti.common.dispatcher.CareerPilotDispatchers.IO
import com.iti.common.dispatcher.Dispatcher
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import com.iti.common.util.toUIText
import com.iti.core.datastore.repo.UserProfileRepo
import com.iti.core.model.CheckoutSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
class PaywallViewModel @Inject constructor(
    private val topUpWalletUseCase: TopUpWalletUseCase,
    private val upgradeSubscriptionUseCase: UpgradeSubscriptionUseCase,
    private val downgradeSubscriptionUseCase: DowngradeSubscriptionUseCase,
    private val pollPaymentStatusUseCase: PollPaymentStatusUseCase,
    private val getSubscriptionTiersUseCase: GetSubscriptionTiersUseCase,
    private val getCoinPacksUseCase: GetCoinPacksUseCase,
    private val confirmPaymentUseCase: ConfirmPaymentUseCase,
    private val userProfileRepo: UserProfileRepo,
    private val userSyncManager: UserSyncManager,
    @Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val mutableState: MutableStateFlow<PaywallState>
    val state: StateFlow<PaywallState>

    private val _effectChannel = Channel<PaywallEffect>()
    val effectFlow = _effectChannel.receiveAsFlow()

    private val isPolling = AtomicBoolean(false)
    private var hasUserSelectedPlan = false

    init {
        val initialProfile = userProfileRepo.userProfile.value
        val initialTier = initialProfile.account.subscriptionTier
        val initialPlanId = SubscriptionTier.normalizeTierId(initialTier)

        mutableState = MutableStateFlow(
            PaywallState(
                coinBalance = initialProfile.account.coinBalance,
                currentSubscriptionTier = initialTier,
                selectedPlanId = initialPlanId
            )
        )
        state = mutableState.asStateFlow()

        userProfileRepo.userProfile.onEach { profile ->
            val tier = profile.account.subscriptionTier
            val mappedPlanId = SubscriptionTier.normalizeTierId(tier)
            mutableState.update { currentState ->
                currentState.copy(
                    coinBalance = profile.account.coinBalance,
                    currentSubscriptionTier = tier,
                    selectedPlanId = if (!hasUserSelectedPlan) mappedPlanId else currentState.selectedPlanId
                )
            }
        }.launchIn(viewModelScope)

        loadWalletBalance()
        loadCurrentSubscription()
        loadSubscriptionTiers()
        loadCoinPacks()
    }

    private fun loadWalletBalance() {
        viewModelScope.launch(ioDispatcher) {
            mutableState.update { it.copy(isLoadingBalance = true) }
            userSyncManager.syncWalletBalance()
            mutableState.update { it.copy(isLoadingBalance = false) }
        }
    }

    private fun loadCurrentSubscription() {
        viewModelScope.launch(ioDispatcher) {
            mutableState.update { it.copy(isLoadingSubscription = true) }
            userSyncManager.syncSubscriptionTier()
            val currentProfile = userProfileRepo.readUserProfile()
            val currentTier = currentProfile.account.subscriptionTier
            val mappedPlanId = SubscriptionTier.normalizeTierId(currentTier)
            mutableState.update { currentState ->
                currentState.copy(
                    isLoadingSubscription = false,
                    currentSubscriptionTier = currentTier,
                    selectedPlanId = if (!hasUserSelectedPlan) mappedPlanId else currentState.selectedPlanId
                )
            }
        }
    }

    private fun loadSubscriptionTiers() {
        viewModelScope.launch(ioDispatcher) {
            mutableState.update { it.copy(isLoadingTierPrices = true) }
            when (val result = getSubscriptionTiersUseCase()) {
                is CareerPilotResult.Success -> {
                    val prices = result.data
                    mutableState.update { currentState ->
                        val updatedPlans = currentState.subscriptionPlans.map { plan ->
                            val planKey = plan.id.uppercase()
                            val price = prices[planKey]?.toInt() ?: plan.priceEgp
                            plan.copy(priceEgp = price, originalPriceEgp = null)
                        }.toImmutableList()
                        currentState.copy(
                            isLoadingTierPrices = false,
                            subscriptionPlans = updatedPlans
                        )
                    }
                }
                is CareerPilotResult.Error -> {
                    mutableState.update { it.copy(isLoadingTierPrices = false) }
                }
            }
        }
    }

    private fun loadCoinPacks() {
        viewModelScope.launch(ioDispatcher) {
            mutableState.update { it.copy(isLoadingCoinPacks = true) }
            when (val result = getCoinPacksUseCase()) {
                is CareerPilotResult.Success -> {
                    val prices = result.data
                    mutableState.update { currentState ->
                        val updatedPacks = currentState.coinPacks.map { pack ->
                            val price = prices[pack.coins]?.toInt() ?: pack.priceEgp
                            pack.copy(priceEgp = price, originalPriceEgp = null)
                        }.toImmutableList()
                        currentState.copy(
                            isLoadingCoinPacks = false,
                            coinPacks = updatedPacks
                        )
                    }
                }
                is CareerPilotResult.Error -> {
                    mutableState.update { it.copy(isLoadingCoinPacks = false) }
                }
            }
        }
    }

    fun refreshUserData() {
        viewModelScope.launch(ioDispatcher) {
            mutableState.update {
                it.copy(
                    isLoadingBalance = true,
                    isLoadingSubscription = true,
                    isLoadingTierPrices = true,
                    isLoadingCoinPacks = true
                )
            }
            runCatching { userSyncManager.syncWalletBalance() }
            runCatching { userSyncManager.syncSubscriptionTier() }
            loadSubscriptionTiers()
            loadCoinPacks()
            mutableState.update {
                it.copy(
                    isLoadingBalance = false,
                    isLoadingSubscription = false
                )
            }
        }
    }

    fun onIntent(intent: PaywallIntent) {
        when (intent) {
            PaywallIntent.CheckoutRequested,
            PaywallIntent.ConfirmUpgradeRequested -> {
                val selectedPlan = mutableState.value.selectedPlan
                if (selectedPlan != null && selectedPlan.id.equals("free", ignoreCase = true)) {
                    handleDowngradeRequested()
                } else {
                    handleUpgradeRequested()
                }
            }
            is PaywallIntent.SelectPlan -> {
                hasUserSelectedPlan = true
                mutableState.update { it.copy(selectedPlanId = intent.planId) }
            }
            is PaywallIntent.SelectCoinPack -> {
                mutableState.update { it.copy(selectedCoinPackId = intent.packId) }
            }
            PaywallIntent.BuyCoinsRequested -> handleBuyCoinsRequested()
            PaywallIntent.UpgradeNowRequested -> emitEffect(PaywallEffect.NavigateToChoosePlan)
            PaywallIntent.WaitUntilNextMonthRequested,
            PaywallIntent.NavigateBackRequested -> emitEffect(PaywallEffect.NavigateBack)
            PaywallIntent.StartPractisingClicked -> emitEffect(PaywallEffect.NavigateToHome)
            PaywallIntent.TryAgainClicked,
            PaywallIntent.ChangePaymentMethodClicked -> handleTryAgainRequested()
            PaywallIntent.TestCoinsClicked -> emitEffect(PaywallEffect.NavigateToGetCoins)
            PaywallIntent.PollPaymentStatus -> pollPaymentStatus()
        }
    }

    private fun handleTryAgainRequested() {
        when (mutableState.value.checkoutItemType) {
            CheckoutItemType.COIN_PACK -> handleBuyCoinsRequested()
            CheckoutItemType.SUBSCRIPTION -> handleUpgradeRequested()
            else -> emitEffect(PaywallEffect.NavigateToChoosePlan)
        }
    }

    private fun executePaymentCheckout(
        checkoutCall: suspend () -> CareerPilotResult<CheckoutSession, NetworkError>
    ) {
        viewModelScope.launch(ioDispatcher) {
            val preBalance = mutableState.value.coinBalance
            val preTier = mutableState.value.currentSubscriptionTier
            mutableState.update { it.copy(isCheckoutInProgress = true) }
            when (val result = checkoutCall()) {
                is CareerPilotResult.Success -> {
                    mutableState.update {
                        it.copy(
                            isCheckoutInProgress = false,
                            merchantOrderId = result.data.merchantOrderId,
                            preCheckoutBalance = preBalance,
                            preCheckoutTier = preTier
                        )
                    }
                    _effectChannel.send(PaywallEffect.NavigateToWebView(result.data.checkoutUrl))
                }
                is CareerPilotResult.Error -> {
                    mutableState.update { it.copy(isCheckoutInProgress = false) }
                    _effectChannel.send(PaywallEffect.ShowSnackbar(result.error.toUIText()))
                }
            }
        }
    }

    private fun handleUpgradeRequested() {
        val selectedPlan = mutableState.value.selectedPlan
        if (selectedPlan == null || selectedPlan.id.equals("free", ignoreCase = true) || (selectedPlan.priceEgp ?: 0) <= 0) {
            if (selectedPlan?.id?.equals("free", ignoreCase = true) == true) {
                handleDowngradeRequested()
            }
            return
        }
        val planId = selectedPlan.id.uppercase()
        mutableState.update { it.copy(checkoutItemType = CheckoutItemType.SUBSCRIPTION) }
        executePaymentCheckout { upgradeSubscriptionUseCase(planId, "EGP", "card") }
    }

    private fun handleDowngradeRequested() {
        val selectedPlan = mutableState.value.selectedPlan ?: return
        val targetTier = selectedPlan.id.uppercase()
        viewModelScope.launch(ioDispatcher) {
            mutableState.update { it.copy(isCheckoutInProgress = true) }
            when (val result = downgradeSubscriptionUseCase(targetTier)) {
                is CareerPilotResult.Success -> {
                    userProfileRepo.updateUserProfile { profile ->
                        profile.copy(
                            account = profile.account.copy(
                                subscriptionTier = targetTier
                            )
                        )
                    }
                    hasUserSelectedPlan = false
                    mutableState.update { currentState ->
                        currentState.copy(
                            isCheckoutInProgress = false,
                            currentSubscriptionTier = targetTier,
                            selectedPlanId = SubscriptionTier.normalizeTierId(targetTier)
                        )
                    }
                    _effectChannel.send(PaywallEffect.NavigateBack)
                }
                is CareerPilotResult.Error -> {
                    mutableState.update { it.copy(isCheckoutInProgress = false) }
                    _effectChannel.send(PaywallEffect.ShowSnackbar(result.error.toUIText()))
                }
            }
        }
    }

    private fun handleBuyCoinsRequested() {
        val packId = mutableState.value.selectedCoinPackId
        val pack = mutableState.value.coinPacks.find { it.id == packId }
        if (pack != null) {
            mutableState.update { it.copy(checkoutItemType = CheckoutItemType.COIN_PACK) }
            executePaymentCheckout { topUpWalletUseCase(pack.coins, "EGP", "card") }
        }
    }

    private fun emitEffect(effect: PaywallEffect) {
        viewModelScope.launch {
            _effectChannel.send(effect)
        }
    }

    private fun pollPaymentStatus() {
        if (!isPolling.compareAndSet(false, true)) return

        viewModelScope.launch(ioDispatcher) {
            try {
                val baselineBalance = mutableState.value.preCheckoutBalance ?: mutableState.value.coinBalance
                val baselineTier = mutableState.value.preCheckoutTier ?: mutableState.value.currentSubscriptionTier
                val selectedPlan = mutableState.value.selectedPlan
                val targetTier = if (mutableState.value.checkoutItemType == CheckoutItemType.SUBSCRIPTION) selectedPlan?.id else null
                val pack = mutableState.value.coinPacks.find { it.id == mutableState.value.selectedCoinPackId }
                val expectedCoinDelta = if (mutableState.value.checkoutItemType == CheckoutItemType.COIN_PACK) pack?.coins ?: 0 else 0
                val merchantOrderId = mutableState.value.merchantOrderId
                if (!merchantOrderId.isNullOrBlank()) {
                    runCatching { confirmPaymentUseCase(merchantOrderId) }
                }

                val result = pollPaymentStatusUseCase(
                    baselineBalance = baselineBalance,
                    baselineTier = baselineTier,
                    getCurrentBalance = { userProfileRepo.readUserProfile().account.coinBalance },
                    getCurrentTier = { userProfileRepo.readUserProfile().account.subscriptionTier },
                    merchantOrderId = merchantOrderId,
                    targetTier = targetTier,
                    expectedCoinDelta = expectedCoinDelta
                )

                when (result) {
                    PollResult.Success -> _effectChannel.send(PaywallEffect.NavigateToPaymentSuccessful)
                    is PollResult.Failed -> {
                        mutableState.update { it.copy(failureReason = result.reason) }
                        _effectChannel.send(PaywallEffect.NavigateToPaymentFailed(result.reason))
                    }
                }
            } finally {
                isPolling.set(false)
            }
        }
    }
}
