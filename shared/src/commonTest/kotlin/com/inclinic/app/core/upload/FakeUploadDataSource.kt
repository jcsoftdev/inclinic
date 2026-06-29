package com.inclinic.app.core.upload

import com.inclinic.app.features.patient.infrastructure.remote.dto.UploadResultDto

/**
 * Controllable fake for [UploadDataSource] in unit tests.
 */
class FakeUploadDataSource : UploadDataSource {

    var result: Result<UploadResultDto> = Result.success(
        UploadResultDto(
            url = "https://cdn.inclinic.com/test/file.pdf",
            path = "test/file.pdf",
            bucket = "documents",
            size = 1024L,
            type = "application/pdf",
        )
    )

    var uploadCallCount = 0
    var lastBucket: String? = null
    var lastBytes: ByteArray? = null
    var lastFileName: String? = null
    var lastMimeType: String? = null

    override suspend fun upload(
        bucket: String,
        bytes: ByteArray,
        fileName: String,
        mimeType: String,
    ): Result<UploadResultDto> {
        uploadCallCount++
        lastBucket = bucket
        lastBytes = bytes
        lastFileName = fileName
        lastMimeType = mimeType
        return result
    }
}
