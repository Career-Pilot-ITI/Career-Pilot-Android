package com.iti.careerpilot.practicesession.data.datasource.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateSessionRequestDto(
    @SerialName("trackId")
    val trackId: Int?,

    @SerialName("questionCount")
    val questionCount: Int?,

    @SerialName("durationMinutes")
    val durationMinutes: Int?
)
