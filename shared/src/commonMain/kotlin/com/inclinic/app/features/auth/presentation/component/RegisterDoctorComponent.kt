package com.inclinic.app.features.auth.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.core.platform.PickedFile
import com.inclinic.app.features.auth.infrastructure.remote.dto.FreelanceScheduleDto

/**
 * Presentation contract for the doctor freelance registration flow.
 *
 * Five-step flow matching POST /api/doctors/freelance:
 *   Step 1 — Datos personales (firstName, lastName, email, phone, licenseNumber)
 *   Step 2 — Especialidad + precio (specialtyIds, primarySpecialtyId, consultationPrice,
 *             appointmentMode, appointmentDuration, offersHomeVisit)
 *   Step 3 — Documentos (document URLs via upload, at least one required)
 *   Step 4 — Horarios (at least one schedule slot)
 *   Step 5 — Revisar / Enviar
 *
 * No password field — the backend sends an activation email after submission.
 */
interface RegisterDoctorComponent {
    val state: Value<RegisterDoctorState>

    // ── Step 1 — Datos personales ─────────────────────────────────────────────
    fun onFirstNameChanged(value: String)
    fun onLastNameChanged(value: String)
    fun onEmailChanged(email: String)
    fun onPhoneChanged(value: String)
    fun onLicenseNumberChanged(value: String)

    // ── Step 2 — Especialidad + precio ────────────────────────────────────────
    fun onToggleSpecialty(specialtyId: String)
    fun onPrimarySpecialtySelected(specialtyId: String)
    fun onConsultationPriceChanged(value: String)
    fun onAppointmentModeChanged(mode: String)
    fun onAppointmentDurationChanged(value: String)
    fun onOffersHomeVisitToggled(value: Boolean)

    // ── Step 3 — Documentos ───────────────────────────────────────────────────
    fun onDocumentUploaded(url: String)
    fun onDocumentRemoved(url: String)

    /**
     * User picked a file from the native picker in the Documents step.
     * Stores [file.fileName] in [documentUrls] as a local placeholder until actual upload.
     */
    fun onDocumentFilePicked(file: PickedFile)

    // ── Step 4 — Horarios ─────────────────────────────────────────────────────
    fun onScheduleAdded(schedule: FreelanceScheduleDto)
    fun onScheduleRemoved(index: Int)

    // ── Navigation ────────────────────────────────────────────────────────────
    fun onNextStep()
    fun onBack()
    fun onSubmit()

    sealed interface Output {
        /** Registration submitted successfully — navigate to confirmation screen. */
        data class Success(val email: String) : Output
        data object Back : Output
    }
}
