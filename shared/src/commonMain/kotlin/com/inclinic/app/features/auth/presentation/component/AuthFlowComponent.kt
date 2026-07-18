package com.inclinic.app.features.auth.presentation.component

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.inclinic.app.core.navigation.AuthNavigationConfig

interface AuthFlowComponent {
    val stack: Value<ChildStack<*, Child>>

    sealed interface Output {
        data class AuthenticatedAsPatient(val patientId: String) : Output
        data class AuthenticatedAsDoctor(val doctorId: String) : Output
        data class AuthenticatedAsAdmin(val adminId: String) : Output
    }

    sealed interface Child {
        class Login(val component: LoginComponent) : Child
        class RegisterChooser(val component: RegisterChooserComponent) : Child
        class RegisterPatient(val component: RegisterPatientComponent) : Child
        class RegisterDoctor(val component: RegisterDoctorComponent) : Child
        class ForgotPassword(val component: ForgotPasswordComponent) : Child
        class ResetPassword(val component: ResetPasswordComponent) : Child
        class Activate(val component: ActivateComponent) : Child
        class AccountCreated(val component: AccountCreatedComponent) : Child
        /** Step 2 of login — TOTP code verification. */
        class TwoFactorVerify(val component: TwoFactorVerifyComponent) : Child
        /** Standalone HTTP 429 "too many attempts" screen. */
        class RateLimit(val component: PatientRateLimitComponent) : Child
    }

    fun navigateTo(config: AuthNavigationConfig)
}
