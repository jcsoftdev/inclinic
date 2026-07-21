@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.auth.presentation.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.features.auth.application.ForgotPasswordUseCase
import com.inclinic.app.features.auth.core.error.AuthError
import com.inclinic.app.features.auth.fakes.FakeAuthRemoteDataSource
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.auth.infrastructure.remote.AuthRemoteDataSource
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull

/**
 * TDD RED → GREEN tests for the 429 (rate-limit) path of [DefaultForgotPasswordComponent].
 *
 * Consistency requirement (design-gap-closure): a 429 during forgot-password must route to
 * the same standalone "Estado - 429" experience as Login, instead of falling into the
 * "always show success" security branch or an inline error banner.
 */
class DefaultForgotPasswordComponentTest {

    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val componentContext = DefaultComponentContext(lifecycle = lifecycle)
    private val dispatchers = TestAppDispatchers()

    private class FakeRemote(
        private val result: Result<Unit>,
    ) : AuthRemoteDataSource by FakeAuthRemoteDataSource() {
        override suspend fun forgotPassword(email: String): Result<Unit> = result
    }

    private fun buildComponent(
        result: Result<Unit>,
        onOutput: (ForgotPasswordComponent.Output) -> Unit = {},
    ): DefaultForgotPasswordComponent {
        val useCase = ForgotPasswordUseCase(remote = FakeRemote(result), dispatchers = dispatchers)
        return DefaultForgotPasswordComponent(
            componentContext = componentContext,
            forgotPasswordUseCase = useCase,
            dispatchers = dispatchers,
            onOutput = onOutput,
        )
    }

    @Test
    fun onSubmit_tooManyAttempts_emits_Output_RateLimited() = runTest {
        var captured: ForgotPasswordComponent.Output? = null
        val component = buildComponent(
            result = Result.failure(AuthError.TooManyAttempts),
            onOutput = { captured = it },
        )
        component.onEmailChanged("user@test.com")

        component.onSubmit()

        assertIs<ForgotPasswordComponent.Output.RateLimited>(captured)
    }

    @Test
    fun onSubmit_tooManyAttempts_does_not_flip_isSent_to_true() = runTest {
        val component = buildComponent(result = Result.failure(AuthError.TooManyAttempts))
        component.onEmailChanged("user@test.com")

        component.onSubmit()

        assertFalse(component.state.value.isSent)
    }

    @Test
    fun onSubmit_tooManyAttempts_does_not_set_inline_error() = runTest {
        val component = buildComponent(result = Result.failure(AuthError.TooManyAttempts))
        component.onEmailChanged("user@test.com")

        component.onSubmit()

        assertNull(component.state.value.error)
    }
}
