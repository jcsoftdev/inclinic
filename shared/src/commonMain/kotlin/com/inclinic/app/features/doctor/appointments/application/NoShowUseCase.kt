package com.inclinic.app.features.doctor.appointments.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.Appointment
import com.inclinic.app.core.model.AppointmentStatus
import com.inclinic.app.features.doctor.infrastructure.remote.DoctorAppointmentDataSource
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

class NoShowUseCase(
    private val dataSource: DoctorAppointmentDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(appointment: Appointment): Result<Appointment> =
        withContext(dispatchers.io) {
            val validStatuses = setOf(AppointmentStatus.CONFIRMED, AppointmentStatus.SCHEDULED)
            if (appointment.status !in validStatuses) {
                return@withContext Result.failure(
                    IllegalStateException("No-show only available for CONFIRMED or SCHEDULED appointments")
                )
            }
            val now = Clock.System.now()
            val windowStart = appointment.startsAt - 30.minutes
            val windowEnd = appointment.startsAt + 30.minutes
            if (now !in windowStart..windowEnd) {
                return@withContext Result.failure(
                    IllegalStateException("No-show only available within ±30 minutes of the appointment start time")
                )
            }
            dataSource.markNoShow(appointment.id)
        }
}
