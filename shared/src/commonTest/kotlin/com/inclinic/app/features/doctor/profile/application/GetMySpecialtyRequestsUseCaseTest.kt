package com.inclinic.app.features.doctor.profile.application

import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.profile.core.model.SpecialtyRequestStatus
import com.inclinic.app.features.doctor.profile.fakes.FakeDoctorProfileRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetMySpecialtyRequestsUseCaseTest {

    private val fakeRepo = FakeDoctorProfileRepository()
    private val dispatchers = TestAppDispatchers()
    private val useCase = GetMySpecialtyRequestsUseCase(repository = fakeRepo, dispatchers = dispatchers)

    @Test
    fun returns_requests_on_success() = runTest {
        val result = useCase()

        assertTrue(result.isSuccess)
        val requests = result.getOrThrow()
        assertEquals(3, requests.size)
        assertEquals(SpecialtyRequestStatus.Pending, requests.first().status)
        assertEquals(1, fakeRepo.getMySpecialtyRequestsCallCount)
    }

    @Test
    fun propagates_failure() = runTest {
        fakeRepo.getMySpecialtyRequestsResult = Result.failure(RuntimeException("Timeout"))

        val result = useCase()

        assertTrue(result.isFailure)
    }
}
