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
import io.ktor.client.request.header
import io.ktor.client.call.body
import io.ktor.http.HttpStatusCode
import com.iti.careerpilot.core.network.Endpoints
import com.iti.careerpilot.core.network.BuildConfig
import com.iti.careerpilot.core.network.auth.SessionManager
import com.iti.careerpilot.core.network.auth.SessionManagerImpl
import com.iti.careerpilot.core.network.model.AuthTokensDto
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import com.iti.core.datastore.UserTokensRepo
import io.ktor.client.plugins.DefaultRequest
import io.ktor.http.ContentType
import io.ktor.http.contentType
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

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
        prettyPrint = true
    }

    @Provides
    @Singleton
    fun provideSessionManager(
        client: HttpClient,
        tokensRepo: UserTokensRepo,
    ): SessionManager = SessionManagerImpl(client, tokensRepo)

    @Provides
    @Singleton
    fun provideHttpClient(json: Json, datastore: UserTokensRepo): HttpClient {
        return HttpClient(OkHttp) {
            engine {
                addInterceptor { chain ->
                    val request = chain.request()
                    val response = chain.proceed(request)

                    val path = request.url.encodedPath
                    val isSkipAuth = path.contains("otp", ignoreCase = true) ||
                            path.contains("refresh", ignoreCase = true)

                    if (response.code == 403 && !isSkipAuth) {
                        response.newBuilder().code(401).message("Unauthorized").build()
                    } else {
                        response
                    }
                }
            }

            expectSuccess = true

            install(ContentNegotiation) {
                json(json)
            }

            install(HttpTimeout) {
                connectTimeoutMillis = 120_000
                requestTimeoutMillis = 120_000
                socketTimeoutMillis = 120_000
            }

            if (BuildConfig.DEBUG) {
                install(Logging) {
                    level = LogLevel.ALL
                    logger = object : Logger {
                        override fun log(message: String) {
                            Log.d("KtorClient", message)
                        }
                    }
                    sanitizeHeader { header -> header.equals(HttpHeaders.Authorization, ignoreCase = true) }
                }
            }

            install(DefaultRequest) {
                contentType(
                    ContentType.Application.Json
                )
                header("ngrok-skip-browser-warning", "true")
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
                        val tokens = datastore.readTokens()
                        val access = tokens.accessToken
                        val refresh = tokens.refreshToken
                        if (!access.isNullOrBlank() && !refresh.isNullOrBlank()) {
                            BearerTokens(access, refresh)
                        } else {
                            null
                        }
                    }
                    refreshTokens {
                        val userTokens = datastore.readTokens()
                        val refreshToken = oldTokens?.refreshToken ?: userTokens.refreshToken

                        if (refreshToken.isNullOrBlank()) {
                            return@refreshTokens null
                        }

                        try {
                            val response = client.post(Endpoints.REFRESH_TOKEN) {
                                markAsRefreshTokenRequest()
                                setBody(mapOf("refreshToken" to refreshToken))
                            }

                            if (response.status == HttpStatusCode.OK) {
                                val tokens = response.body<AuthTokensDto>()
                                datastore.setAccessToken(tokens.accessToken)
                                datastore.setRefreshToken(tokens.refreshToken)
                                BearerTokens(tokens.accessToken, tokens.refreshToken)
                            } else {
                                datastore.clear()
                                null
                            }
                        } catch (cancellation: CancellationException) {
                            throw cancellation
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


