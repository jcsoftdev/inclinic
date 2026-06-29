package com.inclinic.app.features.doctor.reschedule_request.core.port

import com.inclinic.app.core.model.Appointment

interface RescheduleRequestRepository {
    suspend fun requestReschedule(appointmentId: String, proposedSlot: String, message: String?): Result<Appointment>
}
