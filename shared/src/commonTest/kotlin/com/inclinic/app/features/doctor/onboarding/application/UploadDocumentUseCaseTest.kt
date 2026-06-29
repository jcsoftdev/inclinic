package com.inclinic.app.features.doctor.onboarding.application

import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.onboarding.core.model.DocKind
import com.inclinic.app.features.doctor.onboarding.core.model.UploadedDoc
import com.inclinic.app.features.doctor.onboarding.fakes.FakeDoctorOnboardingRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UploadDocumentUseCaseTest {

    private val fakeRepo = FakeDoctorOnboardingRepository()
    private val dispatchers = TestAppDispatchers()
    private val useCase = UploadDocumentUseCase(repository = fakeRepo, dispatchers = dispatchers)

    @Test
    fun happy_path_returns_uploaded_doc_and_delegates_to_repo() = runTest {
        val expected = UploadedDoc(id = "doc-42", kind = DocKind.DIPLOMA, url = "https://cdn.inclinic.com/diploma")
        fakeRepo.uploadResult = Result.success(expected)
        val fileBytes = byteArrayOf(1, 2, 3)

        val result = useCase(file = fileBytes, fileName = "diploma.pdf", kind = DocKind.DIPLOMA)

        assertTrue(result.isSuccess)
        assertEquals(expected, result.getOrNull())
        assertEquals(1, fakeRepo.uploadCallCount)
        assertEquals("diploma.pdf", fakeRepo.lastUploadedFileName)
        assertEquals(DocKind.DIPLOMA, fakeRepo.lastUploadedKind)
    }

    @Test
    fun propagates_repository_error() = runTest {
        val error = RuntimeException("Upload failed")
        fakeRepo.uploadResult = Result.failure(error)

        val result = useCase(file = byteArrayOf(1), fileName = "id.jpg", kind = DocKind.ID_FRONT)

        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }
}
