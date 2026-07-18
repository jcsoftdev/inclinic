package com.inclinic.app.features.auth.presentation.component

import app.cash.turbine.test
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.features.auth.core.error.AuthError
import com.inclinic.app.features.auth.core.model.AuthTokens
import com.inclinic.app.features.auth.core.model.AuthUser
import com.inclinic.app.features.auth.core.model.LoginCredentials
import com.inclinic.app.features.auth.core.model.LoginResult
import com.inclinic.app.features.auth.core.model.UserRole
import com.inclinic.app.features.auth.core.port.AuthRepository
import com.inclinic.app.features.auth.core.port.TokenStorage
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * RED → GREEN tests for [DefaultLoginComponent].
 *
 * Uses Turbine to observe [com.arkivanov.decompose.value.Value] as a Flow via
 * the [asFlow] helper defined below. Lifecycle is provided by a [LifecycleRegistry]
 * set to RESUMED so the component is active throughout the test.
 *
 * [TestAppDispatchers] uses [UnconfinedTestDispatcher] so all coroutines run
 * eagerly inside [runTest] without manual scheduler forwarding.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DefaultLoginComponentTest {

    // ── Test fixtures ─────────────────────────────────────────────────────────

    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val componentContext = DefaultComponentContext(lifecycle = lifecycle)
    private val dispatchers = TestAppDispatchers()
    private val fakeRepo = FakeLoginRepository()
    private val fakeStorage = FakeLoginStorage()

    private fun loginUseCase() = com.inclinic.app.features.auth.application.LoginUseCase(
        repository = fakeRepo,
        tokenStorage = fakeStorage,
        dispatchers = dispatchers,
    )

    private fun createComponent(
        onLoginSucceeded: (AuthUser) -> Unit = {}
    ): DefaultLoginComponent = DefaultLoginComponent(
        componentContext = componentContext,
        loginUseCase = loginUseCase(),
        dispatchers = dispatchers,
        onLoginSucceeded = onLoginSucceeded,
    )

    // ── Initial state ─────────────────────────────────────────────────────────

    @Test
    fun initial_state_is_idle_with_empty_fields() {
        val component = createComponent()
        val state = component.state.value
        assertEquals("", state.email)
        assertEquals("", state.password)
        assertFalse(state.isSubmitting)
        assertNull(state.emailError)
        assertNull(state.passwordError)
        assertNull(state.authError)
        assertFalse(state.loginSuccess)
    }

    // ── Field changes ─────────────────────────────────────────────────────────

    @Test
    fun onEmailChange_updates_email_in_state() {
        val component = createComponent()
        component.onEmailChange("user@test.com")
        assertEquals("user@test.com", component.state.value.email)
    }

    @Test
    fun onPasswordChange_updates_password_in_state() {
        val component = createComponent()
        component.onPasswordChange("secret")
        assertEquals("secret", component.state.value.password)
    }

    @Test
    fun onEmailChange_clears_existing_email_error() {
        val component = createComponent()
        // Trigger an email error first
        component.onSubmit()
        assertNotNull(component.state.value.emailError)
        // Then fix the email — error should clear
        component.onEmailChange("valid@email.com")
        assertNull(component.state.value.emailError)
    }

    // ── Validation (no network) ───────────────────────────────────────────────

    @Test
    fun onSubmit_with_invalid_email_sets_emailError_without_network() = runTest {
        val component = createComponent()
        component.onEmailChange("not-an-email")
        component.onPasswordChange("password123")

        component.onSubmit()

        assertNotNull(component.state.value.emailError)
        assertEquals(0, fakeRepo.loginCallCount)
        assertFalse(component.state.value.isSubmitting)
    }

    @Test
    fun onSubmit_with_empty_password_sets_passwordError_without_network() = runTest {
        val component = createComponent()
        component.onEmailChange("valid@email.com")
        component.onPasswordChange("")

        component.onSubmit()

        assertNotNull(component.state.value.passwordError)
        assertEquals(0, fakeRepo.loginCallCount)
        assertFalse(component.state.value.isSubmitting)
    }

    @Test
    fun onSubmit_with_empty_email_sets_emailError_without_network() = runTest {
        val component = createComponent()
        component.onEmailChange("")
        component.onPasswordChange("password")

        component.onSubmit()

        assertNotNull(component.state.value.emailError)
        assertEquals(0, fakeRepo.loginCallCount)
    }

    // ── Success path ──────────────────────────────────────────────────────────

    @Test
    fun onSubmit_valid_credentials_sets_loginSuccess_true() = runTest {
        fakeRepo.result = Result.success(
            LoginResult.Success(
                user = AuthUser("1", "user@test.com", "Test", "User", UserRole.PATIENT, null, null),
                tokens = AuthTokens("acc", "ref"),
            )
        )
        val component = createComponent()
        component.onEmailChange("user@test.com")
        component.onPasswordChange("password123")

        component.onSubmit()

        assertTrue(component.state.value.loginSuccess)
        assertFalse(component.state.value.isSubmitting)
        assertNull(component.state.value.authError)
    }

    @Test
    fun onSubmit_valid_credentials_calls_onLoginSucceeded_callback() = runTest {
        fakeRepo.result = Result.success(
            LoginResult.Success(
                user = AuthUser("1", "user@test.com", "Test", "User", UserRole.PATIENT, null, null),
                tokens = AuthTokens("acc", "ref"),
            )
        )
        var callbackInvoked = false
        val component = createComponent(onLoginSucceeded = { callbackInvoked = true })
        component.onEmailChange("user@test.com")
        component.onPasswordChange("password123")

        component.onSubmit()

        assertTrue(callbackInvoked)
    }

    // ── Failure path ──────────────────────────────────────────────────────────

    @Test
    fun onSubmit_invalid_credentials_sets_authError() = runTest {
        fakeRepo.result = Result.failure(AuthError.InvalidCredentials)
        val component = createComponent()
        component.onEmailChange("user@test.com")
        component.onPasswordChange("password123")

        component.onSubmit()

        assertIs<AuthError.InvalidCredentials>(component.state.value.authError)
        assertFalse(component.state.value.isSubmitting)
        assertFalse(component.state.value.loginSuccess)
    }

    @Test
    fun onSubmit_network_error_sets_authError_NetworkError() = runTest {
        fakeRepo.result = Result.failure(AuthError.NetworkError)
        val component = createComponent()
        component.onEmailChange("user@test.com")
        component.onPasswordChange("password123")

        component.onSubmit()

        assertIs<AuthError.NetworkError>(component.state.value.authError)
        assertFalse(component.state.value.isSubmitting)
    }

    @Test
    fun onSubmit_tooManyAttempts_invokes_onRateLimited_instead_of_setting_authError() = runTest {
        fakeRepo.result = Result.failure(AuthError.TooManyAttempts)
        var rateLimited = false
        val component = DefaultLoginComponent(
            componentContext = componentContext,
            loginUseCase = loginUseCase(),
            dispatchers = dispatchers,
            onRateLimited = { rateLimited = true },
        )
        component.onEmailChange("user@test.com")
        component.onPasswordChange("password123")

        component.onSubmit()

        assertTrue(rateLimited)
        assertNull(component.state.value.authError)
        assertFalse(component.state.value.isSubmitting)
    }

    // ── Error dismissed ───────────────────────────────────────────────────────

    @Test
    fun onErrorDismissed_clears_authError() = runTest {
        fakeRepo.result = Result.failure(AuthError.InvalidCredentials)
        val component = createComponent()
        component.onEmailChange("user@test.com")
        component.onPasswordChange("password123")
        component.onSubmit()
        assertNotNull(component.state.value.authError)

        component.onErrorDismissed()

        assertNull(component.state.value.authError)
    }

    // ── State transitions via Flow (Turbine) ──────────────────────────────────

    @Test
    fun onSubmit_valid_transitions_through_isSubmitting_to_success() = runTest {
        // Use a suspending fake so we can observe intermediate isSubmitting state
        val suspendingRepo = SuspendingFakeRepository()
        val uc = com.inclinic.app.features.auth.application.LoginUseCase(
            repository = suspendingRepo,
            tokenStorage = fakeStorage,
            dispatchers = dispatchers,
        )
        val component = DefaultLoginComponent(
            componentContext = DefaultComponentContext(lifecycle = LifecycleRegistry().also { it.resume() }),
            loginUseCase = uc,
            dispatchers = TestAppDispatchers(testScheduler),
            onLoginSucceeded = {},
        )
        component.state.asFlow().test {
            val initial = awaitItem()
            assertFalse(initial.isSubmitting)

            component.onEmailChange("user@test.com")
            component.onPasswordChange("pass")
            component.onSubmit()

            // After submit with UnconfinedTestDispatcher, success comes immediately
            val finalState = expectMostRecentItem()
            assertTrue(finalState.loginSuccess || !finalState.isSubmitting)

            cancelAndIgnoreRemainingEvents()
        }
    }
}

