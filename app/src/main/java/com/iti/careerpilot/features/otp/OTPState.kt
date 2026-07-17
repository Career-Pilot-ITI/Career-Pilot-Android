package com.iti.careerpilot.features.otp

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class OTPState(
    val paramOne: String = "default",
    val paramTwo: ImmutableList<String> = persistentListOf(),
)