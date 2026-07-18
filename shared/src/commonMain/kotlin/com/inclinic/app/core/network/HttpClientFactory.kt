package com.inclinic.app.core.network

import com.inclinic.app.features.auth.core.port.TokenStorage
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object HttpClientFactory {

    fun create(engine: HttpClientEngine): HttpClient = HttpClient(engine) {
        expectSuccess = true
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; isLenient = false; encodeDefaults = true })
        }
        install(Logging) { level = LogLevel.INFO }
        defaultRequest { contentType(ContentType.Application.Json) }
    }

    fun createAuthenticated(
        engine: HttpClientEngine,
        baseUrl: String,
        tokenStorage: TokenStorage,
        refreshCoordinator: RefreshCoordinator,
    ): HttpClient = HttpClient(engine) {
        expectSuccess = true
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; isLenient = false; encodeDefaults = true })
        }
        install(Logging) { level = LogLevel.INFO }
        defaultRequest {
            url(baseUrl)
            contentType(ContentType.Application.Json)
        }
        install(Auth) {
            bearer {
                loadTokens {
                    tokenStorage.load()?.let { BearerTokens(it.accessToken, it.refreshToken) }
                }
                refreshTokens {
                    refreshCoordinator.refresh(oldTokens)
                }
                sendWithoutRequest { req ->
                    val path = req.url.encodedPathSegments.joinToString("/", prefix = "/")
                    !path.startsWith("/api/auth/")
                }
            }
        }
    }
}
