package com.inclinic.app.core.navigation

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.inclinic.app.features.admin.presentation.component.AdminFlowComponent
import com.inclinic.app.features.auth.presentation.component.AuthFlowComponent
import com.inclinic.app.features.doctor.onboarding.presentation.component.CorregirSolicitudComponent
import com.inclinic.app.features.doctor.onboarding.presentation.component.DoctorOnboardingComponent
import com.inclinic.app.features.doctor.onboarding.presentation.component.EnviadoComponent
import com.inclinic.app.features.doctor.presentation.component.DoctorFlowComponent
import com.inclinic.app.features.patient.presentation.component.PatientFlowComponent
import com.inclinic.app.features.splash.presentation.component.SplashComponent

interface RootComponent {
    val stack: Value<ChildStack<*, Child>>

    /**
     * Handle an incoming deep link. May be called from the OS entry point (Activity/AppDelegate)
     * at any time — before or after the initial navigation has settled.
     */
    fun handleDeepLink(link: DeepLink)

    sealed interface Child {
        class Splash(val component: SplashComponent) : Child
        class Auth(val component: AuthFlowComponent) : Child
        class Patient(val component: PatientFlowComponent) : Child
        class Doctor(val component: DoctorFlowComponent) : Child
        class Admin(val component: AdminFlowComponent) : Child
        class DoctorOnboarding(val component: DoctorOnboardingComponent) : Child
        class DoctorEnviado(val component: EnviadoComponent) : Child
        class DoctorCorregir(val component: CorregirSolicitudComponent) : Child
    }
}

/**
 * Represents all deep-link destinations supported by the app.
 *
 * Android URL: inclinic://reset-password?token=X
 * Android URL: inclinic://appointments/{id}
 */
sealed interface DeepLink {
    /** clinicai://reset-password?token=X */
    data class ResetPassword(val token: String) : DeepLink

    /** inclinic://appointments/{id} */
    data class AppointmentDetail(val appointmentId: String) : DeepLink
}
