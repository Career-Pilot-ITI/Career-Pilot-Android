package com.iti.onboarding.domain.model

data class UploadedFile(
    val id: Long,
    val url: String,
    val type: String,
    val sizeBytes: Long,
    val originalName: String,
)
