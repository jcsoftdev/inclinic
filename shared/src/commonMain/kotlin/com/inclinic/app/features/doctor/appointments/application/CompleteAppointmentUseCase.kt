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
     * Completes an appointment with the URLs of evidence photos already uploaded
     * to the `visit-proofs` bucket (the component uploads them via UploadFileUseCase
     * and passes the resulting URLs here). Home visits require at least one photo.
     */
    suspend operator fun invoke(
        appointment: Appointment,
        photoUrls: List<String>,
    ): Result<Appointment> = withContext(dispatchers.io) {
        if (appointment.status != AppointmentStatus.CONFIRMED &&
            appointment.status != AppointmentStatus.IN_PROGRESS
        ) {
            return@withContext Result.failure(
                IllegalStateException("Appointment must be CONFIRMED or IN_PROGRESS to complete")
            )
        }
        if (photoUrls.isEmpty()) {
            return@withContext Result.failure(
                IllegalStateException("At least one evidence photo is required")
            )
        }
        dataSource.completeAppointment(appointment.id, photoUrls)
    }
}
