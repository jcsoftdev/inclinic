package com.inclinic.app.core.model

import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class MedicalRecord(
    val id: String,
    val appointmentId: String,
    val patientId: String,
    val doctorId: String,
    /**
     * Nombre del médico en formato "Dr. Nombre Apellido". Resuelto por el backend
     * en la respuesta de lista (`getPatientMedicalHistory` incluye `doctor.user`).
     * Null cuando el backend no incluyó la relación (backward-compat con lado doctor).
     */
    val doctorName: String? = null,
    /** Nombre de la especialidad, incluido por el backend en la respuesta de lista. */
    val specialtyName: String? = null,
    val diagnosis: String,
    val symptoms: String,
    val treatment: String,
    val prescription: String?,
    val notes: String?,
    val createdAt: Instant,
    /**
     * true cuando el backend stripeó el registro por plan FREE
     * (ver medical.service.ts `stripForFreePatient`). Default false para no
     * romper a los constructores existentes (lado doctor).
     */
    val isLocked: Boolean = false,
)

/**
 * @Serializable so Decompose [StateKeeper] can persist draft content across
 * Android configuration changes (orientation, etc.).
 *
 * REQ-4-009
 */
@Serializable
data class MedicalRecordDraft(
    val appointmentId: String? = null,
    val diagnosis: String = "",
    val symptoms: String = "",
    val treatment: String = "",
    val prescription: String = "",
    val notes: String = "",
)
