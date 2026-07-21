package com.inclinic.app.features.doctor.appointments.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.Appointment
import com.inclinic.app.core.model.AppointmentStatus
import com.inclinic.app.core.model.VisitType
import com.inclinic.app.core.platform.GpsFix
import com.inclinic.app.features.doctor.infrastructure.remote.DoctorAppointmentDataSource
import kotlinx.coroutines.withContext

class CompleteAppointmentUseCase(
    private val dataSource: DoctorAppointmentDataSource,
    private val dispatchers: AppDispatchers,
) {
    /**
     * Completes an appointment with the URLs of evidence photos already uploaded
     * to the `visit-proofs` bucket (the component uploads them via UploadFileUseCase
     * and passes the resulting URLs here). A home visit requires at least one photo
     * AND a GPS check-in ([checkIn]) as evidence that the doctor was on site.
     */
    suspend operator fun invoke(
        appointment: Appointment,
        photoUrls: List<String>,
        checkIn: GpsFix? = null,
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
        if (appointment.visitType == VisitType.HOME && checkIn == null) {
            return@withContext Result.failure(
                IllegalStateException("Una visita a domicilio requiere registrar tu ubicación (check-in GPS)")
            )
        }
        dataSource.completeAppointment(
            appointmentId = appointment.id,
            photoUrls = photoUrls,
            checkInLat = checkIn?.lat,
            checkInLng = checkIn?.lng,
            checkInAccuracyM = checkIn?.accuracyMeters,
        )
    }
}
