@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.core.navigation

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.core.events.SessionEvents
import com.inclinic.app.core.model.OnboardingStatus
import com.inclinic.app.features.auth.application.GetStoredTokensUseCase
import com.inclinic.app.features.auth.core.model.AuthUser
import com.inclinic.app.features.auth.core.model.UserRole
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
import com.inclinic.app.features.doctor.onboarding.application.GetOnboardingStatusUseCase
import com.inclinic.app.features.doctor.onboarding.fakes.FakeDoctorOnboardingRepository
import com.inclinic.app.features.doctor.onboarding.presentation.component.CorregirSolicitudComponent
import com.inclinic.app.features.doctor.onboarding.presentation.component.CorregirSolicitudState
import com.inclinic.app.features.doctor.onboarding.presentation.component.DoctorOnboardingComponent
import com.inclinic.app.features.doctor.onboarding.presentation.component.EnviadoComponent
import com.inclinic.app.features.doctor.onboarding.presentation.component.EnviadoState
import com.inclinic.app.features.doctor.presentation.component.DoctorFlowComponent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertIs

/**
 * Tests that [DefaultRootComponent] routes a doctor user to the correct root child
 * based on the [OnboardingStatus] returned by [GetOnboardingStatusUseCase].
 *
 * The test drives the routing via [AuthFlowComponent.Output.AuthenticatedAsDoctor].
 */
class DefaultRootComponentDoctorRoutingTest {

    private val lifecycle = LifecycleRegistry()
    private val fakeTokenStorage = FakeTokenStorage()
    private val dispatchers = TestAppDispatchers()
    private val sessionEvents = SessionEvents()
    private val fakeOnboardingRepo = FakeDoctorOnboardingRepository()

    private val getStoredTokens = GetStoredTokensUseCase(fakeTokenStorage, dispatchers)
    private val getOnboardingStatus = GetOnboardingStatusUseCase(fakeOnboardingRepo, dispatchers)

    private fun buildRoot(
        authUser: AuthUser = AuthUser(id = "doc-1", email = "doc@test.com", firstName = "Test", lastName = "Doctor", role = UserRole.DOCTOR, doctorId = "doc-1"),
        onboardingStatus: OnboardingStatus,
    ): DefaultRootComponent {
        fakeOnboardingRepo.statusResult = Result.success(onboardingStatus)

        lifecycle.resume()
        val ctx = DefaultComponentContext(lifecycle)
        return DefaultRootComponent(
            componentContext = ctx,
            dispatchers = dispatchers,
            sessionEvents = sessionEvents,
            getStoredTokens = getStoredTokens,
            tokenStorage = fakeTokenStorage,
            loginComponentFactory = { _, _, _, _, _ -> StubLoginComponent() },
            twoFactorVerifyComponentFactory = { _, _, _, _ -> StubTwoFactorVerifyComponent() },
            registerPatientComponentFactory = { _, _ -> StubRegisterPatientComponent() },
            registerDoctorComponentFactory = { _, _ -> StubRegisterDoctorComponent() },
            activateComponentFactory = { _, _, _ -> StubActivateComponent() },
            accountCreatedComponentFactory = { _, _, _ -> StubAccountCreatedComponent() },
            forgotPasswordComponentFactory = { _, _ -> StubForgotPasswordComponent() },
            resetPasswordComponentFactory = { _, _, _ -> StubResetPasswordComponent() },
            patientFlowComponentFactory = null,
            doctorFlowComponentFactory = { ctx2, _ -> StubDoctorFlowComponent() },
            doctorOnboardingComponentFactory = { ctx2, _, out -> StubDoctorOnboardingComponent(out) },
            getOnboardingStatusUseCase = getOnboardingStatus,
            doctorCorregirComponentFactory = { ctx2, out -> StubCorregirSolicitudComponent(out) },
        )
    }

    // Helper: trigger doctor auth in the Auth flow child by calling replaceAll(Doctor) directly.
    // Since we can't easily navigate through the full auth flow in unit test, we use the
    // AuthFlowComponent output lambda that the root wires internally. Instead we trigger
    // routing by calling the factory's internal auth output callback path.
    // The simplest approach: just call routeDoctorByOnboardingStatus via the splash output.
    // We simulate splash completing by providing a splashFactory that immediately emits
    // NavigateToDoctor; instead we create root and manually trigger the internal route.

    // Alternative: test from the compositeChild directly by asserting state post-construct.
    // But the root starts at Splash. Instead we skip splash and test via a companion route.

    // The cleanest test: use the Auth flow login output to drive routing.
    // We'll test the routing logic independently using the auth flow's onOutput callback.

