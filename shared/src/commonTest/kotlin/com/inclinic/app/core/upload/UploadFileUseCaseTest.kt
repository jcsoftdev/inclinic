package com.inclinic.app.core.upload

import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.patient.infrastructure.remote.dto.UploadResultDto
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * RED → GREEN tests for [UploadFileUseCase].
 *
 * TDD cycle:
 *   RED   — UploadFileUseCase does not exist; tests fail to compile.
 *   GREEN — UploadFileUseCase delegates to UploadDataSource and maps DTO → URL.
 */
class UploadFileUseCaseTest {

    private val fake = FakeUploadDataSource()
    private val dispatchers = TestAppDispatchers()
    private val useCase = UploadFileUseCase(dataSource = fake, dispatchers = dispatchers)

    @Test
    fun happy_path_returns_uploaded_url() = runTest {
        val expected = UploadResultDto(
            url = "https://cdn.inclinic.com/docs/cert.pdf",
            path = "docs/cert.pdf",
            bucket = "documents",
            size = 2048L,
            type = "application/pdf",
        )
        fake.result = Result.success(expected)

        val result = useCase(
            bucket = "documents",
            bytes = byteArrayOf(1, 2, 3),
            fileName = "cert.pdf",
            mimeType = "application/pdf",
        )

        assertTrue(result.isSuccess)
        assertEquals("https://cdn.inclinic.com/docs/cert.pdf", result.getOrNull())
    }

    @Test
    fun delegates_correct_params_to_datasource() = runTest {
        useCase(
            bucket = "specialty-request-docs",
            bytes = byteArrayOf(10, 20),
            fileName = "diploma.pdf",
            mimeType = "application/pdf",
        )

        assertEquals(1, fake.uploadCallCount)
        assertEquals("specialty-request-docs", fake.lastBucket)
        assertEquals("diploma.pdf", fake.lastFileName)
        assertEquals("application/pdf", fake.lastMimeType)
    }

    @Test
    fun propagates_datasource_failure() = runTest {
        val error = RuntimeException("Upload failed — 10 MB limit exceeded")
        fake.result = Result.failure(error)

        val result = useCase(
            bucket = "medical-attachments",
            bytes = byteArrayOf(1),
            fileName = "scan.png",
            mimeType = "image/png",
        )

        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }
}
