package com.inclinic.app.features.patient.infrastructure.remote

import com.inclinic.app.core.model.Appointment
import com.inclinic.app.core.model.Doctor
import com.inclinic.app.core.model.MedicalProfile
import com.inclinic.app.core.model.PatientProfile

interface PatientDataSource {
    suspend fun getPatientProfile(patientId: String): Result<PatientProfile>
    suspend fun updatePatientProfile(
        patientId: String,
        name: String,
        phone: String?,
        dateOfBirth: String?,
    ): Result<PatientProfile>
    suspend fun getMedicalProfile(patientId: String): Result<MedicalProfile>

    /**
     * PUT /patients/:id/medical-profile — crea o actualiza el perfil clínico.
     * Solo se envían los campos no nulos; el backend hace upsert.
     */
    suspend fun updateMedicalProfile(patientId: String, profile: MedicalProfile): Result<MedicalProfile>

    suspend fun getDashboard(patientId: String): Result<PatientDashboard>

    /**
     * Eliminación de cuenta (derecho al olvido — Ley 29733). Confirmada con la
     * contraseña actual. El backend anonimiza y suspende la cuenta (soft-delete);
     * no es un borrado físico por obligación legal médica.
     */
    suspend fun deleteAccount(password: String, reason: String? = null): Result<Unit>
}

data class PatientDashboard(
    val upcomingCount: Int,
    val recentDoctors: List<Doctor>,
    val nextAppointment: Appointment? = null,
)
