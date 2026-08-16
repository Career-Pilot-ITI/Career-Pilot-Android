package com.iti.careerpilot.core.interviews.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InterviewSessionPageDto(
    @SerialName("content") val content: List<InterviewSessionDto> = emptyList(),
    @SerialName("number") val number: Int = 0,
    @SerialName("size") val size: Int = 0,
    @SerialName("totalPages") val totalPages: Int = 0,
    @SerialName("totalElements") val totalElements: Long = 0L,
    @SerialName("first") val first: Boolean = true,
    @SerialName("last") val last: Boolean = true,
)
