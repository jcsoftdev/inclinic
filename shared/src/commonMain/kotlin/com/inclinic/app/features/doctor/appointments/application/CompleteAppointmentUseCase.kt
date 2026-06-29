package com.inclinic.app.features.doctor.appointments.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.Appointment
import com.inclinic.app.core.model.AppointmentStatus
import com.inclinic.app.features.doctor.infrastructure.remote.DoctorAppointmentDataSource
import kotlinx.coroutines.withContext

class CompleteAppointmentUseCase(
    private val dataSource: DoctorAppointmentDataSource,
    private val dispatchers: AppDispatchers,
) {
    /**
     * Phase 3 stub: photoBytes are not uploaded to Supabase yet — placeholder URLs are used.
     * Real Supabase upload is Phase 5.
     */
    suspend operator fun invoke(
        appointment: Appointment,
        selectedPhotos: List<ByteArray>,
    ): Result<Appointment> = withContext(dispatchers.io) {
        if (appointment.status != AppointmentStatus.CONFIRMED &&
            appointment.status != AppointmentStatus.IN_PROGRESS
        ) {
            return@withContext Result.failure(
                IllegalStateException("Appointment must be CONFIRMED or IN_PROGRESS to complete")
            )
        }
        if (selectedPhotos.isEmpty()) {
            return@withContext Result.failure(
                IllegalStateException("At least one evidence photo is required")
            )
        }
        val stubUrls = selectedPhotos.mapIndexed { i, _ -> "stub-evidence-$i" }
        dataSource.completeAppointment(appointment.id, stubUrls)
    }
}
