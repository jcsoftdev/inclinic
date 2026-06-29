package com.inclinic.app.features.doctor.profile.application

import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.profile.fakes.FakeDoctorProfileRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetDoctorProfileUseCaseTest {

    private val fakeRepo = FakeDoctorProfileRepository()
    private val dispatchers = TestAppDispatchers()
    private val useCase = GetDoctorProfileUseCase(repository = fakeRepo, dispatchers = dispatchers)

    @Test
    fun returns_profile_on_success() = runTest {
        fakeRepo.getProfileResult = Result.success(FakeDoctorProfileRepository.defaultProfile)

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals("doc-1", result.getOrThrow().id)
        assertEquals(1, fakeRepo.getProfileCallCount)
    }

    @Test
    fun propagates_repository_failure() = runTest {
        val error = RuntimeException("Network unavailable")
        fakeRepo.getProfileResult = Result.failure(error)

        val result = useCase()

        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }
}
