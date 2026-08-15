package com.iti.careerpilot.home.data.datasource.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PageResponseDto<T>(
    @SerialName("content") val content: List<T> = emptyList(),
    @SerialName("totalElements") val totalElements: Long? = null,
    @SerialName("totalPages") val totalPages: Int? = null,
    @SerialName("number") val number: Int? = null,
    @SerialName("size") val size: Int? = null,
)
