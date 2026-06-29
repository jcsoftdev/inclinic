package com.inclinic.app.features.auth.config

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * 🔴 RED tests for validateAuthConfig — pure function, no Koin, no BuildKonfig.
 *
 * These tests MUST fail before AuthConfig.kt + validateAuthConfig are written.
 * Once impl is in place, they turn GREEN.
 */
class AuthConfigValidationTest {

    @Test
    fun `validateAuthConfig throws IllegalStateException when apiBaseUrl is blank`() {
        assertFailsWith<IllegalStateException> {
            validateAuthConfig(rawUrl = "", rawEnv = "DEV")
        }
    }

    @Test
    fun `validateAuthConfig throws IllegalStateException when apiBaseUrl is whitespace only`() {
        assertFailsWith<IllegalStateException> {
            validateAuthConfig(rawUrl = "   ", rawEnv = "DEV")
        }
    }

    @Test
    fun `validateAuthConfig throws IllegalStateException when environment is blank`() {
        assertFailsWith<IllegalStateException> {
            validateAuthConfig(rawUrl = "https://api.example.com", rawEnv = "")
        }
    }

    @Test
    fun `validateAuthConfig throws IllegalStateException when environment is not a valid enum value`() {
        assertFailsWith<IllegalStateException> {
            validateAuthConfig(rawUrl = "https://api.example.com", rawEnv = "INVALID_ENV")
        }
    }

    @Test
    fun `validateAuthConfig returns AuthConfig with DEV environment when inputs are valid`() {
        val config = validateAuthConfig(
            rawUrl = "https://dev.api.example.com",
            rawEnv = "DEV"
        )
        assertEquals("https://dev.api.example.com", config.apiBaseUrl)
        assertEquals(Environment.DEV, config.environment)
    }

    @Test
    fun `validateAuthConfig returns AuthConfig with STAGING environment when inputs are valid`() {
        val config = validateAuthConfig(
            rawUrl = "https://staging.api.example.com",
            rawEnv = "STAGING"
        )
        assertEquals("https://staging.api.example.com", config.apiBaseUrl)
        assertEquals(Environment.STAGING, config.environment)
    }

    @Test
    fun `validateAuthConfig returns AuthConfig with PROD environment when inputs are valid`() {
        val config = validateAuthConfig(
            rawUrl = "https://api.example.com",
            rawEnv = "PROD"
        )
        assertEquals("https://api.example.com", config.apiBaseUrl)
        assertEquals(Environment.PROD, config.environment)
    }

    @Test
    fun `validateAuthConfig does not use Elvis operator — crashes not silently defaults`() {
        // Verify that both blank url AND blank env each individually crash.
        // If any Elvis fallback were present, one of these would silently pass.
        assertFailsWith<IllegalStateException> {
            validateAuthConfig(rawUrl = "", rawEnv = "PROD")
        }
        assertFailsWith<IllegalStateException> {
            validateAuthConfig(rawUrl = "https://api.example.com", rawEnv = "")
        }
    }
}