    @Test
    fun doctor_with_NONE_status_routes_to_DoctorOnboarding() = runTest {
        val root = buildRoot(onboardingStatus = OnboardingStatus.NONE)
        // Simulate auth success for a doctor
        simulateDoctorAuth(root)
        testScheduler.advanceUntilIdle()

        assertIs<RootComponent.Child.DoctorOnboarding>(root.stack.value.active.instance)
    }

    @Test
    fun doctor_with_PENDING_status_routes_to_DoctorEnviado() = runTest {
        val root = buildRoot(onboardingStatus = OnboardingStatus.PENDING)
        simulateDoctorAuth(root)
        testScheduler.advanceUntilIdle()

        assertIs<RootComponent.Child.DoctorEnviado>(root.stack.value.active.instance)
    }

    @Test
    fun doctor_with_REJECTED_status_routes_to_DoctorCorregir() = runTest {
        val root = buildRoot(onboardingStatus = OnboardingStatus.REJECTED)
        simulateDoctorAuth(root)
        testScheduler.advanceUntilIdle()

        assertIs<RootComponent.Child.DoctorCorregir>(root.stack.value.active.instance)
    }

    @Test
    fun doctor_with_APPROVED_status_routes_to_Doctor_flow() = runTest {
        val root = buildRoot(onboardingStatus = OnboardingStatus.APPROVED)
        simulateDoctorAuth(root)
        testScheduler.advanceUntilIdle()

        assertIs<RootComponent.Child.Doctor>(root.stack.value.active.instance)
    }

    /**
     * Trigger doctor routing by extracting the auth flow's output lambda and calling it.
     * We navigate root to Auth first, then the Auth flow child signals AuthenticatedAsDoctor.
     */
    private fun simulateDoctorAuth(root: DefaultRootComponent) {
        // The root starts at Splash. Navigate to Auth by emitting session-expired (simplest path).
        // Then get the auth child and invoke its output callback via the StubAuthFlow.
        // Actually — we've registered a stub login factory. The auth flow's output is internal.
        // Simplest approach: use the navigation stack via reflection is fragile.
        // Instead, expose a test-only method via a companion in DefaultRootComponent.
        // Since we can't modify DefaultRootComponent just for tests, use the SplashComponent.Output.

        // Best approach: SplashComponent output handler immediately calls routeDoctorByOnboardingStatus.
        // We can trigger it by replacing the navigation stack to Auth and then triggering auth success.

        // Direct test: the root navigates to auth on session expired → get auth child → trigger login.
        // The auth child is a DefaultAuthFlowComponent which has an onOutput lambda.
        // Since we supply stub factories for login, the only way to trigger auth output is via
        // the DefaultAuthFlowComponent's internal navigation — too complex for unit test.

        // SIMPLEST: Use DefaultRootComponent's internal navigation directly.
        // We add a test-only @VisibleForTesting method... or we test via existing public API.

        // Resolution: use navigateToAuth → replaceAll is internal. Instead, add a test-only
        // function to DefaultRootComponent via a companion internal API.
        // For this PR we expose internal routing by calling the root's navigation directly
        // through the existing public `handleDeepLink` → session event path, then
        // override the auth flow output.

        // Final decision: the simplest TDD approach is to test the routing result from a
        // factory-provided AuthFlowComponent that calls the output immediately on construction.
        // We do this by making the auth flow factory trigger auth output synchronously.

        // The root creates AuthFlowComponent via createAuthFlow which calls DefaultAuthFlowComponent
        // with `loginComponentFactory`. The DefaultAuthFlowComponent only calls onOutput when
        // the login component triggers success — we can't do that from stub factories alone.

        // WORKAROUND: We navigate to Auth, then get the active AuthFlowComponent stub,
        // and since we can't call its output directly, we use sessionEvents to go to Auth
        // and then bypass the normal flow. The test for doctor routing needs to be done
        // at the routing logic level, not the component level.

        // For now: just replaceAll by exposing via sessionEvents + then manually trigger.
        // We'll use the internal fact that sessionEvents fires replaceAll(Auth) and then
        // from the Auth child we call the output. Since Auth child is DefaultAuthFlowComponent
        // with our stub login factory, we cannot easily trigger its output from tests.

        // PRACTICAL FIX: Wrap routing in a TestableDefaultRootComponent or use
        // the SplashComponent output. Since splash factory is DefaultSplashComponent which
        // reads token storage, and the token storage is our FakeTokenStorage (no token),
        // the splash will immediately emit NavigateToAuth. After that we're in Auth state.
        // Then we need to trigger the doctor auth output.

        // For this test, we DIRECTLY test the onboarding routing by providing a custom
        // auth component factory that immediately fires the AuthenticatedAsDoctor output.
        // The way to do this is to note that buildRoot uses a stub login factory, but
        // DefaultAuthFlowComponent calls onOutput only when its internal stack triggers it.

        // The proper solution for this level of isolation: expose an internal test method.
        // We do this by adding `internal fun triggerDoctorAuth()` to DefaultRootComponent.
        // We will add this method now.
        root.triggerDoctorAuth("doc-1")
    }
}

