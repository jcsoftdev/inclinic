package com.inclinic.app.features.doctor.sharing.core.port

import com.inclinic.app.features.doctor.sharing.core.model.ShareRequest

interface DoctorSharingRepository {
    /** Lists all share requests sent BY this doctor. */
    suspend fun listRequests(): Result<List<ShareRequest>>
    /** Creates a new share request to a patient. */
    suspend fun requestShare(patientId: String, reason: String, scope: String): Result<ShareRequest>
    /** Doctor cancels their own pending request (DELETE /api/medical-history-share/{id}). */
    suspend fun cancelRequest(id: String): Result<Unit>
}
