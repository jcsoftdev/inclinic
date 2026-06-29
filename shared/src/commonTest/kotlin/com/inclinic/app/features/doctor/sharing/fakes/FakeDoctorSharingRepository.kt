package com.inclinic.app.features.doctor.sharing.fakes

import com.inclinic.app.features.doctor.sharing.core.model.ShareRequest
import com.inclinic.app.features.doctor.sharing.core.model.ShareRequestStatus
import com.inclinic.app.features.doctor.sharing.core.model.ShareScope
import com.inclinic.app.features.doctor.sharing.core.port.DoctorSharingRepository
import kotlin.time.Instant

class FakeDoctorSharingRepository : DoctorSharingRepository {

    var listResult: Result<List<ShareRequest>> = Result.success(emptyList())
    var requestShareResult: Result<ShareRequest> = Result.success(stubRequest("req-1"))
    var cancelResult: Result<Unit> = Result.success(Unit)

    var listCallCount = 0
    var requestShareCallCount = 0
    var cancelCallCount = 0

    var lastRequestPatientId: String? = null
    var lastRequestReason: String? = null
    var lastCancelledId: String? = null

    override suspend fun listRequests(): Result<List<ShareRequest>> {
        listCallCount++
        return listResult
    }

    override suspend fun requestShare(
        patientId: String,
        reason: String,
        scope: String,
    ): Result<ShareRequest> {
        requestShareCallCount++
        lastRequestPatientId = patientId
        lastRequestReason = reason
        return requestShareResult
    }

    override suspend fun cancelRequest(id: String): Result<Unit> {
        cancelCallCount++
        lastCancelledId = id
        return cancelResult
    }
}

fun stubRequest(
    id: String,
    status: ShareRequestStatus = ShareRequestStatus.PENDING,
) = ShareRequest(
    id = id,
    patientId = "patient-1",
    patientName = "Juan Pérez",
    requesterDoctorId = "doc-2",
    reason = "Consulta de seguimiento médico del paciente",
    scope = ShareScope.FULL_HISTORY,
    status = status,
    requestedAt = Instant.fromEpochMilliseconds(0),
    expiresAt = null,
)
