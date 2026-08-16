package com.iti.careerpilot.features.paywall.presentation.viewmodel

sealed interface MySubscriptionIntent {
    data object LoadSubscription : MySubscriptionIntent
    data object ShowCancelDialog : MySubscriptionIntent
    data object DismissCancelDialog : MySubscriptionIntent
    data object ConfirmCancelSubscription : MySubscriptionIntent
    data object UpgradePlanClicked : MySubscriptionIntent
    data object TopUpCoinsClicked : MySubscriptionIntent
    data object BackClicked : MySubscriptionIntent
}
