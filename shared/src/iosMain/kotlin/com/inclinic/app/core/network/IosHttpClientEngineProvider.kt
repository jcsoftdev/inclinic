package com.inclinic.app.core.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin

/**
 * iOS-specific [HttpClientEngineProvider] using Darwin (NSURLSession-based).
 * Registered in the iOS Koin platform module.
 */
class IosHttpClientEngineProvider : HttpClientEngineProvider {
    override fun provide(): HttpClientEngine = Darwin.create()
}
