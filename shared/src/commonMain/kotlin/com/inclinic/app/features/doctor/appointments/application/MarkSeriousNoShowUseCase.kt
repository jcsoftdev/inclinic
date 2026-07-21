package com.inclinic.app.features.doctor.appointments.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.Appointment
import com.inclinic.app.core.model.AppointmentStatus
import com.inclinic.app.core.model.VisitType
import com.inclinic.app.core.platform.GpsFix
import com.inclinic.app.features.doctor.infrastructure.remote.DoctorAppointmentDataSource
import kotlinx.coroutines.withContext

/**
 * Marca una falta grave del paciente: el médico fue a la visita a domicilio y el paciente
 * no avisó ni estaba. Exige evidencia (al menos una foto + check-in GPS), igual que el
 * cierre de una visita a domicilio. Solo aplica a citas a domicilio.
 */
class MarkSeriousNoShowUseCase(
    private val dataSource: DoctorAppointmentDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(
        appointment: Appointment,
        photoUrls: List<String>,
        checkIn: GpsFix,
        note: String? = null,
    ): Result<Appointment> = withContext(dispatchers.io) {
        if (appointment.status != AppointmentStatus.CONFIRMED &&
            appointment.status != AppointmentStatus.IN_PROGRESS
        ) {
            return@withContext Result.failure(
                IllegalStateException("La cita debe estar confirmada o en progreso")
            )
        }
        if (appointment.visitType != VisitType.HOME) {
            return@withContext Result.failure(
                IllegalStateException("La falta grave aplica solo a visitas a domicilio")
            )
        }
        if (photoUrls.isEmpty()) {
            return@withContext Result.failure(
                IllegalStateException("Se requiere al menos una foto de evidencia")
            )
        }
        dataSource.markSeriousNoShow(
            appointmentId = appointment.id,
            photoUrls = photoUrls,
            checkInLat = checkIn.lat,
            checkInLng = checkIn.lng,
            checkInAccuracyM = checkIn.accuracyMeters,
            note = note,
        )
    }
}
