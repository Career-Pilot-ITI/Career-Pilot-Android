package com.iti.careerpilot.features.profile

data class ProfileState(
    val paramOne: String = "default",
    val paramTwo: List<String> = emptyList(),
)