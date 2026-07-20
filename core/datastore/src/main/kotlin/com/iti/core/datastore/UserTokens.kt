package com.iti.core.datastore

import kotlinx.serialization.Serializable

@Serializable
data class UserTokens(
    val accessToken: String? = null,
    val refreshToken: String? = null,
)
