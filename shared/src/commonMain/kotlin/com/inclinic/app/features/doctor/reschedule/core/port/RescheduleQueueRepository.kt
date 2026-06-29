package com.inclinic.app.features.doctor.reschedule.core.port

import com.inclinic.app.features.doctor.reschedule.core.model.RescheduleRequest
import com.inclinic.app.features.doctor.reschedule.core.model.RescheduleRequestStatus

interface RescheduleQueueRepository {
    suspend fun listRequests(): Result<List<RescheduleRequest>>
    suspend fun respond(id: String, decision: RescheduleRequestStatus): Result<RescheduleRequest>
}
