package com.inclinic.app.features.auth.infrastructure

import com.inclinic.app.features.auth.core.error.AuthError
import com.inclinic.app.features.auth.core.model.AuthTokens
import com.inclinic.app.features.auth.core.model.LoginCredentials
import com.inclinic.app.features.auth.core.model.LoginResult
import com.inclinic.app.features.auth.fakes.FakeAuthRemoteDataSource
import com.inclinic.app.features.auth.fakes.FakeTokenStorage
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * TDD RED → GREEN tests for [DefaultAuthRepository].
 *
 * Uses [FakeAuthRemoteDataSource] and [FakeTokenStorage] — no real network or storage.
 * [TestAppDispatchers] provides deterministic coroutine scheduling via UnconfinedTestDispatcher.
 */
class DefaultAuthRepositoryTest {

    private val remoteDs = FakeAuthRemoteDataSource()
    private val tokenStorage = FakeTokenStorage()
    private val dispatchers = TestAppDispatchers()

    private val repo = DefaultAuthRepository(
        remote = remoteDs,
        local = tokenStorage,
        dispatchers = dispatchers,
    )

    private val credentials = LoginCredentials(email = "doc@inclinic.com", password = "secret123")

    // ── login() success ───────────────────────────────────────────────────────

    @Test
    fun login_success_calls_remote_with_mapped_request_and_returns_tokens_and_user() = runTest {
        val result = repo.login(credentials)

        assertTrue(result.isSuccess)

        // Remote was called exactly once with the right DTO.
        assertEquals(1, remoteDs.loginCallCount)
        assertEquals(credentials.email, remoteDs.lastRequest?.email)
        assertEquals(credentials.password, remoteDs.lastRequest?.password)

        // Result contains both user and tokens wrapped in LoginResult.Success.
        val loginResult = assertIs<LoginResult.Success>(result.getOrThrow())
        assertEquals("user-1", loginResult.user.id)
        assertEquals("fake-access", loginResult.tokens.accessToken)
        assertEquals("fake-refresh", loginResult.tokens.refreshToken)
    }

    @Test
    fun login_success_does_NOT_persist_tokens_in_repository() = runTest {
        // Contract: LoginUseCase persists tokens — DefaultAuthRepository must NOT.
        repo.login(credentials)

        assertEquals(0, tokenStorage.saveCallCount,
            "DefaultAuthRepository.login() must not call TokenStorage.save() — LoginUseCase owns persistence")
    }

    // ── login() failure ───────────────────────────────────────────────────────

    @Test
    fun login_remote_failure_returns_failure_result_wrapping_auth_error() = runTest {
        remoteDs.loginResult = Result.failure(AuthError.InvalidCredentials)

        val result = repo.login(credentials)

        assertTrue(result.isFailure)
        assertIs<AuthError.InvalidCredentials>(result.exceptionOrNull())
    }

    @Test
    fun login_failure_does_not_persist_tokens() = runTest {
        remoteDs.loginResult = Result.failure(AuthError.NetworkError)

        repo.login(credentials)

        assertEquals(0, tokenStorage.saveCallCount)
    }

    // ── logout() ─────────────────────────────────────────────────────────────

    @Test
    fun logout_calls_token_storage_clear() = runTest {
        repo.logout()

        assertEquals(1, tokenStorage.clearCallCount)
    }

    @Test
    fun logout_does_not_call_remote() = runTest {
        repo.logout()

        assertEquals(0, remoteDs.loginCallCount)
    }

    // ── storedTokens() ────────────────────────────────────────────────────────

    @Test
    fun storedTokens_returns_null_when_storage_is_empty() = runTest {
        assertNull(repo.storedTokens())
    }

    @Test
    fun storedTokens_delegates_to_token_storage_load() = runTest {
        val expected = AuthTokens(accessToken = "stored-acc", refreshToken = "stored-ref")
        tokenStorage.save(expected)

        assertEquals(expected, repo.storedTokens())
    }

    // ── isLoggedIn() ──────────────────────────────────────────────────────────

    @Test
    fun isLoggedIn_returns_false_when_no_tokens() = runTest {
        assertFalse(repo.isLoggedIn())
    }

    @Test
    fun isLoggedIn_returns_true_when_tokens_present() = runTest {
        tokenStorage.save(AuthTokens(accessToken = "a", refreshToken = "r"))
        assertTrue(repo.isLoggedIn())
    }
}
