package com.iti.careerpilot.features.reports

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class ReportsState(
    val paramOne: String = "default",
    val paramTwo: ImmutableList<String> = persistentListOf(),
)