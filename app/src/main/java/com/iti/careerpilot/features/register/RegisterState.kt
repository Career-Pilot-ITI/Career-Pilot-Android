package com.iti.careerpilot.features.register

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class RegisterState(
    val paramOne: String = "default",
    val paramTwo: ImmutableList<String> = persistentListOf(),
)