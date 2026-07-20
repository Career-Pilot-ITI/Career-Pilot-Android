package com.iti.careerpilot.core.network.di

import android.util.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.HttpHeaders
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.call.body
import io.ktor.http.HttpStatusCode
import com.iti.careerpilot.core.network.Endpoints
import com.iti.careerpilot.core.network.model.AuthTokensDto
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.json.Json
import com.iti.core.datastore.CareerPilotPreferencesDataSource
import io.ktor.client.plugins.DefaultRequest
import io.ktor.http.ContentType
import io.ktor.http.contentType
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        encodeDefaults = true
        explicitNulls = false
    }

    @Provides
    @Singleton
    fun provideHttpClient(json: Json, datastore: CareerPilotPreferencesDataSource): HttpClient {
        return HttpClient(OkHttp) {
            expectSuccess = true

            install(ContentNegotiation) {
                json(json)
            }

            install(HttpTimeout) {
                connectTimeoutMillis = 15_000
                requestTimeoutMillis = 30_000
                socketTimeoutMillis = 30_000
            }

            install(Logging) {
                level = LogLevel.ALL
                logger = object : Logger {
                    override fun log(message: String) {
                        Log.d("KtorClient", message)
                    }
                }
                sanitizeHeader { header -> header.equals(HttpHeaders.Authorization, ignoreCase = true) }
            }

            install(DefaultRequest) {
                contentType(
                    ContentType.Application.Json
                )
            }

            install(Auth) {
                bearer {
                    sendWithoutRequest { request ->
                        val path = request.url.pathSegments.joinToString("/")
                        val skipAuth = path.contains("otp", ignoreCase = true) ||
                                path.contains("refresh", ignoreCase = true)
                        !skipAuth
                    }
                    loadTokens {
                        val accessToken = datastore.token.firstOrNull()
                        val refreshToken = datastore.refreshToken.firstOrNull()
                        if (!accessToken.isNullOrEmpty()) {
                            BearerTokens(accessToken, refreshToken ?: "")
                        } else {
                            null
                        }
                    }
                    refreshTokens {
                        val refreshToken = oldTokens?.refreshToken ?: datastore.refreshToken.firstOrNull()

                        if (refreshToken.isNullOrEmpty()) {
                            return@refreshTokens null
                        }

                        try {
                            val response = client.post(Endpoints.REFRESH_TOKEN) {
                                markAsRefreshTokenRequest()
                                setBody(mapOf("refreshToken" to refreshToken))
                            }

                            if (response.status == HttpStatusCode.OK) {
                                val tokens = response.body<AuthTokensDto>()
                                datastore.setToken(tokens.accessToken)
                                datastore.setRefreshToken(tokens.refreshToken)
                                BearerTokens(tokens.accessToken, tokens.refreshToken)
                            } else {
                                datastore.clear()
                                null
                            }
                        } catch (e: Exception) {
                            Log.e("KtorClient", "Error refreshing tokens", e)
                            datastore.clear()
                            null
                        }
                    }
                }
            }
        }
    }
}

