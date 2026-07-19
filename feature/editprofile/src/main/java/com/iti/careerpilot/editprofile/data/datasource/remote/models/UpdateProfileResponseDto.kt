package com.iti.careerpilot.editprofile.data.datasource.remote.models

import kotlinx.serialization.Serializable

@Serializable
data class UpdateProfileResponseDto(
    val id: Int? = null,
    val phoneNumber: String? = null,
    val profile: UserProfileDto? = null,
    val newUser: Boolean? = null
)