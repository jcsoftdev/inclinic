package com.inclinic.app.features.patient.infrastructure.remote

import com.inclinic.app.core.model.ShareRequest

interface ShareDataSource {
    suspend fun getShareRequests(): Result<List<ShareRequest>>
    suspend fun getShareRequestDetail(requestId: String): Result<ShareRequest>
    suspend fun respondToShareRequest(requestId: String, action: String, duration: Int? = null): Result<ShareRequest>
    suspend fun revokeAccess(requestId: String): Result<ShareRequest>
}
