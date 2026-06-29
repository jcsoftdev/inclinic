package com.inclinic.app.features.doctor.packages.application

import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.packages.fakes.FakeDoctorPackagesRepository
import com.inclinic.app.features.doctor.packages.fakes.samplePackage
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetDoctorPackagesUseCaseTest {

    private val fakeRepo = FakeDoctorPackagesRepository()
    private val dispatchers = TestAppDispatchers()
    private val useCase = GetDoctorPackagesUseCase(repository = fakeRepo, dispatchers = dispatchers)

    @Test
    fun returns_empty_list_when_no_packages() = runTest {
        fakeRepo.listResult = Result.success(emptyList())

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(emptyList(), result.getOrThrow())
    }

    @Test
    fun returns_packages_on_success() = runTest {
        val packages = listOf(samplePackage())
        fakeRepo.listResult = Result.success(packages)

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(packages, result.getOrThrow())
        assertEquals(1, fakeRepo.listCallCount)
    }

    @Test
    fun propagates_failure() = runTest {
        val error = RuntimeException("Server error")
        fakeRepo.listResult = Result.failure(error)

        val result = useCase()

        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }
}
