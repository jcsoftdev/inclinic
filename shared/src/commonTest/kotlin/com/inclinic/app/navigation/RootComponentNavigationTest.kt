@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.navigation

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.core.events.SessionEvents
import com.inclinic.app.core.events.SessionExpiryReason
import com.inclinic.app.core.navigation.DefaultRootComponent
import com.inclinic.app.core.navigation.PendingSessionMessage
import com.inclinic.app.core.navigation.RootConfig
import com.inclinic.app.features.auth.application.GetStoredTokensUseCase
import com.inclinic.app.features.auth.fakes.FakeTokenStorage
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.auth.presentation.component.AccountCreatedComponent
import com.inclinic.app.features.auth.presentation.component.AccountCreatedState
import com.inclinic.app.features.auth.presentation.component.ActivateComponent
import com.inclinic.app.features.auth.presentation.component.ActivateState
import com.inclinic.app.features.auth.presentation.component.ForgotPasswordComponent
import com.inclinic.app.features.auth.presentation.component.ForgotPasswordState
import com.inclinic.app.features.auth.presentation.component.LoginComponent
import com.inclinic.app.features.auth.presentation.component.LoginState
import com.inclinic.app.features.auth.presentation.component.RegisterDoctorComponent
import com.inclinic.app.features.auth.presentation.component.RegisterDoctorState
import com.inclinic.app.features.auth.presentation.component.RegisterPatientComponent
import com.inclinic.app.features.auth.presentation.component.RegisterPatientState
import com.inclinic.app.features.auth.presentation.component.ResetPasswordComponent
import com.inclinic.app.features.auth.presentation.component.ResetPasswordState
import com.inclinic.app.features.auth.presentation.component.TwoFactorVerifyComponent
import com.inclinic.app.features.auth.presentation.component.TwoFactorVerifyState
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Navigation unit tests for [DefaultRootComponent].
 *
 * Focus: initial state + session-expired transition.
 * These tests do NOT exercise the full splash→auth→patient chain because
 * that requires a running HTTP server; instead they verify the two navigation
 * invariants that don't depend on remote data.
 */
class RootComponentNavigationTest {

    private val lifecycle = LifecycleRegistry()
    private val fakeTokenStorage = FakeTokenStorage()
    private val dispatchers = TestAppDispatchers()
    private val sessionEvents = SessionEvents()

    private val getStoredTokens = GetStoredTokensUseCase(fakeTokenStorage, dispatchers)

    // PendingSessionMessage is a process-global mutable — reset it after every test so a
    // test that sets `expired = true` (e.g. session_expired_with_EXPIRED_reason_...) can't
    // leak that flag into unrelated tests that run afterward, in this class or others.
    @AfterTest
    fun resetPendingSessionMessage() {
        PendingSessionMessage.expired = false
    }

    private fun buildRoot(
        localDispatchers: TestAppDispatchers = dispatchers,
    ): DefaultRootComponent {
        val localGetStoredTokens = GetStoredTokensUseCase(fakeTokenStorage, localDispatchers)
        lifecycle.resume()
        val ctx = DefaultComponentContext(lifecycle)
        return DefaultRootComponent(
            componentContext = ctx,
            dispatchers = localDispatchers,
            sessionEvents = sessionEvents,
            getStoredTokens = localGetStoredTokens,
            tokenStorage = fakeTokenStorage,
            loginComponentFactory = { _, _, _, _, _, _ -> StubLoginComponent() },
            twoFactorVerifyComponentFactory = { _, _, _, _ -> StubTwoFactorVerifyComponent() },
            registerPatientComponentFactory = { _, _ -> StubRegisterPatientComponent() },
            registerDoctorComponentFactory = { _, _ -> StubRegisterDoctorComponent() },
            activateComponentFactory = { _, _, _ -> StubActivateComponent() },
            accountCreatedComponentFactory = { _, _, _ -> StubAccountCreatedComponent() },
            forgotPasswordComponentFactory = { _, _ -> StubForgotPasswordComponent() },
            resetPasswordComponentFactory = { _, _, _ -> StubResetPasswordComponent() },
            patientFlowComponentFactory = null,
            doctorFlowComponentFactory = null,
        )
    }

    @Test
    fun initial_configuration_is_Splash() = runTest {
        // StandardTestDispatcher prevents the splash coroutine from running eagerly,
        // so the stack stays in Splash until the scheduler is explicitly advanced.
        val stdDispatchers = TestAppDispatchers(scheduler = testScheduler, useStandard = true)
        val root = buildRoot(stdDispatchers)
        assertIs<RootConfig.Splash>(root.stack.value.active.configuration)
    }

    @Test
    fun session_expired_event_transitions_stack_to_Auth() = runTest {
        val root = buildRoot()

        // Emit session-expired; the root component subscribes in init{}
        sessionEvents.emitExpired()
        testScheduler.advanceUntilIdle()

        assertIs<RootConfig.Auth>(root.stack.value.active.configuration)
    }

