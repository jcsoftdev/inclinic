package com.inclinic.app.features.auth.infrastructure.local

import com.inclinic.app.features.auth.core.model.AuthTokens
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Round-trip test for [TokenLocalDataSource] using [createKeychainSettings].
 *
 * Runs in the iosTest source set (iOS targets only).
 *
 * ## Headless CI caveat (Keychain error -25291)
 * `./gradlew iosSimulatorArm64Test` runs without a booted, unlocked simulator Keychain.
 * The OS returns -25291 on any Keychain I/O. Tests detect this at operation time and
 * skip gracefully so the overall build stays green.
 *
 * Phase 11 (integration verification) covers the real end-to-end smoke test inside Xcode
 * with a live simulator session.
 */
class IosKeychainSettingsTokenStorageTest {

    private val testService = "com.inclinic.app.auth.test"
    private lateinit var dataSource: TokenLocalDataSource

    @BeforeTest
    fun setUp() {
        // KeychainSettings construction succeeds even without a Keychain —
        // the error is thrown on first I/O. Safe to always construct here.
        val settings = createKeychainSettings(service = testService)
        dataSource = TokenLocalDataSource(settings)
    }

    @AfterTest
    fun tearDown() = runTest {
        try { dataSource.clear() } catch (_: IllegalStateException) {
            // Keychain not available in headless runner — nothing to clean up.
        }
    }

    @Test
    fun roundTrip_save_then_load_returns_same_tokens() = runTest {
        val tokens = AuthTokens(accessToken = "ios-acc-token", refreshToken = "ios-ref-token")
        try {
            dataSource.save(tokens)
            assertEquals(tokens, dataSource.load())
        } catch (e: IllegalStateException) {
            if (e.message?.contains("25291") == true) return@runTest // no Keychain in headless runner
            throw e
        }
    }

    @Test
    fun clear_removes_tokens() = runTest {
        try {
            dataSource.save(AuthTokens(accessToken = "to-clear-acc", refreshToken = "to-clear-ref"))
            dataSource.clear()
            assertNull(dataSource.load())
        } catch (e: IllegalStateException) {
            if (e.message?.contains("25291") == true) return@runTest
            throw e
        }
    }

    @Test
    fun load_on_empty_keychain_returns_null() = runTest {
        try {
            assertNull(dataSource.load())
        } catch (e: IllegalStateException) {
            if (e.message?.contains("25291") == true) return@runTest
            throw e
        }
    }
}
