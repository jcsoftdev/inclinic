package com.inclinic.app.features.patient.appointments.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.patient.infrastructure.remote.AppointmentDataSource
import kotlinx.coroutines.withContext

class ConfirmRatingUseCase(
    private val dataSource: AppointmentDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(
        appointmentId: String,
        punctuality: Int,
        professionalism: Int,
        empathy: Int,
        comment: String?,
    ): Result<Unit> = withContext(dispatchers.io) {
        dataSource.confirmRating(appointmentId, punctuality, professionalism, empathy, comment)
    }
}
