package com.inclinic.app.core.model

import kotlinx.serialization.Serializable

/**
 * Perfil clínico permanente del paciente (alergias, condiciones crónicas, contacto de emergencia).
 *
 * Refleja el modelo `MedicalProfile` del backend (Prisma). El endpoint
 * `GET /patients/:id/medical-profile` puede devolver `null` cuando el paciente aún
 * no tiene un perfil creado; en ese caso se representa con [empty].
 *
 * Campos adicionales alineados con `updateMedicalProfileSchema` (medical.ts):
 * bloodType, heightCm, weightKg, currentMedications, pastSurgeries, familyHistory.
 */
@Serializable
data class MedicalProfile(
    val bloodType: String? = null,
    val allergies: List<String> = emptyList(),
    val chronicConditions: List<String> = emptyList(),
    val currentMedications: List<String> = emptyList(),
    val pastSurgeries: List<String> = emptyList(),
    val familyHistory: String? = null,
    val heightCm: Float? = null,
    val weightKg: Float? = null,
    val emergencyContact: EmergencyContact = EmergencyContact(),
    val insuranceProvider: String? = null,
    val insuranceNumber: String? = null,
) {
    companion object {
        fun empty(): MedicalProfile = MedicalProfile()
    }
}

@Serializable
data class EmergencyContact(
    val name: String? = null,
    val phone: String? = null,
    val relation: String? = null,
) {
    val isEmpty: Boolean
        get() = name.isNullOrBlank() && phone.isNullOrBlank() && relation.isNullOrBlank()
}
