package com.iti.careerpilot.features.paywall

data class PaywallState(
    val paramOne: String = "default",
    val paramTwo: List<String> = emptyList(),
)