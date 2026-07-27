package com.iti.careerpilot.core.network.auth

import com.iti.core.datastore.UserTokensRepo
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider

class SessionManagerImpl(
    private val client: HttpClient,
    private val tokensRepo: UserTokensRepo,
) : SessionManager {

    override suspend fun onAuthenticated(accessToken: String, refreshToken: String) {
        tokensRepo.setAccessToken(accessToken)
        tokensRepo.setRefreshToken(refreshToken)
        invalidateTokenCache()
    }

    override suspend fun clearSession() {
        tokensRepo.clear()
        invalidateTokenCache()
    }

    private fun invalidateTokenCache() {
        client.authProvider<BearerAuthProvider>()?.clearToken()
    }
}