// ── Stubs ──────────────────────────────────────────────────────────────────────

private class StubLoginComponent : LoginComponent {
    override val state: Value<LoginState> = MutableValue(LoginState())
    override fun onEmailChange(email: String) {}
    override fun onPasswordChange(password: String) {}
    override fun onSubmit() {}
    override fun onErrorDismissed() {}
    override fun onForgotPassword() {}
    override fun onRegister() {}
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

private class StubDoctorFlowComponent : DoctorFlowComponent {
    override val currentTab = MutableValue(com.inclinic.app.features.doctor.presentation.component.DoctorTab.Inicio)
    override val iniciStack = MutableValue(com.arkivanov.decompose.router.stack.ChildStack<com.inclinic.app.core.navigation.DoctorConfig, DoctorFlowComponent.Child>(
        active = com.arkivanov.decompose.Child.Created(
            configuration = com.inclinic.app.core.navigation.DoctorConfig.Dashboard,
            instance = DoctorFlowComponent.Child.Dashboard(
                object : com.inclinic.app.features.doctor.presentation.component.DoctorDashboardComponent {
                    override val state = MutableValue(com.inclinic.app.features.doctor.presentation.component.DoctorDashboardState())
                    override fun onRefresh() {}
                    override fun onNavigateToSchedule() {}
                    override fun onNavigateToPendingAppointments() {}
                    override fun onNavigateToNotifications() {}
                    override fun onAppointmentTap(appointmentId: String) {}
                    override fun onCreateMedicalRecord() {}
                    override fun onNavigateToPackages() {}
                    override fun onNavigateToPatients() {}
                    override fun onNavigateToIncome() {}
                }
            ),
        ),
    ))
    override val agendaStack = iniciStack
    override val pacientesStack = iniciStack
    override val mensajesStack = iniciStack
    override val perfilStack = iniciStack
    override fun onTabSelected(tab: com.inclinic.app.features.doctor.presentation.component.DoctorTab) {}
    override fun navigateTo(config: com.inclinic.app.core.navigation.DoctorConfig) {}
}

private class StubDoctorOnboardingComponent(
    private val onOutput: (DoctorOnboardingComponent.Output) -> Unit,
) : DoctorOnboardingComponent {
    override val stack = MutableValue(
        com.arkivanov.decompose.router.stack.ChildStack<com.inclinic.app.features.doctor.onboarding.presentation.component.OnboardingNavConfig, DoctorOnboardingComponent.Child>(
            active = com.arkivanov.decompose.Child.Created(
                configuration = com.inclinic.app.features.doctor.onboarding.presentation.component.OnboardingNavConfig.StepDatos,
                instance = DoctorOnboardingComponent.Child.StepDatos(
                    object : com.inclinic.app.features.doctor.onboarding.presentation.component.StepDatosComponent {
                        override val state = MutableValue(com.inclinic.app.features.doctor.onboarding.presentation.component.StepDatosState())
                        override fun onFirstNameChanged(value: String) {}
                        override fun onLastNameChanged(value: String) {}
                        override fun onCmpLicenseChanged(value: String) {}
                        override fun onPhoneChanged(value: String) {}
                        override fun onContinueClicked() {}
                        override fun onErrorDismissed() {}
                    }
                ),
            ),
        )
    )
}

private class StubEnviadoComponent : EnviadoComponent {
    override val state: Value<EnviadoState> = MutableValue(EnviadoState())
    override fun onLogOutClicked() {}
    override fun onErrorDismissed() {}
}

private class StubCorregirSolicitudComponent(
    private val onOutput: (CorregirSolicitudComponent.Output) -> Unit,
) : CorregirSolicitudComponent {
    override val state: Value<CorregirSolicitudState> = MutableValue(CorregirSolicitudState())
    override fun onFieldChanged(field: String, value: String) {}
    override fun onSubmitClicked() {}
    override fun onErrorDismissed() {}
}

private class StubTwoFactorVerifyComponent : TwoFactorVerifyComponent {
    override val state: Value<TwoFactorVerifyState> = MutableValue(TwoFactorVerifyState())
    override fun onCodeChange(code: String) {}
    override fun onVerify() {}
    override fun onErrorDismissed() {}
    override fun onBack() {}
}