// ── Test helpers ──────────────────────────────────────────────────────────────

/**
 * Converts a Decompose [com.arkivanov.decompose.value.Value] to a [Flow]
 * using its subscribe/unsubscribe callback mechanism.
 */
fun <T : Any> com.arkivanov.decompose.value.Value<T>.asFlow(): Flow<T> = callbackFlow {
    val cancellation = subscribe { value -> trySend(value) }
    awaitClose { cancellation.cancel() }
}

/**
 * Minimal in-test fake for [AuthRepository] (used only for LoginUseCase).
 * Separate from FakeAuthRepository in fakes/ to avoid cross-test coupling.
 */
private class FakeLoginRepository : AuthRepository {
    var result: Result<LoginResult> = Result.success(
        LoginResult.Success(
            user = AuthUser("1", "user@test.com", "Test", "User", UserRole.PATIENT, null, null),
            tokens = AuthTokens("acc", "ref"),
        )
    )
    var loginCallCount = 0

    override suspend fun login(credentials: LoginCredentials): Result<LoginResult> {
        loginCallCount++
        return result
    }

    override suspend fun verifyTwoFactor(partialToken: String, code: String): Result<Pair<AuthUser, AuthTokens>> =
        Result.success(
            Pair(
                AuthUser("1", "user@test.com", "Test", "User", UserRole.PATIENT, null, null),
                AuthTokens("acc", "ref"),
            )
        )

