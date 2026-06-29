package com.inclinic.app.features.doctor.reschedule.fakes

import com.inclinic.app.features.doctor.reschedule.core.model.RescheduleRequest
import com.inclinic.app.features.doctor.reschedule.core.model.RescheduleRequestStatus
import com.inclinic.app.features.doctor.reschedule.core.port.RescheduleQueueRepository

class FakeRescheduleQueueRepository : RescheduleQueueRepository {

    var listResult: Result<List<RescheduleRequest>> = Result.success(emptyList())
    var respondResult: Result<RescheduleRequest> = Result.success(stubRequest("req-1", RescheduleRequestStatus.APPROVED))

    var listCallCount = 0
    var respondCallCount = 0
    var lastRespondId: String? = null
    var lastRespondDecision: RescheduleRequestStatus? = null

    override suspend fun listRequests(): Result<List<RescheduleRequest>> {
        listCallCount++
        return listResult
    }

    override suspend fun respond(id: String, decision: RescheduleRequestStatus): Result<RescheduleRequest> {
        respondCallCount++
        lastRespondId = id
        lastRespondDecision = decision
        return respondResult
    }
}

fun stubRequest(
    id: String,
    status: RescheduleRequestStatus = RescheduleRequestStatus.PENDING,
) = RescheduleRequest(
    id = id,
    patientName = "María Quispe",
    currentSlot = "Mar 15 · 10:00",
    requestedSlot = "Mar 17 · 14:00",
    reason = "Tengo viaje de trabajo imprevisto.",
    status = status,
)
