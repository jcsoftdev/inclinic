package com.inclinic.app.features.patient.appointments.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.patient.infrastructure.remote.AppointmentDataSource
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlin.time.Instant

class CancelAppointmentUseCase(
    private val dataSource: AppointmentDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(
        appointmentId: String,
        appointmentStartsAt: Instant,
        reason: String,
    ): Result<Unit> = withContext(dispatchers.io) {
        val now = Clock.System.now()
        val daysUntil = (appointmentStartsAt - now).inWholeDays
        if (daysUntil < 3) {
            return@withContext Result.failure(
                IllegalStateException("No se puede cancelar con menos de 3 días de anticipación")
            )
        }
        dataSource.cancelAppointment(appointmentId, reason)
    }
}
