package com.iti.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class Track(
    val id: Long,
    val name: String,
)

@Serializable
data class TrackDto(
    @SerialName("id") val id: Long? = null,
    @SerialName("name") val name: String? = null,
)