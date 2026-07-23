package com.iti.careerpilot.features.home

data class HomeState(
    val coinBalance: Int = 0,
    val subscriptionTier: String = "FREE",
) {
    val formattedSubscriptionTier: String
        get() = when (subscriptionTier.uppercase()) {
            "PRO" -> "Pro"
            "MAX" -> "Max"
            "PLUS" -> "Plus"
            else -> "Free"
        }

    val formattedCoinBalance: String
        get() = "Coins: $coinBalance"
}