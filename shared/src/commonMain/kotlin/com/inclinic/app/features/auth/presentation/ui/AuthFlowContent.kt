package com.inclinic.app.features.auth.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.inclinic.app.features.auth.presentation.component.AuthFlowComponent

@Composable
fun AuthFlowContent(component: AuthFlowComponent, modifier: Modifier = Modifier) {
    Children(stack = component.stack, modifier = modifier) { child ->
        when (val c = child.instance) {
            is AuthFlowComponent.Child.Login -> LoginScreen(c.component)
            is AuthFlowComponent.Child.RegisterChooser -> RegisterChooserScreen(c.component)
            is AuthFlowComponent.Child.RegisterPatient -> RegisterPatientScreen(c.component)
            is AuthFlowComponent.Child.RegisterDoctor -> RegisterDoctorScreen(c.component)
            is AuthFlowComponent.Child.ForgotPassword -> ForgotPasswordScreen(c.component)
            is AuthFlowComponent.Child.ResetPassword -> ResetPasswordScreen(c.component)
            is AuthFlowComponent.Child.Activate -> ActivateScreen(c.component)
            is AuthFlowComponent.Child.AccountCreated -> AccountCreatedScreen(c.component)
            is AuthFlowComponent.Child.TwoFactorVerify -> TwoFactorVerifyScreen(c.component)
            is AuthFlowComponent.Child.RateLimit -> PatientRateLimitScreen(c.component)
        }
    }
}
