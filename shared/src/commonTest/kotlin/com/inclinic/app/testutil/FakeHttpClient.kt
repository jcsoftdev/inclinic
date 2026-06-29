package com.inclinic.app.testutil

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json

/**
 * Test utility that builds a [HttpClient] backed by [MockEngine].
 *
 * Usage:
 * ```kotlin
 * val fake = FakeHttpClient()
 * fake.respondWith("/api/v1/auth/login", """{"accessToken":"t","refreshToken":"r"}""")
 * val client = fake.build()
 * ```
 */
class FakeHttpClient {
    private val responses = mutableMapOf<String, String>()
    val callCount = mutableMapOf<String, Int>()

    fun respondWith(path: String, body: String) {
        responses[path] = body
    }

    fun build(): HttpClient = HttpClient(MockEngine { request ->
        val path = request.url.encodedPath
        callCount[path] = (callCount[path] ?: 0) + 1
        val body = responses[path] ?: error("No mock response registered for path: $path")
        respond(
            content = body,
            status = HttpStatusCode.OK,
            headers = headersOf(HttpHeaders.ContentType, "application/json"),
        )
    }) {
        install(ContentNegotiation) { json() }
    }
}
