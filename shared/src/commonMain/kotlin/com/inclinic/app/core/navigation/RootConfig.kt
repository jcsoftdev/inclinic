package com.inclinic.app.core.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface RootConfig {
    @Serializable data object Splash : RootConfig
    @Serializable data object Auth : RootConfig

    /** Patient flow. [patientId] is serialized with the stack so it survives process death. */
    @Serializable data class Patient(val patientId: String) : RootConfig

    /** Approved-doctor flow. [doctorId] is serialized with the stack so it survives process death. */
    @Serializable data class Doctor(val doctorId: String) : RootConfig

    @Serializable data object Admin : RootConfig

    /** Doctor has not yet started onboarding. [doctorId] persisted with the stack. */
    @Serializable data class DoctorOnboarding(val doctorId: String) : RootConfig

    /** Doctor submitted onboarding, awaiting admin review. */
    @Serializable data object DoctorEnviado : RootConfig

    /** Admin rejected onboarding, doctor must correct and resubmit. */
    @Serializable data object DoctorCorregir : RootConfig
}
