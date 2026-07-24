package com.iti.careerpilot.features.paywall.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface PaymentRoute : NavKey {
    @Serializable
    data object ChoosePlan : PaymentRoute

    @Serializable
    data object GetCoins : PaymentRoute
    @Serializable
    data object MonthlyLimit : PaymentRoute
    @Serializable
    data object PaymentProcessing : PaymentRoute
    @Serializable
    data object PaymentSuccessful : PaymentRoute
    @Serializable
    data object PaymentFailed : PaymentRoute
}
