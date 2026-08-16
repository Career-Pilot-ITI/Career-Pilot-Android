package com.iti.careerpilot.features.paywall.presentation.viewmodel

sealed interface MySubscriptionEffect {
    data object NavigateToChoosePlan : MySubscriptionEffect
    data object NavigateToGetCoins : MySubscriptionEffect
    data object NavigateBack : MySubscriptionEffect
    data class ShowSnackbar(val message: String) : MySubscriptionEffect
}
