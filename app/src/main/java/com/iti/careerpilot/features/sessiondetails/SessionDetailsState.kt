package com.iti.careerpilot.features.sessiondetails

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class SessionDetailsState(
    val paramOne: String = "default",
    val paramTwo: ImmutableList<String> = persistentListOf(),
)