@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.auth.presentation.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.features.auth.application.ResendActivationUseCase
import com.inclinic.app.features.auth.fakes.FakeAuthRemoteDataSource
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.auth.infrastructure.remote.AuthRemoteDataSource
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Unit tests for [DefaultAccountCreatedComponent].
 *
 * Validates that:
 * - [AccountCreatedComponent.onResend] invokes [ResendActivationUseCase] with the component email.
 * - On success the [AccountCreatedState.isResent] flag is flipped and [AccountCreatedComponent.Output.ResendEmail] is emitted.
 * - On failure the flag stays false so the user can retry.
 */
class DefaultAccountCreatedComponentTest {

    private val lifecycle = LifecycleRegistry()
    private val dispatchers = TestAppDispatchers()

    /**
     * Minimal fake that tracks invocation and lets tests configure the result.
     */
    private inner class FakeResendUseCase(
        private val result: Result<Unit> = Result.success(Unit),
    ) : AuthRemoteDataSource by FakeAuthRemoteDataSource() {
        var callCount = 0
        var lastEmail: String? = null

        val useCase = ResendActivationUseCase(
            remote = this,
            dispatchers = dispatchers,
        )

        override suspend fun resendActivation(email: String): Result<Unit> {
            callCount++
            lastEmail = email
            return result
        }
    }

    private fun buildComponent(
        email: String = "test@inclinic.com",
        fake: FakeResendUseCase = FakeResendUseCase(),
        onOutput: (AccountCreatedComponent.Output) -> Unit = {},
    ): Pair<DefaultAccountCreatedComponent, FakeResendUseCase> {
        lifecycle.resume()
        val ctx = DefaultComponentContext(lifecycle)
        val component = DefaultAccountCreatedComponent(
            componentContext = ctx,
            email = email,
            resendActivationUseCase = fake.useCase,
            dispatchers = dispatchers,
            onOutput = onOutput,
        )
        return component to fake
    }

    // ── email property ─────────────────────────────────────────────────────────

    @Test
    fun email_property_exposes_the_value_passed_in_constructor() {
        val (component) = buildComponent(email = "patient@example.com")
        assertEquals("patient@example.com", component.email)
    }

    // ── onGoToLogin ────────────────────────────────────────────────────────────

    @Test
    fun onGoToLogin_emits_Output_GoToLogin() = runTest {
        var captured: AccountCreatedComponent.Output? = null
        val (component) = buildComponent(onOutput = { captured = it })

        component.onGoToLogin()

        assertIs<AccountCreatedComponent.Output.GoToLogin>(captured)
    }

    // ── onResend — use-case invocation ─────────────────────────────────────────

    @Test
    fun onResend_invokes_ResendActivationUseCase_with_correct_email() = runTest {
        val fake = FakeResendUseCase()
        val (component) = buildComponent(email = "patient@inclinic.com", fake = fake)

        component.onResend()

        assertEquals(1, fake.callCount)
        assertEquals("patient@inclinic.com", fake.lastEmail)
    }

    @Test
    fun onResend_on_success_emits_Output_ResendEmail() = runTest {
        var captured: AccountCreatedComponent.Output? = null
        val (component) = buildComponent(onOutput = { captured = it })

        component.onResend()

        assertIs<AccountCreatedComponent.Output.ResendEmail>(captured)
    }

    @Test
    fun onResend_on_success_flips_resent_flag_in_state() = runTest {
        val (component) = buildComponent()

        assertFalse(component.state.value.isResent)

        component.onResend()

        assertTrue(component.state.value.isResent)
    }

    @Test
    fun onResend_on_failure_does_not_flip_resent_flag() = runTest {
        val fake = FakeResendUseCase(result = Result.failure(Exception("network error")))
        val (component) = buildComponent(fake = fake)

        component.onResend()

        assertFalse(component.state.value.isResent)
    }

    @Test
    fun onResend_is_idempotent_after_success() = runTest {
        val fake = FakeResendUseCase()
        val (component) = buildComponent(fake = fake)

        component.onResend()
        component.onResend() // second tap should be a no-op (isResent guard)

        assertEquals(1, fake.callCount)
    }

    // ── initial state ──────────────────────────────────────────────────────────

    @Test
    fun initial_state_has_isResent_false() {
        val (component) = buildComponent()
        assertFalse(component.state.value.isResent)
    }
}
