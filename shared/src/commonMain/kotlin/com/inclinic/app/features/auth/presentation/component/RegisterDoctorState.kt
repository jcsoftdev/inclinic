package com.inclinic.app.features.auth.presentation.component

import com.inclinic.app.core.model.Specialty
import com.inclinic.app.features.auth.core.error.AuthError
import com.inclinic.app.features.auth.infrastructure.remote.dto.FreelanceScheduleDto

data class RegisterDoctorState(
    val step: Step = Step.PersonalData,

    // ── Step 1 — Datos personales ─────────────────────────────────────────────
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phone: String = "",
    val licenseNumber: String = "",

    val firstNameError: String? = null,
    val lastNameError: String? = null,
    val emailError: String? = null,
    val phoneError: String? = null,

    // ── Step 2 — Especialidad + precio ────────────────────────────────────────
    val specialties: List<Specialty> = emptyList(),
    val specialtiesLoading: Boolean = false,
    val selectedSpecialtyIds: Set<String> = emptySet(),
    val primarySpecialtyId: String? = null,
    val consultationPriceText: String = "",
    val appointmentMode: String = "BY_SCHEDULE",
    val appointmentDurationText: String = "30",
    val offersHomeVisit: Boolean = false,

    val specialtyError: String? = null,
    val priceError: String? = null,

    // ── Step 3 — Documentos ───────────────────────────────────────────────────
    /** Uploaded document URLs (returned by the upload endpoint). */
    val documentUrls: List<String> = emptyList(),
    val documentError: String? = null,

    // ── Step 4 — Horarios ─────────────────────────────────────────────────────
    val schedules: List<FreelanceScheduleDto> = emptyList(),
    val scheduleError: String? = null,

    // ── Global ────────────────────────────────────────────────────────────────
    val isLoading: Boolean = false,
    val serverError: AuthError? = null,
) {
    enum class Step {
        PersonalData,
        SpecialtyAndPrice,
        Documents,
        Schedules,
        Review,
    }

    val canSubmit: Boolean
        get() = firstName.isNotBlank() &&
                lastName.isNotBlank() &&
                email.isNotBlank() &&
                phone.length >= 6 &&
                selectedSpecialtyIds.isNotEmpty() &&
                primarySpecialtyId != null &&
                (consultationPriceText.toDoubleOrNull() ?: 0.0) >= 50.0 &&
                documentUrls.isNotEmpty() &&
                schedules.isNotEmpty()
}
