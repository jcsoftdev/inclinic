@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.auth.presentation.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.features.auth.application.ResendActivationUseCase
import com.inclinic.app.features.auth.fakes.FakeAuthRemoteDataSource
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * 🔴 RED → 🟢 GREEN tests that verify the state the screen will observe.
 *
 * The email interpolation value and callback-driven state mutations are what
 * AccountCreatedScreen renders — validated here at the component level without
 * a Compose runtime dependency.
 */
class AccountCreatedScreenLogicTest {

    private val lifecycle = LifecycleRegistry()
    private val dispatchers = TestAppDispatchers()
    private val fakeRemote = FakeAuthRemoteDataSource()
    private val resendUseCase = ResendActivationUseCase(remote = fakeRemote, dispatchers = dispatchers)

    private fun buildComponent(
        email: String = "juan@paciente.com",
        onOutput: (AccountCreatedComponent.Output) -> Unit = {},
    ): DefaultAccountCreatedComponent {
        lifecycle.resume()
        val ctx = DefaultComponentContext(lifecycle)
        return DefaultAccountCreatedComponent(
            componentContext = ctx,
            email = email,
            resendActivationUseCase = resendUseCase,
            dispatchers = dispatchers,
            onOutput = onOutput,
        )
    }

    // ── Screen renders the correct email ───────────────────────────────────────

    @Test
    fun component_email_matches_what_screen_will_interpolate_in_body() {
        val email = "maria@hospital.pe"
        val component = buildComponent(email = email)

        // The screen body text uses component.email directly — verify it's correct.
        assertEquals(email, component.email)
    }

    // ── "Ir a iniciar sesión" button triggers GoToLogin ────────────────────────

    @Test
    fun clicking_go_to_login_emits_GoToLogin_output() {
        var output: AccountCreatedComponent.Output? = null
        val component = buildComponent(onOutput = { output = it })

        component.onGoToLogin()

        assertTrue(output is AccountCreatedComponent.Output.GoToLogin)
    }

    // ── "Reenviar correo" button triggers ResendEmail ──────────────────────────

    @Test
    fun clicking_resend_emits_ResendEmail_output() = runTest {
        var output: AccountCreatedComponent.Output? = null
        val component = buildComponent(onOutput = { output = it })

        component.onResend()

        assertTrue(output is AccountCreatedComponent.Output.ResendEmail)
    }

    // ── Resent flag controls secondary button label logic ──────────────────────

    @Test
    fun resent_flag_starts_false_so_resend_button_shows_initial_label() {
        val component = buildComponent()
        assertFalse(component.state.value.isResent)
    }

    @Test
    fun after_resend_flag_is_true_so_screen_can_show_confirmation_feedback() = runTest {
        val component = buildComponent()

        component.onResend()

        assertTrue(component.state.value.isResent)
    }
}
