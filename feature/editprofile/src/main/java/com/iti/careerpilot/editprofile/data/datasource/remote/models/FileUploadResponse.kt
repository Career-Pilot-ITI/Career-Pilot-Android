package com.iti.careerpilot.editprofile.data.datasource.remote.models

import kotlinx.serialization.Serializable

@Serializable
data class FileUploadResponse(
    val id: Long,
    val type: String,
    val originalName: String,
    val url: String,
    val sizeBytes: Long,
    val createdAt: String
)