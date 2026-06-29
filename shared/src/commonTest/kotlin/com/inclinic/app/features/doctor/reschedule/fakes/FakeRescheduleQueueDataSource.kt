package com.inclinic.app.features.doctor.reschedule.fakes

import com.inclinic.app.features.doctor.reschedule.infrastructure.remote.RescheduleQueueDataSource
import com.inclinic.app.features.doctor.reschedule.infrastructure.remote.dto.RescheduleRequestDto
import com.inclinic.app.features.doctor.reschedule.infrastructure.remote.dto.RespondRescheduleRequestDto

class FakeRescheduleQueueDataSource : RescheduleQueueDataSource {

    var listResult: Result<List<RescheduleRequestDto>> = Result.success(emptyList())
    var respondResult: Result<RescheduleRequestDto> = Result.success(stubDto("req-1", "APPROVED"))

    var lastRespondId: String? = null
    var lastRespondBody: RespondRescheduleRequestDto? = null

    override suspend fun listRequests() = listResult

    override suspend fun respond(id: String, body: RespondRescheduleRequestDto): Result<RescheduleRequestDto> {
        lastRespondId = id
        lastRespondBody = body
        return respondResult
    }
}

fun stubDto(id: String, status: String = "PENDING") = RescheduleRequestDto(
    id = id,
    patientName = "María Quispe",
    currentSlot = "Mar 15 · 10:00",
    requestedSlot = "Mar 17 · 14:00",
    reason = "Tengo viaje de trabajo imprevisto.",
    status = status,
)
