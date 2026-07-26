package com.iti.careerpilot.features.paywall.presentation.viewmodel

sealed interface PaywallIntent {
    data class SelectPlan(val planId: String) : PaywallIntent
    data class SelectCoinPack(val packId: String) : PaywallIntent
    data object CheckoutRequested : PaywallIntent
    data object ConfirmUpgradeRequested : PaywallIntent
    data object BuyCoinsRequested : PaywallIntent
    data object UpgradeNowRequested : PaywallIntent
    data object WaitUntilNextMonthRequested : PaywallIntent
    data object NavigateBackRequested : PaywallIntent
    data object StartPractisingClicked : PaywallIntent
    data object TryAgainClicked : PaywallIntent
    data object ChangePaymentMethodClicked : PaywallIntent
    data object TestCoinsClicked : PaywallIntent
    data object PollPaymentStatus : PaywallIntent
}
