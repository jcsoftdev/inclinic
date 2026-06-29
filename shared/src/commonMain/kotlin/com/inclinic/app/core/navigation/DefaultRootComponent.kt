package com.inclinic.app.core.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.events.SessionEvents
import com.inclinic.app.core.model.OnboardingStatus
import com.inclinic.app.features.admin.presentation.component.AdminFlowComponent
import com.inclinic.app.features.auth.application.GetStoredTokensUseCase
import com.inclinic.app.features.auth.core.port.TokenStorage
import com.inclinic.app.features.auth.presentation.component.AccountCreatedComponent
import com.inclinic.app.features.auth.presentation.component.ActivateComponent
import com.inclinic.app.features.auth.presentation.component.AuthFlowComponent
import com.inclinic.app.features.auth.presentation.component.DefaultAuthFlowComponent
import com.inclinic.app.features.auth.presentation.component.ForgotPasswordComponent
import com.inclinic.app.features.auth.core.model.AuthUser
import com.inclinic.app.features.auth.presentation.component.LoginComponent
import com.inclinic.app.features.auth.presentation.component.RegisterDoctorComponent
import com.inclinic.app.features.auth.presentation.component.RegisterPatientComponent
import com.inclinic.app.features.auth.presentation.component.ResetPasswordComponent
import com.inclinic.app.features.auth.presentation.component.TwoFactorVerifyComponent
import com.inclinic.app.features.doctor.onboarding.application.GetOnboardingStatusUseCase
import com.inclinic.app.features.doctor.onboarding.presentation.component.CorregirSolicitudComponent
import com.inclinic.app.features.doctor.onboarding.presentation.component.DefaultCorregirSolicitudComponent
import com.inclinic.app.features.doctor.onboarding.presentation.component.DefaultDoctorOnboardingComponent
import com.inclinic.app.features.doctor.onboarding.presentation.component.DefaultEnviadoComponent
import com.inclinic.app.features.doctor.onboarding.presentation.component.DoctorOnboardingComponent
import com.inclinic.app.features.doctor.onboarding.presentation.component.EnviadoComponent
import com.inclinic.app.features.doctor.presentation.component.DoctorFlowComponent
import com.inclinic.app.features.patient.presentation.component.PatientFlowComponent
import com.inclinic.app.features.splash.presentation.component.DefaultSplashComponent
import com.inclinic.app.features.splash.presentation.component.SplashComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultRootComponent(
    componentContext: ComponentContext,
    private val dispatchers: AppDispatchers,
    private val sessionEvents: SessionEvents,
    private val getStoredTokens: GetStoredTokensUseCase,
    private val tokenStorage: TokenStorage,
    private val loginComponentFactory: (ComponentContext, (AuthUser) -> Unit, (String) -> Unit, () -> Unit, () -> Unit) -> LoginComponent,
    private val twoFactorVerifyComponentFactory: (ComponentContext, String, (AuthUser) -> Unit, () -> Unit) -> TwoFactorVerifyComponent,
    private val registerPatientComponentFactory: (ComponentContext, (RegisterPatientComponent.Output) -> Unit) -> RegisterPatientComponent,
    private val registerDoctorComponentFactory: (ComponentContext, (RegisterDoctorComponent.Output) -> Unit) -> RegisterDoctorComponent,
    private val activateComponentFactory: (ComponentContext, String, (ActivateComponent.Output) -> Unit) -> ActivateComponent,
    private val accountCreatedComponentFactory: (ComponentContext, String, (AccountCreatedComponent.Output) -> Unit) -> AccountCreatedComponent,
    private val forgotPasswordComponentFactory: (ComponentContext, (ForgotPasswordComponent.Output) -> Unit) -> ForgotPasswordComponent,
    private val resetPasswordComponentFactory: (ComponentContext, String, (ResetPasswordComponent.Output) -> Unit) -> ResetPasswordComponent,
    private val patientFlowComponentFactory: ((ComponentContext, String) -> PatientFlowComponent)? = null,
    private val doctorFlowComponentFactory: ((ComponentContext, String) -> DoctorFlowComponent)? = null,
    private val adminFlowComponentFactory: ((ComponentContext) -> AdminFlowComponent)? = null,
    private val doctorOnboardingComponentFactory: ((ComponentContext, String, (DoctorOnboardingComponent.Output) -> Unit) -> DoctorOnboardingComponent)? = null,
    private val getOnboardingStatusUseCase: GetOnboardingStatusUseCase? = null,
    private val doctorCorregirComponentFactory: ((ComponentContext, (CorregirSolicitudComponent.Output) -> Unit) -> CorregirSolicitudComponent)? = null,
) : RootComponent, ComponentContext by componentContext {

    private val navigation = StackNavigation<RootConfig>()
    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())

    init {
        lifecycle.doOnDestroy { scope.cancel() }
        scope.launch {
            sessionEvents.expired.collect {
                navigation.replaceAll(RootConfig.Auth)
            }
        }
    }

    private val _stack = childStack(
        source = navigation,
        serializer = RootConfig.serializer(),
        initialConfiguration = RootConfig.Splash,
        handleBackButton = false,
        childFactory = ::createChild,
    )
    override val stack: Value<ChildStack<*, RootComponent.Child>> = _stack

    private fun createChild(config: RootConfig, ctx: ComponentContext): RootComponent.Child =
        when (config) {
            is RootConfig.Splash -> RootComponent.Child.Splash(createSplash(ctx))
            is RootConfig.Auth -> RootComponent.Child.Auth(createAuthFlow(ctx))
            is RootConfig.Patient -> RootComponent.Child.Patient(createPatientFlow(ctx, config.patientId))
            is RootConfig.Doctor -> RootComponent.Child.Doctor(createDoctorFlow(ctx, config.doctorId))
            is RootConfig.Admin -> RootComponent.Child.Admin(createAdminFlow(ctx))
            is RootConfig.DoctorOnboarding -> RootComponent.Child.DoctorOnboarding(createDoctorOnboarding(ctx, config.doctorId))
            is RootConfig.DoctorEnviado -> RootComponent.Child.DoctorEnviado(createDoctorEnviado(ctx))
            is RootConfig.DoctorCorregir -> RootComponent.Child.DoctorCorregir(createDoctorCorregir(ctx))
        }

    private fun createSplash(ctx: ComponentContext): SplashComponent =
        DefaultSplashComponent(ctx, getStoredTokens, tokenStorage, dispatchers) { output ->
            when (output) {
                SplashComponent.Output.NavigateToAuth -> navigation.replaceAll(RootConfig.Auth)
                is SplashComponent.Output.NavigateToPatient ->
                    navigation.replaceAll(RootConfig.Patient(output.patientId))
                is SplashComponent.Output.NavigateToDoctor ->
                    routeDoctorByOnboardingStatus(output.doctorId)
                is SplashComponent.Output.NavigateToAdmin ->
                    navigation.replaceAll(RootConfig.Admin)
            }
        }

    private fun createAuthFlow(ctx: ComponentContext): AuthFlowComponent {
        val flow = DefaultAuthFlowComponent(
            componentContext = ctx,
            dispatchers = dispatchers,
            loginComponentFactory = loginComponentFactory,
            twoFactorVerifyComponentFactory = twoFactorVerifyComponentFactory,
            registerPatientComponentFactory = registerPatientComponentFactory,
            registerDoctorComponentFactory = registerDoctorComponentFactory,
            activateComponentFactory = activateComponentFactory,
            accountCreatedComponentFactory = accountCreatedComponentFactory,
            forgotPasswordComponentFactory = forgotPasswordComponentFactory,
            resetPasswordComponentFactory = resetPasswordComponentFactory,
        ) { output ->
            when (output) {
                is AuthFlowComponent.Output.AuthenticatedAsPatient ->
                    navigation.replaceAll(RootConfig.Patient(output.patientId))
                is AuthFlowComponent.Output.AuthenticatedAsDoctor ->
                    routeDoctorByOnboardingStatus(output.doctorId)
                is AuthFlowComponent.Output.AuthenticatedAsAdmin ->
                    navigation.replaceAll(RootConfig.Admin)
            }
        }
        // Drain any pending deep link that arrived before the Auth flow was active.
        val pending = PendingDeepLink.link
        if (pending is DeepLink.ResetPassword) {
            PendingDeepLink.link = null
            flow.navigateTo(AuthNavigationConfig.ResetPassword(pending.token))
        }
        return flow
    }

    private fun routeDoctorByOnboardingStatus(doctorId: String) {
        val useCase = getOnboardingStatusUseCase
        if (useCase == null) {
            // No use case provided (e.g., in tests or legacy wiring) — go straight to flow.
            navigation.replaceAll(RootConfig.Doctor(doctorId))
            return
        }
        scope.launch {
            useCase().fold(
                onSuccess = { status ->
                    val target = when (status) {
                        OnboardingStatus.NONE -> RootConfig.DoctorOnboarding(doctorId)
                        OnboardingStatus.PENDING -> RootConfig.DoctorEnviado
                        OnboardingStatus.REJECTED -> RootConfig.DoctorCorregir
                        OnboardingStatus.APPROVED -> RootConfig.Doctor(doctorId)
                    }
                    navigation.replaceAll(target)
                },
                onFailure = {
                    // On error default to the main flow; the flow itself can handle stale state.
                    navigation.replaceAll(RootConfig.Doctor(doctorId))
                }
            )
        }
    }

    private fun createPatientFlow(ctx: ComponentContext, patientId: String): PatientFlowComponent {
        val flow = patientFlowComponentFactory?.invoke(ctx, patientId)
            ?: error("PatientFlowComponent factory not provided")
        // Drain pending appointment deep link.
        val pending = PendingDeepLink.link
        if (pending is DeepLink.AppointmentDetail) {
            PendingDeepLink.link = null
            flow.navigateTo(PatientConfig.AppointmentDetail(pending.appointmentId))
        }
        return flow
    }

    private fun createDoctorFlow(ctx: ComponentContext, doctorId: String): DoctorFlowComponent {
        val flow = doctorFlowComponentFactory?.invoke(ctx, doctorId)
            ?: error("DoctorFlowComponent factory not provided")
        // Drain pending appointment deep link.
        val pending = PendingDeepLink.link
        if (pending is DeepLink.AppointmentDetail) {
            PendingDeepLink.link = null
            flow.navigateTo(DoctorConfig.AppointmentDetail(pending.appointmentId))
        }
        return flow
    }

    private fun createAdminFlow(ctx: ComponentContext): AdminFlowComponent =
        adminFlowComponentFactory?.invoke(ctx)
            ?: error("AdminFlowComponent factory not provided")

    private fun createDoctorOnboarding(ctx: ComponentContext, doctorId: String): DoctorOnboardingComponent {
        val factory = doctorOnboardingComponentFactory
            ?: error("DoctorOnboardingComponent factory not provided")
        return factory(ctx, doctorId) { output ->
            when (output) {
                DoctorOnboardingComponent.Output.NavigateOutToLogin ->
                    navigation.replaceAll(RootConfig.Auth)
            }
        }
    }

    private fun createDoctorEnviado(ctx: ComponentContext): EnviadoComponent {
        val useCase = getOnboardingStatusUseCase
            ?: error("GetOnboardingStatusUseCase not provided")
        return DefaultEnviadoComponent(
            componentContext = ctx,
            dispatchers = dispatchers,
            getOnboardingStatusUseCase = useCase,
        ) { output ->
            when (output) {
                EnviadoComponent.Output.LogOut -> navigation.replaceAll(RootConfig.Auth)
            }
        }
    }

    private fun createDoctorCorregir(ctx: ComponentContext): CorregirSolicitudComponent {
        val factory = doctorCorregirComponentFactory
            ?: error("CorregirSolicitudComponent factory not provided")
        return factory(ctx) { output ->
            when (output) {
                CorregirSolicitudComponent.Output.LogOut -> navigation.replaceAll(RootConfig.Auth)
            }
        }
    }

    // ── Test helpers ──────────────────────────────────────────────────────────

    /**
     * Simulates a doctor authentication event so that unit tests can exercise
     * [routeDoctorByOnboardingStatus] without driving the full Auth flow.
     * Only visible within the module (internal) — not part of [RootComponent].
     */
    internal fun triggerDoctorAuth(doctorId: String) {
        routeDoctorByOnboardingStatus(doctorId)
    }

    // ── Deep link handling ────────────────────────────────────────────────────

    /**
     * Route an incoming deep link to the correct screen.
     *
     * Strategy:
     * - Inspect the current top-of-stack to know which flow is active.
     * - If the target flow matches, delegate navigation into that flow.
     * - If the target flow is not yet active (e.g., app just launched or user is
     *   unauthenticated), store the link in [PendingDeepLink]; it will be consumed
     *   once the flow becomes active (see [createPatientFlow] / [createDoctorFlow]
     *   overrides below).
     *
     * Must be called on the main thread.
     */
    override fun handleDeepLink(link: DeepLink) {
        val activeChild = _stack.value.active.instance
        when (link) {
            is DeepLink.ResetPassword -> {
                when (activeChild) {
                    is RootComponent.Child.Auth -> {
                        // Auth flow is active — push reset-password into it.
                        activeChild.component.navigateTo(AuthNavigationConfig.ResetPassword(link.token))
                    }
                    else -> {
                        // Not on Auth yet — navigate to Auth then store so it can be consumed.
                        PendingDeepLink.link = link
                        navigation.replaceAll(RootConfig.Auth)
                    }
                }
            }
            is DeepLink.AppointmentDetail -> {
                when (activeChild) {
                    is RootComponent.Child.Patient -> {
                        activeChild.component.navigateTo(
                            PatientConfig.AppointmentDetail(link.appointmentId)
                        )
                    }
                    is RootComponent.Child.Doctor -> {
                        activeChild.component.navigateTo(
                            DoctorConfig.AppointmentDetail(link.appointmentId)
                        )
                    }
                    else -> {
                        // Unauthenticated — store and process after login.
                        PendingDeepLink.link = link
                    }
                }
            }
        }
    }
}
