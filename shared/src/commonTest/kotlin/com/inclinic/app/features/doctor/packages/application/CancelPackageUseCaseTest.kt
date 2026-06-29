package com.inclinic.app.features.doctor.packages.application

import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.packages.fakes.FakeDoctorPackagesRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CancelPackageUseCaseTest {

    private val fakeRepo = FakeDoctorPackagesRepository()
    private val dispatchers = TestAppDispatchers()
    private val useCase = CancelPackageUseCase(repository = fakeRepo, dispatchers = dispatchers)

    @Test
    fun returns_success_when_cancel_succeeds() = runTest {
        fakeRepo.cancelResult = Result.success(Unit)

        val result = useCase("pkg-1")

        assertTrue(result.isSuccess)
        assertEquals("pkg-1", fakeRepo.lastCancelledId)
        assertEquals(1, fakeRepo.cancelCallCount)
    }

    @Test
    fun propagates_failure() = runTest {
        val error = RuntimeException("Cancel failed")
        fakeRepo.cancelResult = Result.failure(error)

        val result = useCase("pkg-2")

        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }
}