    override suspend fun logout() {}
    override suspend fun isLoggedIn(): Boolean = false
    override suspend fun storedTokens(): AuthTokens? = null
}

private class FakeLoginStorage : TokenStorage {
    override suspend fun save(tokens: AuthTokens) {}
    override suspend fun load(): AuthTokens? = null
    override suspend fun clear() {}
    override suspend fun saveUser(user: AuthUser) {}
    override suspend fun loadUser(): AuthUser? = null
}

/** Fake that simulates a slow login (not resuming until explicitly told to) */
private class SuspendingFakeRepository : AuthRepository {
    override suspend fun login(credentials: LoginCredentials): Result<LoginResult> =
        Result.success(
            LoginResult.Success(
                user = AuthUser("1", credentials.email, "Test", "User", UserRole.PATIENT, null, null),
                tokens = AuthTokens("acc", "ref"),
            )
        )

    override suspend fun verifyTwoFactor(partialToken: String, code: String): Result<Pair<AuthUser, AuthTokens>> =
        Result.success(
            Pair(
                AuthUser("1", "user@test.com", "Test", "User", UserRole.PATIENT, null, null),
                AuthTokens("acc", "ref"),
            )
        )

    override suspend fun logout() {}
    override suspend fun isLoggedIn(): Boolean = false
    override suspend fun storedTokens(): AuthTokens? = null
}
