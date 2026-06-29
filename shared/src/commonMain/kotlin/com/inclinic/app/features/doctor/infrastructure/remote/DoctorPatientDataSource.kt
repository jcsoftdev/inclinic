package com.inclinic.app.features.doctor.infrastructure.remote

import com.inclinic.app.core.model.Appointment
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class PatientDetail(
    val id: String,
    val fullName: String,
    val email: String,
    val phone: String?,
    val dateOfBirth: String?,
    val gender: String?,
    val bloodType: String?,
    val allergies: String?,
    val chronicConditions: String?,
    val currentMedications: String?,
)

interface DoctorPatientDataSource {
    suspend fun getPatientDetail(patientId: String): Result<PatientDetail>
    suspend fun getPatientAppointments(patientId: String, limit: Int): Result<List<Appointment>>
}
