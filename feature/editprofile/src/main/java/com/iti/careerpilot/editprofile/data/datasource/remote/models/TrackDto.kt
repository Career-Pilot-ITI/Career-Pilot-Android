package com.iti.careerpilot.editprofile.data.datasource.remote.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TrackDto(
    @SerialName("id") val id: Long? = null,
    @SerialName("name") val name: String? = null,
)
