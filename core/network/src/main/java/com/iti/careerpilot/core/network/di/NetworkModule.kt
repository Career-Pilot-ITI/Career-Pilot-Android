package com.iti.careerpilot.core.network.di

import android.util.Log
import com.iti.careerpilot.core.network.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.json.Json
import com.iti.core.datastore.CareerPilotPreferencesDataSource
import io.ktor.client.request.header
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
    fun provideHttpClient(json: Json, datastore: CareerPilotPreferencesDataSource): HttpClient = HttpClient(OkHttp) {
        expectSuccess = true

        install(ContentNegotiation) {
            json(json)
        }

        install(HttpTimeout) {
            connectTimeoutMillis = 15_000
            requestTimeoutMillis = 30_000
            socketTimeoutMillis = 30_000
        }

        install(Auth) {
            bearer {
                loadTokens {
                    datastore.token.firstOrNull()?.let { BearerTokens(it, "") }
                }
            }
        }

        if (BuildConfig.DEBUG) {
            install(Logging) {
                level = LogLevel.INFO
                logger = object : Logger {
                    override fun log(message: String) {
                        Log.d("KtorClient", message)
                    }
                }
                sanitizeHeader { header -> header.equals(HttpHeaders.Authorization, ignoreCase = true) }
            }
        }

        defaultRequest {
            url(BuildConfig.BASE_URL)
            header(HttpHeaders.Authorization, "Bearer eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6WyJST0xFX1VTRVIiXSwiaWQiOjEyLCJlbWFpbCI6ImhhemVta29yYTY2MEBnbWFpbC5jb20iLCJzdWIiOiJ1c2VyXzAwMDAwNiIsImlhdCI6MTc4NDQwNjU3OSwiZXhwIjoxNzg0NDEwMTc5fQ.bdb2GWFRpyP4E_tGcz0NF7IVNaJpC0K3b8C8xHRNTyA")
        }
    }
}

