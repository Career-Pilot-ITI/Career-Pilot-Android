package com.iti.careerpilot.features.paywall.presentation.viewmodel

import com.iti.core.model.SubscriptionInfo

data class MySubscriptionUiState(
    val isLoading: Boolean = true,
    val subscriptionInfo: SubscriptionInfo? = null,
    val planFeatures: List<String> = emptyList(),
    val error: String? = null,
    val isCancelling: Boolean = false,
    val showCancelConfirmDialog: Boolean = false,
)
