package com.inclinic.app.features.doctor.profile.application

import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.profile.core.model.SpecialtyRequest
import com.inclinic.app.features.doctor.profile.fakes.FakeDoctorProfileRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RequestSpecialtyUseCaseTest {

    private val fakeRepo = FakeDoctorProfileRepository()
    private val dispatchers = TestAppDispatchers()
    private val useCase = RequestSpecialtyUseCase(repository = fakeRepo, dispatchers = dispatchers)

    private val sampleRequest = SpecialtyRequest(
        specialtyName = "Neurología",
        documentUrls = listOf("https://cdn.inclinic.com/doc-a"),
        comment = "Tengo 10 años de experiencia.",
    )

    @Test
    fun passes_request_to_repository_on_success() = runTest {
        val result = useCase(sampleRequest)

        assertTrue(result.isSuccess)
        assertEquals(1, fakeRepo.requestSpecialtyCallCount)
        assertEquals(sampleRequest, fakeRepo.lastSpecialtyRequest)
    }

    @Test
    fun propagates_failure() = runTest {
        fakeRepo.requestSpecialtyResult = Result.failure(RuntimeException("Upload failed"))

        val result = useCase(sampleRequest)

        assertTrue(result.isFailure)
    }
}
