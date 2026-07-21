@file:OptIn(DelicateDecomposeApi::class)

package com.inclinic.app.features.auth.presentation.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DelicateDecomposeApi
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.navigation.AuthNavigationConfig
import com.inclinic.app.features.auth.core.model.AuthUser
import com.inclinic.app.features.auth.core.model.UserRole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

class DefaultAuthFlowComponent(
    componentContext: ComponentContext,
    private val dispatchers: AppDispatchers,
    private val loginComponentFactory: (ComponentContext, (AuthUser) -> Unit, (String) -> Unit, () -> Unit, () -> Unit, () -> Unit) -> LoginComponent,
    private val twoFactorVerifyComponentFactory: (ComponentContext, String, (AuthUser) -> Unit, () -> Unit) -> TwoFactorVerifyComponent,
    private val registerPatientComponentFactory: (ComponentContext, (RegisterPatientComponent.Output) -> Unit) -> RegisterPatientComponent,
    private val registerDoctorComponentFactory: (ComponentContext, (RegisterDoctorComponent.Output) -> Unit) -> RegisterDoctorComponent,
    private val activateComponentFactory: (ComponentContext, String, (ActivateComponent.Output) -> Unit) -> ActivateComponent,
    private val accountCreatedComponentFactory: (ComponentContext, String, (AccountCreatedComponent.Output) -> Unit) -> AccountCreatedComponent,
    private val forgotPasswordComponentFactory: (ComponentContext, (ForgotPasswordComponent.Output) -> Unit) -> ForgotPasswordComponent,
    private val resetPasswordComponentFactory: (ComponentContext, String, (ResetPasswordComponent.Output) -> Unit) -> ResetPasswordComponent,
    private val onOutput: (AuthFlowComponent.Output) -> Unit,
) : AuthFlowComponent, ComponentContext by componentContext {

    private val navigation = StackNavigation<AuthNavigationConfig>()
    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())

    init {
        lifecycle.doOnDestroy { scope.cancel() }
    }

    private val _stack = childStack(
        source = navigation,
        serializer = AuthNavigationConfig.serializer(),
        initialConfiguration = AuthNavigationConfig.Login,
        handleBackButton = true,
        childFactory = ::createChild,
    )
    override val stack: Value<ChildStack<*, AuthFlowComponent.Child>> = _stack

    override fun navigateTo(config: AuthNavigationConfig) {
        navigation.push(config)
    }

    /** Emits the role-specific authenticated output — shared by direct login and 2FA verify paths. */
    private fun routeAuthenticated(user: AuthUser) {
        when (user.role) {
            UserRole.DOCTOR -> onOutput(AuthFlowComponent.Output.AuthenticatedAsDoctor(user.doctorId ?: user.id))
            UserRole.SUPER_ADMIN -> onOutput(AuthFlowComponent.Output.AuthenticatedAsAdmin(user.id))
            else -> onOutput(AuthFlowComponent.Output.AuthenticatedAsPatient(user.patientId ?: user.id))
        }
    }

    private fun createChild(
        config: AuthNavigationConfig,
        ctx: ComponentContext,
    ): AuthFlowComponent.Child = when (config) {
        is AuthNavigationConfig.Login -> AuthFlowComponent.Child.Login(
            loginComponentFactory(
                ctx,
                { user -> routeAuthenticated(user) },
                { partialToken -> navigation.push(AuthNavigationConfig.TwoFactorVerify(partialToken)) },
                { navigation.push(AuthNavigationConfig.ForgotPassword) },
                { navigation.push(AuthNavigationConfig.RegisterChooser) },
                { navigation.push(AuthNavigationConfig.RateLimit) },
            )
        )
        is AuthNavigationConfig.TwoFactorVerify -> AuthFlowComponent.Child.TwoFactorVerify(
            twoFactorVerifyComponentFactory(
                ctx,
                config.partialToken,
                { user -> routeAuthenticated(user) },
                { navigation.pop() },
            )
        )
        is AuthNavigationConfig.RateLimit -> AuthFlowComponent.Child.RateLimit(
            object : PatientRateLimitComponent {
                override fun onBackToLogin() = navigation.replaceAll(AuthNavigationConfig.Login)
            }
        )
        is AuthNavigationConfig.RegisterChooser -> AuthFlowComponent.Child.RegisterChooser(
            object : RegisterChooserComponent {
                override fun onPatientSelected() = navigation.push(AuthNavigationConfig.RegisterPatient)
                override fun onDoctorSelected() = navigation.push(AuthNavigationConfig.RegisterDoctor)
                override fun onLogin() = navigation.pop()
            }
        )
        is AuthNavigationConfig.RegisterPatient -> AuthFlowComponent.Child.RegisterPatient(
            registerPatientComponentFactory(ctx) { output ->
                when (output) {
                    // Patient registration → email-confirmation screen (not the doctor Activate flow).
                    is RegisterPatientComponent.Output.Success ->
                        navigation.push(AuthNavigationConfig.AccountCreated(email = output.email))
                    is RegisterPatientComponent.Output.Back -> navigation.pop()
                    is RegisterPatientComponent.Output.RateLimited -> navigation.push(AuthNavigationConfig.RateLimit)
                }
            }
        )
        is AuthNavigationConfig.RegisterDoctor -> AuthFlowComponent.Child.RegisterDoctor(
            registerDoctorComponentFactory(ctx) { output ->
                when (output) {
                    // Freelance doctor registration → confirmation screen.
                    // Backend emails the activation link; no in-app Activate step needed.
                    is RegisterDoctorComponent.Output.Success ->
                        navigation.push(AuthNavigationConfig.AccountCreated(email = output.email))
                    is RegisterDoctorComponent.Output.Back -> navigation.pop()
                }
            }
        )
        is AuthNavigationConfig.ForgotPassword -> AuthFlowComponent.Child.ForgotPassword(
            forgotPasswordComponentFactory(ctx) { output ->
                when (output) {
                    is ForgotPasswordComponent.Output.Back -> navigation.pop()
                    is ForgotPasswordComponent.Output.RateLimited -> navigation.push(AuthNavigationConfig.RateLimit)
                }
            }
        )
        is AuthNavigationConfig.ResetPassword -> AuthFlowComponent.Child.ResetPassword(
            resetPasswordComponentFactory(ctx, config.token) { output ->
                when (output) {
                    is ResetPasswordComponent.Output.Success -> navigation.replaceAll(AuthNavigationConfig.Login)
                    is ResetPasswordComponent.Output.Back -> navigation.pop()
                }
            }
        )
        is AuthNavigationConfig.Activate -> AuthFlowComponent.Child.Activate(
            activateComponentFactory(ctx, config.email) { output ->
                when (output) {
                    is ActivateComponent.Output.Success -> navigation.replaceAll(AuthNavigationConfig.Login)
                    is ActivateComponent.Output.Back -> navigation.pop()
                }
            }
        )
        is AuthNavigationConfig.AccountCreated -> AuthFlowComponent.Child.AccountCreated(
            accountCreatedComponentFactory(ctx, config.email) { output ->
                when (output) {
                    is AccountCreatedComponent.Output.GoToLogin ->
                        navigation.replaceAll(AuthNavigationConfig.Login)
                    is AccountCreatedComponent.Output.ResendEmail ->
                        // Resend runs inside the component (calls ResendActivationUseCase).
                        // Emitted here only for observability; no navigation change.
                        Unit
                }
            }
        )
    }
}
