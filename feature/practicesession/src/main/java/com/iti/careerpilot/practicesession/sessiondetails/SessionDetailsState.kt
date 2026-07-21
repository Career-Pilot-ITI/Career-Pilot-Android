package com.iti.careerpilot.practicesession.sessiondetails

data class SessionDetailsState(
    val paramOne: String = "default",
    val paramTwo: List<String> = emptyList(),
)