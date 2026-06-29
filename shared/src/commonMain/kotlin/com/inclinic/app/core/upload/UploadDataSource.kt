package com.inclinic.app.core.upload

import com.inclinic.app.features.patient.infrastructure.remote.dto.UploadResultDto

/**
 * Port for the shared file-upload endpoint (POST /api/upload).
 * Each caller specifies the target [bucket]; the server validates mime/size.
 */
interface UploadDataSource {
    /**
     * Uploads [bytes] to [bucket] and returns the server-assigned [UploadResultDto]
     * (including the public URL).
     *
     * Server constraints: max 10 MB, allowed mimes:
     *   image/jpeg, image/png, image/webp, image/gif, application/pdf.
     */
    suspend fun upload(
        bucket: String,
        bytes: ByteArray,
        fileName: String,
        mimeType: String,
    ): Result<UploadResultDto>
}
