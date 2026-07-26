package com.iti.careerpilot.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthTokensDto(
    @SerialName("accessToken") val rawAccessToken: String? = null,
    @SerialName("token") val rawToken: String? = null,
    @SerialName("refreshToken") val rawRefreshToken: String? = null,
    @SerialName("expiresIn") val expiresIn: Long = 0L,
) {
    constructor(
        accessToken: String,
        refreshToken: String,
        expiresIn: Long = 0L,
    ) : this(
        rawAccessToken = accessToken,
        rawRefreshToken = refreshToken,
        expiresIn = expiresIn,
    )

    val accessToken: String
        get() = rawAccessToken ?: rawToken ?: ""

    val refreshToken: String
        get() = rawRefreshToken ?: ""
}
