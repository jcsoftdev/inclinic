package com.inclinic.app.features.doctor.sharing.infrastructure.remote

import com.inclinic.app.features.doctor.sharing.infrastructure.remote.dto.CreateShareRequestDto
import com.inclinic.app.features.doctor.sharing.infrastructure.remote.dto.ShareRequestDto

interface DoctorSharingDataSource {
    /** GET /api/medical-history-share — returns doctor's own share requests. */
    suspend fun listRequests(): Result<List<ShareRequestDto>>
    /** POST /api/medical-history-share — doctor creates a share request. */
    suspend fun requestShare(body: CreateShareRequestDto): Result<ShareRequestDto>
    /** DELETE /api/medical-history-share/{id} — doctor cancels a pending request. */
    suspend fun cancelRequest(id: String): Result<Unit>
}
