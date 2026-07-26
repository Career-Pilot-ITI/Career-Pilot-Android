package com.iti.careerpilot.features.paywall.presentation.viewmodel

import com.iti.careerpilot.features.paywall.domain.model.PaymentFailureReason
import com.iti.common.util.UIText

sealed interface PaywallEffect {
    data class ShowSnackbar(val message: UIText) : PaywallEffect
    data class NavigateToWebView(val checkoutUrl: String) : PaywallEffect
    data object NavigateToCheckout : PaywallEffect
    data object NavigateToChoosePlan : PaywallEffect
    data object NavigateToGetCoins : PaywallEffect
    data object NavigateToPaymentProcessing : PaywallEffect
    data object NavigateToPaymentSuccessful : PaywallEffect
    data class NavigateToPaymentFailed(val reason: PaymentFailureReason) : PaywallEffect
    data object NavigateBack : PaywallEffect
    data object NavigateToHome : PaywallEffect
}
