package com.inclinic.app.core.network

import io.ktor.client.engine.HttpClientEngine

/**
 * Port (interface) for providing the platform-specific [HttpClientEngine].
 * Android adapter provides OkHttp; iOS adapter provides Darwin.
 * Wired via Koin in platform DI modules — NOT expect/actual.
 */
interface HttpClientEngineProvider {
    fun provide(): HttpClientEngine
}
