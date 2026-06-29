package com.inclinic.app.features.doctor.onboarding.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.inclinic.app.features.doctor.onboarding.presentation.component.DoctorOnboardingComponent

/**
 * Renders the active child of [DoctorOnboardingComponent]'s ChildStack.
 *
 * Each screen is stateless — it receives the typed component and renders its
 * subscribeAsState() snapshot. Back navigation is handled by the ChildStack's
 * handleBackButton = true; the [onBack] lambda provides the platform hook.
 */
@Composable
fun OnboardingFlowContent(
    component: DoctorOnboardingComponent,
    modifier: Modifier = Modifier,
) {
    Children(stack = component.stack, modifier = modifier) { child ->
        when (val c = child.instance) {
            is DoctorOnboardingComponent.Child.StepDatos ->
                OnboardingDatosScreen(component = c.component)

            is DoctorOnboardingComponent.Child.StepDocumentos ->
                OnboardingDocumentosScreen(component = c.component)

            is DoctorOnboardingComponent.Child.StepEspecialidades ->
                OnboardingEspecialidadesScreen(component = c.component)

            is DoctorOnboardingComponent.Child.StepHorarios ->
                OnboardingHorariosScreen(component = c.component)

            is DoctorOnboardingComponent.Child.StepPrecios ->
                OnboardingPreciosScreen(component = c.component)

            is DoctorOnboardingComponent.Child.Enviado ->
                EnviadoScreen(component = c.component)

            is DoctorOnboardingComponent.Child.Corregir ->
                CorregirSolicitudScreen(component = c.component)
        }
    }
}
