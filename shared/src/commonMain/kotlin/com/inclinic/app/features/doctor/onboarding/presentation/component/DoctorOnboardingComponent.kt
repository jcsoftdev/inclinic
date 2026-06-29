package com.inclinic.app.features.doctor.onboarding.presentation.component

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value

/**
 * Parent component for the doctor onboarding flow.
 *
 * Holds a [ChildStack] covering 5 form steps + Enviado + Corregir.
 * Navigation between steps is driven by [Child] events; the final
 * output signals the host that the flow is complete.
 */
interface DoctorOnboardingComponent {
    val stack: Value<ChildStack<*, Child>>

    sealed interface Output {
        /** After submit success the host routes to Enviado (or back to Login). */
        data object NavigateOutToLogin : Output
    }

    sealed interface Child {
        class StepDatos(val component: StepDatosComponent) : Child
        class StepDocumentos(val component: StepDocumentosComponent) : Child
        class StepEspecialidades(val component: StepEspecialidadesComponent) : Child
        class StepHorarios(val component: StepHorariosComponent) : Child
        class StepPrecios(val component: StepPreciosComponent) : Child
        class Enviado(val component: EnviadoComponent) : Child
        class Corregir(val component: CorregirSolicitudComponent) : Child
    }
}
