package com.inclinic.app.features.auth.application

import com.inclinic.app.core.events.SessionEvents
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
}
