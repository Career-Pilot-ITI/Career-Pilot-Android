package com.iti.careerpilot.features.paywall.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.core.access.PlanAccessMap
import com.iti.careerpilot.core.access.domain.usecase.RefreshAccessUseCase
import com.iti.careerpilot.features.paywall.domain.usecase.CancelSubscriptionUseCase
import com.iti.careerpilot.features.paywall.domain.usecase.GetCurrentSubscriptionUseCase
import com.iti.common.result.onError
import com.iti.common.result.onSuccess
import com.iti.core.model.Plan
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MySubscriptionViewModel @Inject constructor(
    private val getCurrentSubscriptionUseCase: GetCurrentSubscriptionUseCase,
    private val cancelSubscriptionUseCase: CancelSubscriptionUseCase,
    private val refreshAccessUseCase: RefreshAccessUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(MySubscriptionUiState())
    val state = _state.asStateFlow()

    private val _effects = Channel<MySubscriptionEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        loadSubscription()
    }

    fun onIntent(intent: MySubscriptionIntent) {
        when (intent) {
            MySubscriptionIntent.LoadSubscription -> loadSubscription()
            MySubscriptionIntent.ShowCancelDialog -> _state.update { it.copy(showCancelConfirmDialog = true) }
            MySubscriptionIntent.DismissCancelDialog -> _state.update { it.copy(showCancelConfirmDialog = false) }
            MySubscriptionIntent.ConfirmCancelSubscription -> cancelSubscription()
            MySubscriptionIntent.UpgradePlanClicked -> sendEffect(MySubscriptionEffect.NavigateToChoosePlan)
            MySubscriptionIntent.TopUpCoinsClicked -> sendEffect(MySubscriptionEffect.NavigateToGetCoins)
            MySubscriptionIntent.BackClicked -> sendEffect(MySubscriptionEffect.NavigateBack)
        }
    }

    private fun loadSubscription() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getCurrentSubscriptionUseCase()
                .onSuccess { info ->
                    val plan = when (info.tier.uppercase().trim()) {
                        "PLUS" -> Plan.PLUS
                        "PRO", "MAX" -> Plan.MAX
                        else -> Plan.FREE
                    }
                    val features = PlanAccessMap.featuresFor(plan).map { it.displayName() }
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isCancelling = false,
                            subscriptionInfo = info,
                            planFeatures = features,
                            error = null,
                        )
                    }
                }
                .onError {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isCancelling = false,
                            error = "Failed to load subscription details",
                        )
                    }
                }
        }
    }

    private fun cancelSubscription() {
        viewModelScope.launch {
            _state.update { it.copy(isCancelling = true, showCancelConfirmDialog = false) }
            cancelSubscriptionUseCase()
                .onSuccess {
                    refreshAccessUseCase()
                    loadSubscription()
                    sendEffect(MySubscriptionEffect.ShowSnackbar("Subscription cancelled successfully"))
                }
                .onError {
                    _state.update { it.copy(isCancelling = false) }
                    sendEffect(MySubscriptionEffect.ShowSnackbar("Failed to cancel subscription"))
                }
        }
    }

    private fun sendEffect(effect: MySubscriptionEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }
}
