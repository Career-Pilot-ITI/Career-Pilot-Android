package com.iti.onboarding.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TracksResponseDto(
    @SerialName("id")
    val id: Long,
    @SerialName("name")
    val name: String,
    @SerialName("description")
    val description: String,
    @SerialName("isActive")
    val isActive: Boolean,
    @SerialName("createdAt")
    val createdAt: String
)
