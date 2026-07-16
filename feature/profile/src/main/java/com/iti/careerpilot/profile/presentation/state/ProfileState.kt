package com.iti.careerpilot.profile.presentation.state

import androidx.compose.runtime.Immutable

@Immutable
data class ProfileState(
    val paramOne: String = "default",
    val paramTwo: List<String> = emptyList(),
)