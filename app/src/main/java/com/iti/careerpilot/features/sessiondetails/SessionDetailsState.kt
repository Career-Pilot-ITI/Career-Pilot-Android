package com.iti.careerpilot.features.sessiondetails

data class SessionDetailsState(
    val paramOne: String = "default",
    val paramTwo: List<String> = emptyList(),
)