package com.inclinic.app.features.auth.application

import app.cash.turbine.test
import com.inclinic.app.core.events.SessionEvents
import com.inclinic.app.core.events.SessionExpiryReason
import com.inclinic.app.features.auth.fakes.FakeAuthRepository
import com.inclinic.app.features.auth.fakes.FakeTokenStorage
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LogoutUseCaseTest {

    private val fakeRepo = FakeAuthRepository()
    private val fakeStorage = FakeTokenStorage()
    private val dispatchers = TestAppDispatchers()
    private val sessionEvents = SessionEvents()

    private val useCase = LogoutUseCase(
        tokenStorage = fakeStorage,
        sessionEvents = sessionEvents,
        dispatchers = dispatchers,
    )

    @Test
    fun logout_clears_stored_tokens() = runTest {
        // Simulate pre-stored tokens
        fakeStorage.save(
            com.inclinic.app.features.auth.core.model.AuthTokens("acc", "ref")
        )

        useCase()

        assertNull(fakeStorage.current)
        assertEquals(1, fakeStorage.clearCallCount)
    }

    @Test
    fun logout_is_idempotent_on_empty_storage() = runTest {
        // No tokens stored — calling logout should not crash
        useCase()
        useCase()

        assertEquals(2, fakeStorage.clearCallCount)
    }

    @Test
    fun logout_does_not_call_auth_repository() = runTest {
        useCase()

        assertEquals(0, fakeRepo.loginCallCount)
        assertEquals(0, fakeRepo.logoutCallCount)
    }

    // ── Session-expiry reason (design-gap-closure) ──────────────────────────────
    //
    // Explicit logout must be distinguishable from a real 401/token-expiry so the
    // Login screen can stay silent for a deliberate logout but show "tu sesión
    // expiró" only when the session was killed by the server.

    @Test
    fun logout_emits_expired_event_with_USER_INITIATED_reason() = runTest {
        sessionEvents.expired.test {
            useCase()
            assertEquals(SessionExpiryReason.USER_INITIATED, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
