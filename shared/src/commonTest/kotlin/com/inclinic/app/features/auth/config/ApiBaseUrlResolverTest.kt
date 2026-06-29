package com.inclinic.app.features.auth.config

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for resolveApiBaseUrl — pure function that rewrites loopback hosts to the
 * platform-specific loopback host in DEV, so Android emulator (10.0.2.2) and iOS
 * simulator (localhost) both reach the host machine without per-platform edits.
 */
class ApiBaseUrlResolverTest {

    @Test
    fun `rewrites localhost to platform host in DEV preserving port`() {
        val result = resolveApiBaseUrl(
            rawUrl = "http://localhost:4005",
            rawEnv = "DEV",
            loopbackHost = "10.0.2.2",
        )
        assertEquals("http://10.0.2.2:4005", result)
    }

    @Test
    fun `rewrites 10_0_2_2 to localhost for iOS in DEV`() {
        val result = resolveApiBaseUrl(
            rawUrl = "http://10.0.2.2:4005",
            rawEnv = "DEV",
            loopbackHost = "localhost",
        )
        assertEquals("http://localhost:4005", result)
    }

    @Test
    fun `rewrites 127_0_0_1 loopback host`() {
        val result = resolveApiBaseUrl(
            rawUrl = "http://127.0.0.1:3000",
            rawEnv = "DEV",
            loopbackHost = "10.0.2.2",
        )
        assertEquals("http://10.0.2.2:3000", result)
    }

    @Test
    fun `preserves path when rewriting`() {
        val result = resolveApiBaseUrl(
            rawUrl = "http://localhost:4005/api",
            rawEnv = "DEV",
            loopbackHost = "10.0.2.2",
        )
        assertEquals("http://10.0.2.2:4005/api", result)
    }

    @Test
    fun `keeps url unchanged when host is a real LAN ip (physical device)`() {
        val result = resolveApiBaseUrl(
            rawUrl = "http://192.168.1.50:4005",
            rawEnv = "DEV",
            loopbackHost = "10.0.2.2",
        )
        assertEquals("http://192.168.1.50:4005", result)
    }

    @Test
    fun `keeps url unchanged when host is a real domain`() {
        val result = resolveApiBaseUrl(
            rawUrl = "http://localhost:4005",
            rawEnv = "STAGING",
            loopbackHost = "10.0.2.2",
        )
        assertEquals("http://localhost:4005", result)
    }

    @Test
    fun `does not rewrite in PROD even if host is loopback`() {
        val result = resolveApiBaseUrl(
            rawUrl = "http://localhost:4005",
            rawEnv = "PROD",
            loopbackHost = "10.0.2.2",
        )
        assertEquals("http://localhost:4005", result)
    }

    @Test
    fun `returns raw url unchanged when scheme is missing`() {
        val result = resolveApiBaseUrl(
            rawUrl = "localhost:4005",
            rawEnv = "DEV",
            loopbackHost = "10.0.2.2",
        )
        assertEquals("localhost:4005", result)
    }

    @Test
    fun `rewrites host with no port`() {
        val result = resolveApiBaseUrl(
            rawUrl = "http://localhost",
            rawEnv = "DEV",
            loopbackHost = "10.0.2.2",
        )
        assertEquals("http://10.0.2.2", result)
    }
}
