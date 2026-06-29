package com.inclinic.app.core.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp

/**
 * Android-specific [HttpClientEngineProvider] using OkHttp.
 * Registered in the Android Koin platform module.
 */
class AndroidHttpClientEngineProvider : HttpClientEngineProvider {
    override fun provide(): HttpClientEngine = OkHttp.create()
}
