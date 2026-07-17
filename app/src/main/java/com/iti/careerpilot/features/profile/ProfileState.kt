package com.iti.careerpilot.features.profile

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class ProfileState(
    val paramOne: String = "default",
    val paramTwo: ImmutableList<String> = persistentListOf(),
)