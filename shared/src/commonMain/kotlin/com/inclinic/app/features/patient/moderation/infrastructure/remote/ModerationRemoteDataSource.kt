package com.inclinic.app.features.patient.moderation.infrastructure.remote

import com.inclinic.app.features.patient.moderation.core.model.ReportCategory

/** Port: remote moderation operations. */
interface ModerationRemoteDataSource {
    /** POST /api/moderation/report. reason must be 10..2000 chars. */
    suspend fun reportUser(userId: String, reason: String, category: ReportCategory?): Result<Unit>

    /** POST /api/moderation/block. reason is optional, max 500 chars. */
    suspend fun blockUser(userId: String, reason: String?): Result<Unit>

    /** DELETE /api/moderation/block/:userId */
    suspend fun unblockUser(userId: String): Result<Unit>
}
