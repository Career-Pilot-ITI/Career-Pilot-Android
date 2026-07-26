package com.iti.careerpilot.practicesession.data.datasource.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FileUploadResponse(
    @SerialName("id")
    val id: Long?,
    @SerialName("type")
    val type: String?,
    @SerialName("originalName")
    val originalName: String?,
    @SerialName("url")
    val url: String?,
    @SerialName("sizeBytes")
    val sizeBytes: Long?,
    @SerialName("createdAt")
    val createdAt: String?
)
