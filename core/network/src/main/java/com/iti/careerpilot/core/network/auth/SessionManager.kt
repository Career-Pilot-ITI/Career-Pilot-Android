package com.iti.careerpilot.core.network.auth

interface SessionManager {
    suspend fun onAuthenticated(accessToken: String, refreshToken: String)
    suspend fun clearSession()
}
