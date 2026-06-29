package com.inclinic.app.features.auth.infrastructure.local

import com.inclinic.app.features.auth.core.model.AuthTokens
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * TDD RED → GREEN tests for [TokenLocalDataSource].
 *
 * Uses [MapSettings] (from multiplatform-settings-test) as an in-memory [Settings]
 * implementation — no platform dependencies, runs on all targets.
 */
class TokenLocalDataSourceTest {

    private fun makeDataSource(settings: MapSettings = MapSettings()): TokenLocalDataSource =
        TokenLocalDataSource(settings)

    // ── save → load returns same tokens ──────────────────────────────────────

    @Test
    fun save_then_load_returns_same_tokens() = runTest {
        val ds = makeDataSource()
        val tokens = AuthTokens(accessToken = "acc-1", refreshToken = "ref-1")

        ds.save(tokens)

        assertEquals(tokens, ds.load())
    }

    // ── clear → load returns null ─────────────────────────────────────────────

    @Test
    fun clear_then_load_returns_null() = runTest {
        val ds = makeDataSource()
        val tokens = AuthTokens(accessToken = "acc-2", refreshToken = "ref-2")

        ds.save(tokens)
        ds.clear()

        assertNull(ds.load())
    }

    // ── save twice → load returns latest ─────────────────────────────────────

    @Test
    fun save_twice_load_returns_latest() = runTest {
        val ds = makeDataSource()
        ds.save(AuthTokens(accessToken = "old-acc", refreshToken = "old-ref"))

        val latest = AuthTokens(accessToken = "new-acc", refreshToken = "new-ref")
        ds.save(latest)

        assertEquals(latest, ds.load())
    }

    // ── load without prior save returns null ─────────────────────────────────

    @Test
    fun load_without_prior_save_returns_null() = runTest {
        val ds = makeDataSource()
        assertNull(ds.load())
    }

    // ── save → clear → save → load returns last saved ────────────────────────

    @Test
    fun save_clear_save_returns_last_saved() = runTest {
        val ds = makeDataSource()
        ds.save(AuthTokens(accessToken = "first-acc", refreshToken = "first-ref"))
        ds.clear()

        val second = AuthTokens(accessToken = "second-acc", refreshToken = "second-ref")
        ds.save(second)

        assertEquals(second, ds.load())
    }

    // ── partial state: only access key present → load returns null and cleans orphan ──

    @Test
    fun partial_state_only_access_key_load_returns_null_and_cleans_up() = runTest {
        val settings = MapSettings()
        // Manually poison: write only the access key — simulates partial write crash scenario.
        settings.putString("auth.accessToken", "orphan-acc")

        val ds = TokenLocalDataSource(settings)
        val result = ds.load()

        assertNull(result, "load() must return null when only one token key is present")
        // The orphan must be cleaned up so a subsequent load is still null
        assertNull(ds.load(), "second load must still return null after cleanup")
    }

    // ── partial state: only refresh key present → null + cleanup ─────────────

    @Test
    fun partial_state_only_refresh_key_load_returns_null_and_cleans_up() = runTest {
        val settings = MapSettings()
        settings.putString("auth.refreshToken", "orphan-ref")

        val ds = TokenLocalDataSource(settings)

        assertNull(ds.load())
        assertNull(ds.load())
    }
}
