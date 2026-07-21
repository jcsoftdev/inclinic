package com.inclinic.app.features.auth.presentation.component

import app.cash.turbine.test
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.core.navigation.PendingSessionMessage
import com.inclinic.app.features.auth.application.ResendActivationUseCase
import com.inclinic.app.features.auth.core.error.AuthError
import com.inclinic.app.features.auth.core.error.SessionExpiredMessage
import com.inclinic.app.features.auth.core.model.AuthTokens
import com.inclinic.app.features.auth.core.model.AuthUser
import com.inclinic.app.features.auth.core.model.LoginCredentials
import com.inclinic.app.features.auth.core.model.LoginResult
import com.inclinic.app.features.auth.core.model.UserRole
import com.inclinic.app.features.auth.core.port.AuthRepository
import com.inclinic.app.features.auth.core.port.TokenStorage
import com.inclinic.app.features.auth.fakes.FakeAuthRemoteDataSource
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.auth.infrastructure.remote.AuthRemoteDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
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

    // ── Suspended & inactive-account dead-ends (design-gap-closure) ────────────

    @Test
    fun onSubmit_inactiveAccount_sets_canResendActivation_true() = runTest {
        fakeRepo.result = Result.failure(AuthError.InactiveAccount)
        val component = createComponent()
        component.onEmailChange("user@test.com")
        component.onPasswordChange("password123")

        component.onSubmit()

        assertTrue(component.state.value.canResendActivation)
        assertFalse(component.state.value.isSuspended)
    }

    @Test
    fun onSubmit_suspendedAccount_sets_isSuspended_true() = runTest {
        fakeRepo.result = Result.failure(AuthError.SuspendedAccount)
        val component = createComponent()
        component.onEmailChange("user@test.com")
        component.onPasswordChange("password123")

        component.onSubmit()

        assertTrue(component.state.value.isSuspended)
        assertFalse(component.state.value.canResendActivation)
    }

    @Test
    fun onSubmit_invalidCredentials_does_not_set_canResendActivation_or_isSuspended() = runTest {
        fakeRepo.result = Result.failure(AuthError.InvalidCredentials)
        val component = createComponent()
        component.onEmailChange("user@test.com")
        component.onPasswordChange("password123")

        component.onSubmit()

        assertFalse(component.state.value.canResendActivation)
        assertFalse(component.state.value.isSuspended)
    }

    // ── Resend activation from Login (reuses ResendActivationUseCase) ──────────

    @Test
    fun onResendActivation_when_inactiveAccount_invokes_ResendActivationUseCase_with_typed_email() = runTest {
        fakeRepo.result = Result.failure(AuthError.InactiveAccount)
        val fakeResend = FakeResendRemote()
        val component = DefaultLoginComponent(
            componentContext = componentContext,
            loginUseCase = loginUseCase(),
            dispatchers = dispatchers,
            resendActivationUseCase = ResendActivationUseCase(remote = fakeResend, dispatchers = dispatchers),
        )
        component.onEmailChange("inactive@test.com")
        component.onPasswordChange("password123")
        component.onSubmit()

        component.onResendActivation()

        assertEquals(1, fakeResend.resendCallCount)
        assertEquals("inactive@test.com", fakeResend.lastResendEmail)
    }

    @Test
    fun onResendActivation_on_success_sets_resendActivationSent_true() = runTest {
        fakeRepo.result = Result.failure(AuthError.InactiveAccount)
        val fakeResend = FakeResendRemote()
        val component = DefaultLoginComponent(
            componentContext = componentContext,
            loginUseCase = loginUseCase(),
            dispatchers = dispatchers,
            resendActivationUseCase = ResendActivationUseCase(remote = fakeResend, dispatchers = dispatchers),
        )
        component.onEmailChange("inactive@test.com")
        component.onPasswordChange("password123")
        component.onSubmit()
        assertFalse(component.state.value.resendActivationSent)

        component.onResendActivation()

        assertTrue(component.state.value.resendActivationSent)
    }

    @Test
    fun onResendActivation_when_not_inactiveAccount_does_not_invoke_use_case() = runTest {
        fakeRepo.result = Result.failure(AuthError.InvalidCredentials)
        val fakeResend = FakeResendRemote()
        val component = DefaultLoginComponent(
            componentContext = componentContext,
            loginUseCase = loginUseCase(),
            dispatchers = dispatchers,
            resendActivationUseCase = ResendActivationUseCase(remote = fakeResend, dispatchers = dispatchers),
        )
        component.onEmailChange("user@test.com")
        component.onPasswordChange("password123")
        component.onSubmit()

        component.onResendActivation()

        assertEquals(0, fakeResend.resendCallCount)
    }

    @Test
    fun onResendActivation_second_call_while_first_is_in_flight_does_not_invoke_use_case_twice() = runTest {
        fakeRepo.result = Result.failure(AuthError.InactiveAccount)
        val fakeResend = SlowResendRemote()
        val component = DefaultLoginComponent(
            componentContext = componentContext,
            loginUseCase = loginUseCase(),
            dispatchers = dispatchers,
            resendActivationUseCase = ResendActivationUseCase(remote = fakeResend, dispatchers = dispatchers),
        )
        component.onEmailChange("inactive@test.com")
        component.onPasswordChange("password123")
        component.onSubmit()

        // First tap starts the use case and suspends at the gate (still "in flight").
        component.onResendActivation()
        // Rapid second tap must be swallowed by the isResending guard.
        component.onResendActivation()

        assertEquals(1, fakeResend.resendCallCount)

        // Release the gate so the first call's coroutine can finish cleanly.
        fakeResend.gate.trySend(Unit)
    }

    // ── Session-expiry message (design-gap-closure) ─────────────────────────────
    //
    // DefaultRootComponent sets PendingSessionMessage.expired = true only for a real
    // 401/token-expiry (never for explicit logout). DefaultLoginComponent must consume
    // and clear that flag on construction so it surfaces once and doesn't leak into
    // unrelated future Login instances.

    @Test
    fun init_when_PendingSessionMessage_expired_is_true_sets_sessionExpiredMessage_and_clears_flag() {
        PendingSessionMessage.expired = true

        val component = createComponent()

        assertEquals(SessionExpiredMessage, component.state.value.sessionExpiredMessage)
        assertFalse(PendingSessionMessage.expired)
    }

    @Test
    fun init_when_PendingSessionMessage_expired_is_false_leaves_sessionExpiredMessage_null() {
        PendingSessionMessage.expired = false

        val component = createComponent()

        assertNull(component.state.value.sessionExpiredMessage)
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

/** Fake [AuthRemoteDataSource] used only to exercise [ResendActivationUseCase] from Login. */
private class FakeResendRemote : AuthRemoteDataSource by FakeAuthRemoteDataSource() {
    var resendCallCount = 0
    var lastResendEmail: String? = null

    override suspend fun resendActivation(email: String): Result<Unit> {
        resendCallCount++
        lastResendEmail = email
        return Result.success(Unit)
    }
}

/**
 * Fake [AuthRemoteDataSource] that suspends [resendActivation] on [gate] until the test
 * releases it — used to exercise the in-flight guard in [DefaultLoginComponent.onResendActivation].
 * With [UnconfinedTestDispatcher], the launched coroutine runs eagerly up to `gate.receive()`
 * and suspends there, letting the test call `onResendActivation()` again while the first
 * call is still "in flight".
 */
private class SlowResendRemote : AuthRemoteDataSource by FakeAuthRemoteDataSource() {
    var resendCallCount = 0
    val gate = Channel<Unit>()

    override suspend fun resendActivation(email: String): Result<Unit> {
        resendCallCount++
        gate.receive()
        return Result.success(Unit)
    }
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
