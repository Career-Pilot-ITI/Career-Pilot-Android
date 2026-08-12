package com.iti.careerpilot.practicesession.data.datasource.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateSessionRequestDto(
    @SerialName("trackId")
    val trackId: Long?,

    @SerialName("questionCount")
    val questionCount: Int?,

    @SerialName("durationMinutes")
    val durationMinutes: Int?,

    @SerialName("workspaceId")
    val workspaceId: Long? = null,
)
