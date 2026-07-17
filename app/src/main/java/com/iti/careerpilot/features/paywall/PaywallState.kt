package com.iti.careerpilot.features.paywall

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class PaywallState(
    val paramOne: String = "default",
    val paramTwo: ImmutableList<String> = persistentListOf(),
)