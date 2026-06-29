@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.core

import app.cash.turbine.test
import com.inclinic.app.core.events.SessionEvents
import com.inclinic.app.core.network.RefreshCoordinator
import com.inclinic.app.features.auth.core.model.AuthTokens
import com.inclinic.app.features.auth.core.port.TokenStorage
import io.ktor.client.plugins.auth.providers.BearerTokens
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * In-memory [TokenStorage] for coordinator tests.
 */
private class InMemoryTokenStorage(initial: AuthTokens? = null) : TokenStorage {
    private var stored: AuthTokens? = initial
    override suspend fun save(tokens: AuthTokens) { stored = tokens }
    override suspend fun load(): AuthTokens? = stored
    override suspend fun clear() { stored = null }
    override suspend fun saveUser(user: com.inclinic.app.features.auth.core.model.AuthUser) {}
    override suspend fun loadUser(): com.inclinic.app.features.auth.core.model.AuthUser? = null
}

class RefreshCoordinatorTest {

    @Test
    fun concurrent_requests_call_refreshCallback_exactly_once() = runTest {
        // Arrange
        val initialTokens = AuthTokens("old-access", "old-refresh")
        val storage = InMemoryTokenStorage(initialTokens)
        val sessionEvents = SessionEvents()

        val refreshCallCount = kotlinx.coroutines.sync.Mutex()
        var counter = 0

        val coordinator = RefreshCoordinator(
            tokenStorage = storage,
            sessionEvents = sessionEvents,
            refreshCall = { _ ->
                // Simulate the lock inside refreshCall to count actual executions
                val mutex = kotlinx.coroutines.sync.Mutex()
                mutex.withLock { counter++ }
                AuthTokens("new-access", "new-refresh")
            },
        )

        // Set up: counter tracking via atomic int pattern
        var refreshCallsMade = 0
        val countingCoordinator = RefreshCoordinator(
            tokenStorage = storage,
            sessionEvents = sessionEvents,
            refreshCall = { _ ->
                refreshCallsMade++
                AuthTokens("new-access", "new-refresh")
            },
        )

        val oldBearer = BearerTokens("old-access", "old-refresh")

        // Act — 5 concurrent coroutines all trigger refresh with the same oldTokens
        val results = (1..5).map {
            async { countingCoordinator.refresh(oldBearer) }
        }.awaitAll()

        // Assert — only 1 refresh call (single-flight via Mutex)
        assertEquals(1, refreshCallsMade,
            "Expected exactly 1 refresh call but got $refreshCallsMade")

        // All results should carry the new token
        results.forEach { tokens ->
            assertNotNull(tokens, "Expected non-null BearerTokens from refresh")
            assertEquals("new-access", tokens.accessToken)
        }
    }

    @Test
    fun second_coroutine_reuses_already_refreshed_token() = runTest {
        // Arrange: storage already has new tokens (simulates first coroutine already refreshed)
        val newTokens = AuthTokens("new-access", "new-refresh")
        val storage = InMemoryTokenStorage(newTokens)
        val sessionEvents = SessionEvents()
        var refreshCallsMade = 0

        val coordinator = RefreshCoordinator(
            tokenStorage = storage,
            sessionEvents = sessionEvents,
            refreshCall = { _ ->
                refreshCallsMade++
                AuthTokens("newer-access", "newer-refresh")
            },
        )

        // oldTokens differs from stored — means a previous coroutine already refreshed
        val staleBearer = BearerTokens("old-access", "old-refresh")

        // Act
        val result = coordinator.refresh(staleBearer)

        // Assert — no actual refresh needed; reuses stored token
        assertEquals(0, refreshCallsMade)
        assertEquals("new-access", result?.accessToken)
    }

    @Test
    fun failed_refresh_clears_storage_and_emits_session_expired() = runTest {
        val initialTokens = AuthTokens("old-access", "old-refresh")
        val storage = InMemoryTokenStorage(initialTokens)
        val sessionEvents = SessionEvents()

        val coordinator = RefreshCoordinator(
            tokenStorage = storage,
            sessionEvents = sessionEvents,
            refreshCall = { _ -> null }, // refresh fails
        )

        sessionEvents.expired.test {
            val result = coordinator.refresh(BearerTokens("old-access", "old-refresh"))

            assertEquals(null, result)
            assertEquals(null, storage.load())
            awaitItem() // session-expired event must be emitted exactly once
            cancelAndIgnoreRemainingEvents()
        }
    }
}
