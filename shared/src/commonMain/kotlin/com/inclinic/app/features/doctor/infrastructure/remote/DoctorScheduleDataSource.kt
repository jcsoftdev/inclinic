package com.inclinic.app.features.doctor.infrastructure.remote

import kotlinx.serialization.Serializable

/**
 * One configured availability block for a single weekday, mirroring the backend
 * `DoctorSchedule` Prisma model (range-based, not an hour grid).
 *
 * Maps 1:1 to the design "Editar Horarios" day card:
 * Inicio/Fin, Max pacientes, Duración slot, Precio, and the
 * "Permitir cambio domicilio/consultorio" toggle.
 */
@Serializable
data class DaySchedule(
    val dayOfWeek: String,          // MONDAY..SUNDAY
    val startTime: String,          // HH:MM
    val endTime: String,            // HH:MM
    val maxPatients: Int = 8,
    val slotDuration: Int? = null,  // minutes
    val price: Double? = null,
    val breakStart: String? = null, // HH:MM
    val breakEnd: String? = null,   // HH:MM
    val specialtyId: String? = null,
    val isHomeVisit: Boolean? = null,
    val allowVisitTypeNegotiation: Boolean = false,
    val isActive: Boolean = true,
)

/**
 * The doctor's full weekly availability — the collection of [DaySchedule] blocks.
 */
@Serializable
data class WeeklySchedule(
    val days: List<DaySchedule> = emptyList(),
)

interface DoctorScheduleDataSource {
    /** Reads the weekly schedule from the doctor profile (`GET /api/doctors/{id}`). */
    suspend fun getWeeklySchedule(doctorId: String): Result<WeeklySchedule>

    /** Persists the weekly schedule (`PUT /api/doctors/{id}/schedules`). */
    suspend fun saveWeeklySchedule(doctorId: String, schedule: WeeklySchedule): Result<WeeklySchedule>
}
