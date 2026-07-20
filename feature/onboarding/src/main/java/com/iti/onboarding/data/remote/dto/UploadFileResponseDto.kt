package com.iti.onboarding.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UploadFileResponseDto(
    val id: Long,
    val type: String,
    val originalName: String,
    val url: String,
    val sizeBytes: Long,
    val createdAt: String,
)