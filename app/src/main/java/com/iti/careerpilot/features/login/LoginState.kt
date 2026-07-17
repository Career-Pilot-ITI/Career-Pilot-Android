package com.iti.careerpilot.features.login

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class LoginState(
    val paramOne: String = "default",
    val paramTwo: ImmutableList<String> = persistentListOf(),
)