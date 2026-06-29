package com.inclinic.app.features.doctor.infrastructure.remote

import com.inclinic.app.core.model.Appointment
import com.inclinic.app.features.doctor.no_shows.core.model.NoShowItem

interface DoctorAppointmentDataSource {
    suspend fun getDashboard(doctorId: String): Result<DoctorDashboard>
    suspend fun getDailySchedule(doctorId: String, date: String): Result<List<Appointment>>
    suspend fun getWeeklySchedule(doctorId: String, weekStart: String): Result<List<DaySummary>>
    suspend fun getAppointmentById(appointmentId: String): Result<Appointment>
    suspend fun confirmAppointment(appointmentId: String): Result<Appointment>
    suspend fun completeAppointment(appointmentId: String, photoUrls: List<String>): Result<Appointment>
    suspend fun markNoShow(appointmentId: String): Result<Appointment>

    /**
     * Fetch all no-show appointments for the authenticated doctor.
     *
     * Backend: GET /api/appointments?status=NO_SHOW[&from=…&to=…]
     * The doctor JWT auto-injects doctorId scoping server-side.
     */
    suspend fun getNoShowAppointments(
        from: String? = null,
        to: String? = null,
    ): Result<List<NoShowItem>>
}

data class DoctorDashboard(
    val todayCount: Int,
    val pendingCount: Int,
    val monthlyEarnings: Double,
    val ratingAverage: Double,
    val ratingCount: Int = 0,
    val completedCount: Int = 0,
    val completedThisMonth: Int = 0,
    val patientsCount: Int = 0,
    val recurringPatientsCount: Int = 0,
    val completedTodayPct: Int = 0,
)

data class DaySummary(
    val date: String,
    val appointmentCount: Int,
)
