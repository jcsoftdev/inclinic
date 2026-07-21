package com.inclinic.app.features.patient.booking.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.Appointment
import com.inclinic.app.features.patient.infrastructure.remote.AppointmentDataSource
import kotlinx.coroutines.withContext

class CreateAppointmentUseCase(
    private val dataSource: AppointmentDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(
        doctorId: String,
        date: String,
        slotId: String,
        visitType: String,
        notes: String?,
        homeVisitAddress: String? = null,
        homeVisitLat: Double? = null,
        homeVisitLng: Double? = null,
    ): Result<Appointment> = withContext(dispatchers.io) {
        if (visitType.isBlank()) return@withContext Result.failure(IllegalArgumentException("Visit type is required"))
        // Una visita a domicilio exige coordenadas geolocalizadas de la dirección.
        if (visitType == "HOME" && (homeVisitLat == null || homeVisitLng == null)) {
            return@withContext Result.failure(
                IllegalArgumentException("Una visita a domicilio requiere una dirección con ubicación en el mapa")
            )
        }
        dataSource.createAppointment(doctorId, date, slotId, visitType, notes, homeVisitAddress, homeVisitLat, homeVisitLng)
    }
}
