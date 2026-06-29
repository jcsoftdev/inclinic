package com.inclinic.app.features.doctor.sharing.infrastructure

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.sharing.core.model.ShareRequest
import com.inclinic.app.features.doctor.sharing.core.model.ShareRequestStatus
import com.inclinic.app.features.doctor.sharing.core.model.ShareScope
import com.inclinic.app.features.doctor.sharing.core.port.DoctorSharingRepository
import com.inclinic.app.features.doctor.sharing.infrastructure.remote.DoctorSharingDataSource
import com.inclinic.app.features.doctor.sharing.infrastructure.remote.dto.CreateShareRequestDto
import com.inclinic.app.features.doctor.sharing.infrastructure.remote.dto.ShareRequestDto
import kotlinx.coroutines.withContext
import kotlin.time.Instant

class DefaultDoctorSharingRepository(
    private val remote: DoctorSharingDataSource,
    private val dispatchers: AppDispatchers,
) : DoctorSharingRepository {

    override suspend fun listRequests(): Result<List<ShareRequest>> =
        withContext(dispatchers.io) {
            remote.listRequests().map { list -> list.map { it.toDomain() } }
        }

    override suspend fun requestShare(
        patientId: String,
        reason: String,
        scope: String,
    ): Result<ShareRequest> =
        withContext(dispatchers.io) {
            remote.requestShare(CreateShareRequestDto(patientId, reason, scope))
                .map { it.toDomain() }
        }

    override suspend fun cancelRequest(id: String): Result<Unit> =
        withContext(dispatchers.io) { remote.cancelRequest(id) }

    // ── Mapping ───────────────────────────────────────────────────────────────

    private fun ShareRequestDto.toDomain() = ShareRequest(
        id = id,
        patientId = patientId,
        patientName = patient?.user?.let { "${it.firstName} ${it.lastName}".trim() } ?: patientId,
        requesterDoctorId = requesterDoctorId,
        reason = reason,
        scope = runCatching { ShareScope.valueOf(scope) }.getOrElse { ShareScope.FULL_HISTORY },
        status = runCatching { ShareRequestStatus.valueOf(status) }.getOrElse { ShareRequestStatus.PENDING },
        requestedAt = runCatching { Instant.parse(createdAt) }.getOrElse { Instant.fromEpochMilliseconds(0) },
        expiresAt = expiresAt?.let { runCatching { Instant.parse(it) }.getOrNull() },
    )
}
