@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.auth.presentation.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.features.auth.application.RegisterPatientUseCase
import com.inclinic.app.features.auth.core.error.AuthError
import com.inclinic.app.features.auth.fakes.FakeAuthRemoteDataSource
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.auth.infrastructure.remote.AuthRemoteDataSource
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertNull

/**
 * TDD RED → GREEN tests for the 429 (rate-limit) path of [DefaultRegisterPatientComponent].
 *
 * Consistency requirement (design-gap-closure): a 429 during patient registration must
 * route to the same standalone "Estado - 429" experience as Login, instead of showing an
 * inline [RegisterPatientState.serverError] banner. See [DefaultLoginComponent] for the
 * mirrored behavior.
 */
class DefaultRegisterPatientComponentTest {

    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val componentContext = DefaultComponentContext(lifecycle = lifecycle)
    private val dispatchers = TestAppDispatchers()

    private class FakeRemote(
        private val result: Result<Unit>,
    ) : AuthRemoteDataSource by FakeAuthRemoteDataSource() {
        override suspend fun registerPatient(
            firstName: String,
            lastName: String,
            email: String,
            phone: String?,
            password: String,
        ): Result<Unit> = result
    }

    private fun buildComponent(
        result: Result<Unit>,
        onOutput: (RegisterPatientComponent.Output) -> Unit = {},
    ): DefaultRegisterPatientComponent {
        val useCase = RegisterPatientUseCase(remote = FakeRemote(result), dispatchers = dispatchers)
        return DefaultRegisterPatientComponent(
            componentContext = componentContext,
            registerUseCase = useCase,
            dispatchers = dispatchers,
            onOutput = onOutput,
        )
    }

    private fun fillValidForm(component: DefaultRegisterPatientComponent) {
        component.onNameChanged("Juan")
        component.onLastNameChanged("Perez")
        component.onEmailChanged("juan@test.com")
        component.onPasswordChanged("Password1")
        component.onConfirmPasswordChanged("Password1")
    }

    @Test
    fun onSubmit_tooManyAttempts_emits_Output_RateLimited() = runTest {
        var captured: RegisterPatientComponent.Output? = null
        val component = buildComponent(
            result = Result.failure(AuthError.TooManyAttempts),
            onOutput = { captured = it },
        )
        fillValidForm(component)

        component.onSubmit()

        assertIs<RegisterPatientComponent.Output.RateLimited>(captured)
    }

    @Test
    fun onSubmit_tooManyAttempts_does_not_set_inline_serverError() = runTest {
        val component = buildComponent(result = Result.failure(AuthError.TooManyAttempts))
        fillValidForm(component)

        component.onSubmit()

        assertNull(component.state.value.serverError)
    }

    @Test
    fun onSubmit_tooManyAttempts_clears_isLoading() = runTest {
        val component = buildComponent(result = Result.failure(AuthError.TooManyAttempts))
        fillValidForm(component)

        component.onSubmit()

        assertNull(component.state.value.serverError)
        kotlin.test.assertFalse(component.state.value.isLoading)
    }
}