    // ── Session-expiry reason routing (design-gap-closure) ──────────────────────
    //
    // A real 401/token-expiry (EXPIRED) must surface "tu sesión expiró" at Login;
    // an explicit user-initiated logout must stay silent. DefaultRootComponent
    // distinguishes the two via PendingSessionMessage, consumed by DefaultLoginComponent.

    @Test
    fun session_expired_with_EXPIRED_reason_sets_pending_session_message() = runTest {
        PendingSessionMessage.expired = false
        val root = buildRoot()

        sessionEvents.emitExpired(SessionExpiryReason.EXPIRED)
        testScheduler.advanceUntilIdle()

        assertIs<RootConfig.Auth>(root.stack.value.active.configuration)
        assertTrue(PendingSessionMessage.expired)
    }

    @Test
    fun session_expired_with_USER_INITIATED_reason_does_not_set_pending_session_message() = runTest {
        PendingSessionMessage.expired = false
        val root = buildRoot()

        sessionEvents.emitExpired(SessionExpiryReason.USER_INITIATED)
        testScheduler.advanceUntilIdle()

        assertIs<RootConfig.Auth>(root.stack.value.active.configuration)
        assertFalse(PendingSessionMessage.expired)
    }
}

// --- Minimal stubs implementing only the required interface methods ---

private class StubLoginComponent : LoginComponent {
    override val state: Value<LoginState> = MutableValue(LoginState())
    override fun onEmailChange(email: String) {}
    override fun onPasswordChange(password: String) {}
    override fun onSubmit() {}
    override fun onErrorDismissed() {}
    override fun onForgotPassword() {}
    override fun onRegister() {}
    override fun onResendActivation() {}
}

private class StubRegisterPatientComponent : RegisterPatientComponent {
    override val state: Value<RegisterPatientState> = MutableValue(RegisterPatientState())
    override fun onNameChanged(name: String) {}
    override fun onLastNameChanged(lastName: String) {}
    override fun onEmailChanged(email: String) {}
    override fun onPhoneChanged(phone: String) {}
    override fun onPasswordChanged(password: String) {}
    override fun onConfirmPasswordChanged(confirmPassword: String) {}
    override fun onSubmit() {}
    override fun onBack() {}
}

private class StubRegisterDoctorComponent : RegisterDoctorComponent {
    override val state: Value<RegisterDoctorState> = MutableValue(RegisterDoctorState())
    override fun onFirstNameChanged(value: String) {}
    override fun onLastNameChanged(value: String) {}
    override fun onEmailChanged(email: String) {}
    override fun onPhoneChanged(value: String) {}
    override fun onLicenseNumberChanged(value: String) {}
    override fun onToggleSpecialty(specialtyId: String) {}
    override fun onPrimarySpecialtySelected(specialtyId: String) {}
    override fun onConsultationPriceChanged(value: String) {}
    override fun onAppointmentModeChanged(mode: String) {}
    override fun onAppointmentDurationChanged(value: String) {}
    override fun onOffersHomeVisitToggled(value: Boolean) {}
    override fun onDocumentUploaded(url: String) {}
    override fun onDocumentRemoved(url: String) {}
    override fun onDocumentFilePicked(file: com.inclinic.app.core.platform.PickedFile) {}
    override fun onScheduleAdded(schedule: com.inclinic.app.features.auth.infrastructure.remote.dto.FreelanceScheduleDto) {}
    override fun onScheduleRemoved(index: Int) {}
    override fun onNextStep() {}
    override fun onBack() {}
    override fun onSubmit() {}
}

private class StubAccountCreatedComponent : AccountCreatedComponent {
    override val state: Value<AccountCreatedState> = MutableValue(AccountCreatedState())
    override val email: String = "stub@test.com"
    override fun onGoToLogin() {}
    override fun onResend() {}
}

private class StubActivateComponent : ActivateComponent {
    override val state: Value<ActivateState> = MutableValue(ActivateState())
    override val email: String = "stub@test.com"
    override fun onCodeChanged(code: String) {}
    override fun onSubmit() {}
    override fun onResend() {}
    override fun onBack() {}
}

private class StubForgotPasswordComponent : ForgotPasswordComponent {
    override val state: Value<ForgotPasswordState> = MutableValue(ForgotPasswordState())
    override fun onEmailChanged(email: String) {}
    override fun onSubmit() {}
    override fun onBack() {}
}

private class StubResetPasswordComponent : ResetPasswordComponent {
    override val token: String = "stub-token"
    override val state: Value<ResetPasswordState> = MutableValue(ResetPasswordState())
    override fun onPasswordChanged(password: String) {}
    override fun onConfirmPasswordChanged(confirmPassword: String) {}
    override fun onSubmit() {}
    override fun onBack() {}
}

private class StubTwoFactorVerifyComponent : TwoFactorVerifyComponent {
    override val state: Value<TwoFactorVerifyState> = MutableValue(TwoFactorVerifyState())
    override fun onCodeChange(code: String) {}
    override fun onVerify() {}
    override fun onErrorDismissed() {}
    override fun onBack() {}
}
