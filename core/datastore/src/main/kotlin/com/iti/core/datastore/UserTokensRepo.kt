package com.iti.core.datastore

import androidx.datastore.core.DataStore
import com.iti.common.dispatcher.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class UserTokensRepo @Inject constructor(
    private val userTokens: DataStore<UserTokens>,
    @param:ApplicationScope private val scope: CoroutineScope
) {

    val tokens: StateFlow<UserTokens> = userTokens.data
        .stateIn(scope, SharingStarted.Eagerly, UserTokens())

    val accessToken: String?
        get() = tokens.value.accessToken

    val refreshToken: String?
        get() = tokens.value.refreshToken

    suspend fun setAccessToken(token: String?) {
        userTokens.updateData { it.copy(accessToken = token) }
    }

    suspend fun setRefreshToken(refreshToken: String?) {
        userTokens.updateData { it.copy(refreshToken = refreshToken) }
    }

    suspend fun clear() {
        userTokens.updateData { UserTokens() }
    }
}
