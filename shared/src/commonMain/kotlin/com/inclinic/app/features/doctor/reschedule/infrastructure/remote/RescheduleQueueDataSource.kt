package com.inclinic.app.features.doctor.reschedule.infrastructure.remote

import com.inclinic.app.features.doctor.reschedule.infrastructure.remote.dto.RescheduleRequestDto
import com.inclinic.app.features.doctor.reschedule.infrastructure.remote.dto.RespondRescheduleRequestDto

interface RescheduleQueueDataSource {
    suspend fun listRequests(): Result<List<RescheduleRequestDto>>
    suspend fun respond(id: String, body: RespondRescheduleRequestDto): Result<RescheduleRequestDto>
}
