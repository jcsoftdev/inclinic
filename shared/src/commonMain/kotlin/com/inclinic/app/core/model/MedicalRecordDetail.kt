package com.inclinic.app.core.model

import kotlin.time.Instant
import kotlinx.serialization.Serializable

/**
 * Detalle completo de un registro médico tal como lo expone el backend
 * (`GET /api/medical-records/:id`).
 *
 * Para pacientes en plan FREE el backend devuelve [isLocked] = true y NULLea los
 * campos sensibles (chiefComplaint, symptoms, vitalSigns, treatmentPlan,
 * prescriptions, notes), vacía studiesOrdered/attachments y recorta el
 * diagnóstico a una vista previa. Todos los campos son nullable para soportar
 * ese estado bloqueado de forma segura.
 */
@Serializable
data class MedicalRecordDetail(
    val id: String,
    val appointmentId: String? = null,
    val doctorId: String,
    val doctorName: String? = null,
    val specialtyName: String? = null,
    val recordDate: Instant,
    val chiefComplaint: String? = null,
    val symptoms: String? = null,
    val vitalSigns: VitalSigns? = null,
    val diagnosis: String? = null,
    val treatmentPlan: String? = null,
    val prescriptions: List<RecordPrescription> = emptyList(),
    val studiesOrdered: List<String> = emptyList(),
    val attachments: List<String> = emptyList(),
    val followUpDate: Instant? = null,
    val notes: String? = null,
    val isLocked: Boolean = false,
)

/**
 * Signos vitales. El backend los almacena como JSON libre
 * (`{ bloodPressure, heartRate, temperature, weight, height, ... }`),
 * por eso modelamos las claves más comunes como campos opcionales.
 */
@Serializable
data class VitalSigns(
    val bloodPressureSystolic: Int? = null,
    val bloodPressureDiastolic: Int? = null,
    val heartRate: Int? = null,
    val temperature: Double? = null,
    val oxygenSaturation: Int? = null,
    val weight: Double? = null,
    val height: Double? = null,
)

/**
 * Ítem de receta. El backend guarda `prescriptions` como JSON array
 * (`[{ medication, dosage, frequency, duration, instructions }]`).
 */
@Serializable
data class RecordPrescription(
    val medication: String? = null,
    val dosage: String? = null,
    val frequency: String? = null,
    val duration: String? = null,
    val instructions: String? = null,
)
