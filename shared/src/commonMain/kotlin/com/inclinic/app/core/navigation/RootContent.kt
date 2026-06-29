package com.inclinic.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.inclinic.app.features.admin.presentation.ui.AdminFlowContent
import com.inclinic.app.features.auth.presentation.ui.AuthFlowContent
import com.inclinic.app.features.doctor.onboarding.presentation.ui.CorregirSolicitudScreen
import com.inclinic.app.features.doctor.onboarding.presentation.ui.EnviadoScreen
import com.inclinic.app.features.doctor.onboarding.presentation.ui.OnboardingFlowContent
import com.inclinic.app.features.doctor.presentation.ui.DoctorFlowContent
import com.inclinic.app.features.patient.presentation.ui.PatientFlowContent
import com.inclinic.app.features.splash.presentation.ui.SplashScreen
import com.inclinic.app.ui.theme.AppTheme

@Composable
fun RootContent(component: RootComponent, modifier: Modifier = Modifier) {
    AppTheme {
        Children(stack = component.stack, modifier = modifier) { child ->
            when (val c = child.instance) {
                is RootComponent.Child.Splash -> SplashScreen()
                is RootComponent.Child.Auth -> AuthFlowContent(c.component)
                is RootComponent.Child.Patient -> PatientFlowContent(c.component)
                is RootComponent.Child.Doctor -> DoctorFlowContent(c.component)
                is RootComponent.Child.Admin -> AdminFlowContent(c.component)
                is RootComponent.Child.DoctorOnboarding -> OnboardingFlowContent(c.component)
                is RootComponent.Child.DoctorEnviado -> EnviadoScreen(c.component)
                is RootComponent.Child.DoctorCorregir -> CorregirSolicitudScreen(c.component)
            }
        }
    }
}
