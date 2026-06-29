package com.inclinic.app.core.upload

import com.inclinic.app.core.concurrency.AppDispatchers
import kotlinx.coroutines.withContext

/**
 * Uploads a file to the specified [bucket] via [UploadDataSource] and returns
 * the publicly accessible URL assigned by the backend.
 *
 * Buckets in use:
 *   - `"documents"` → RegisterDoctor validation docs
 *   - `"specialty-request-docs"` → RequestSpecialty cert/diploma
 *   - `"medical-attachments"` → Doctor/patient chat attachments
 */
class UploadFileUseCase(
    private val dataSource: UploadDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(
        bucket: String,
        bytes: ByteArray,
        fileName: String,
        mimeType: String,
    ): Result<String> = withContext(dispatchers.io) {
        dataSource.upload(bucket, bytes, fileName, mimeType).map { it.url }
    }
}
