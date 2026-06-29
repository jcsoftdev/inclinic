package com.inclinic.app.features.doctor.reschedule_request.infrastructure.remote

import com.inclinic.app.core.model.Appointment
import com.inclinic.app.features.doctor.reschedule_request.infrastructure.remote.dto.CreateRescheduleRequestDto

interface RescheduleRequestDataSource {
    suspend fun requestReschedule(appointmentId: String, body: CreateRescheduleRequestDto): Result<Appointment>
}
