package com.inclinic.app.features.doctor.onboarding.presentation.component

import kotlinx.serialization.Serializable

@Serializable
sealed interface OnboardingNavConfig {
    @Serializable data object StepDatos : OnboardingNavConfig
    @Serializable data object StepDocumentos : OnboardingNavConfig
    @Serializable data object StepEspecialidades : OnboardingNavConfig
    @Serializable data object StepHorarios : OnboardingNavConfig
    @Serializable data object StepPrecios : OnboardingNavConfig
    @Serializable data object Enviado : OnboardingNavConfig
    @Serializable data object Corregir : OnboardingNavConfig
}
