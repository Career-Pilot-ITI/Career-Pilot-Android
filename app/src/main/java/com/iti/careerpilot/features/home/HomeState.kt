package com.iti.careerpilot.features.home

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class HomeState(
    val paramOne: String = "default",
    val paramTwo: ImmutableList<String> = persistentListOf(),
)