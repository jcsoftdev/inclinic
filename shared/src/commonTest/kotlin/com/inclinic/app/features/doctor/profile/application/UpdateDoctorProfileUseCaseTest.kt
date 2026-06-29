package com.inclinic.app.features.doctor.profile.application

import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.profile.fakes.FakeDoctorProfileRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UpdateDoctorProfileUseCaseTest {

    private val fakeRepo = FakeDoctorProfileRepository()
    private val dispatchers = TestAppDispatchers()
    private val useCase = UpdateDoctorProfileUseCase(repository = fakeRepo, dispatchers = dispatchers)

    @Test
    fun delegates_profile_to_repository_and_returns_updated() = runTest {
        val updated = FakeDoctorProfileRepository.defaultProfile.copy(bio = "Nueva bio")
        fakeRepo.updateProfileResult = Result.success(updated)

        val result = useCase(updated)

        assertTrue(result.isSuccess)
        assertEquals("Nueva bio", result.getOrThrow().bio)
        assertEquals(1, fakeRepo.updateProfileCallCount)
        assertEquals(updated, fakeRepo.lastUpdatedProfile)
    }

    @Test
    fun propagates_failure_from_repository() = runTest {
        val error = RuntimeException("Server error")
        fakeRepo.updateProfileResult = Result.failure(error)

        val result = useCase(FakeDoctorProfileRepository.defaultProfile)

        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }
}
