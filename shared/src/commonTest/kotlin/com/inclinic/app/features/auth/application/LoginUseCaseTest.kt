package com.inclinic.app.features.auth.application

import com.inclinic.app.features.auth.core.error.AuthError
import com.inclinic.app.features.auth.core.model.AuthTokens
import com.inclinic.app.features.auth.core.model.AuthUser
import com.inclinic.app.features.auth.core.model.LoginCredentials
import com.inclinic.app.features.auth.core.model.LoginResult
import com.inclinic.app.features.auth.core.model.UserRole
import com.inclinic.app.features.auth.fakes.FakeAuthRepository
import com.inclinic.app.features.auth.fakes.FakeTokenStorage
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LoginUseCaseTest {

    private val fakeRepo = FakeAuthRepository()
    private val fakeStorage = FakeTokenStorage()
    private val dispatchers = TestAppDispatchers()

    private val useCase = LoginUseCase(
        repository = fakeRepo,
        tokenStorage = fakeStorage,
        dispatchers = dispatchers,
    )

    // --- Happy path ---

    @Test
    fun success_returns_user() = runTest {
        val expectedUser = AuthUser(
            id = "u1",
            email = "doctor@inclinic.com",
            firstName = "Carlos",
            lastName = "Ramirez",
            role = UserRole.DOCTOR,
        )
        val expectedTokens = AuthTokens("acc", "ref")
        fakeRepo.loginResult = Result.success(LoginResult.Success(expectedUser, expectedTokens))

        val result = useCase(LoginCredentials("doctor@inclinic.com", "secret123"))

        assertTrue(result.isSuccess)
        val loginResult = assertIs<LoginResult.Success>(result.getOrNull())
        assertEquals(expectedUser, loginResult.user)
    }

    @Test
    fun success_persists_tokens_via_token_storage() = runTest {
        val tokens = AuthTokens("access-abc", "refresh-xyz")
        val existingUser = (fakeRepo.loginResult.getOrNull() as LoginResult.Success).user
        fakeRepo.loginResult = Result.success(LoginResult.Success(existingUser, tokens))

        useCase(LoginCredentials("user@test.com", "pass1234"))

        assertEquals(1, fakeStorage.saveCallCount)
        assertEquals(tokens, fakeStorage.current)
    }

    // --- Network failure ---

    @Test
    fun network_failure_returns_error() = runTest {
        fakeRepo.loginResult = Result.failure(AuthError.NetworkError)

        val result = useCase(LoginCredentials("user@test.com", "pass1234"))

        assertTrue(result.isFailure)
        assertIs<AuthError.NetworkError>(result.exceptionOrNull())
    }

    @Test
    fun network_failure_does_not_persist_tokens() = runTest {
        fakeRepo.loginResult = Result.failure(AuthError.NetworkError)

        useCase(LoginCredentials("user@test.com", "pass1234"))

        assertEquals(0, fakeStorage.saveCallCount)
        assertNull(fakeStorage.current)
    }

    // --- Invalid credentials ---

    @Test
    fun invalid_credentials_returns_error() = runTest {
        fakeRepo.loginResult = Result.failure(AuthError.InvalidCredentials)

        val result = useCase(LoginCredentials("user@test.com", "wrongpass"))

        assertTrue(result.isFailure)
        assertIs<AuthError.InvalidCredentials>(result.exceptionOrNull())
    }

    // --- Input validation: email ---

    @Test
    fun empty_email_returns_validation_error_without_calling_repository() = runTest {
        val result = useCase(LoginCredentials("", "pass1234"))

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertIs<AuthError.ValidationError>(error)
        assertEquals(AuthError.ValidationError.Field.EMAIL, error.field)
        assertEquals(AuthError.ValidationError.Kind.INVALID_EMAIL, error.kind)
        assertEquals(0, fakeRepo.loginCallCount)
    }

    @Test
    fun invalid_email_format_returns_validation_error_without_calling_repository() = runTest {
        val result = useCase(LoginCredentials("not-an-email", "pass1234"))

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertIs<AuthError.ValidationError>(error)
        assertEquals(AuthError.ValidationError.Field.EMAIL, error.field)
        assertEquals(0, fakeRepo.loginCallCount)
    }

    @Test
    fun email_missing_at_sign_returns_validation_error() = runTest {
        val result = useCase(LoginCredentials("userdomain.com", "pass1234"))

        assertTrue(result.isFailure)
        assertIs<AuthError.ValidationError>(result.exceptionOrNull())
        assertEquals(0, fakeRepo.loginCallCount)
    }

    // --- Input validation: password ---

    @Test
    fun empty_password_returns_validation_error_without_calling_repository() = runTest {
        val result = useCase(LoginCredentials("user@test.com", ""))

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertIs<AuthError.ValidationError>(error)
        assertEquals(AuthError.ValidationError.Field.PASSWORD, error.field)
        assertEquals(AuthError.ValidationError.Kind.EMPTY_PASSWORD, error.kind)
        assertEquals(0, fakeRepo.loginCallCount)
    }

    // --- Both invalid: email checked first ---

    @Test
    fun both_invalid_returns_email_error_first() = runTest {
        val result = useCase(LoginCredentials("bad-email", ""))

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertIs<AuthError.ValidationError>(error)
        assertEquals(AuthError.ValidationError.Field.EMAIL, error.field)
        assertEquals(0, fakeRepo.loginCallCount)
    }
}
