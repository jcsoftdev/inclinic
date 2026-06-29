package com.inclinic.app.features.doctor.packages.application

import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.packages.fakes.FakeDoctorPackagesRepository
import com.inclinic.app.features.doctor.packages.fakes.sampleDraft
import com.inclinic.app.features.doctor.packages.fakes.samplePackage
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CreatePackageUseCaseTest {

    private val fakeRepo = FakeDoctorPackagesRepository()
    private val dispatchers = TestAppDispatchers()
    private val useCase = CreatePackageUseCase(repository = fakeRepo, dispatchers = dispatchers)

    private val draft = sampleDraft()

    @Test
    fun returns_created_package_on_success() = runTest {
        val created = samplePackage(id = "new-id")
        fakeRepo.createResult = Result.success(created)

        val result = useCase(draft)

        assertTrue(result.isSuccess)
        assertEquals(created, result.getOrThrow())
        assertEquals(draft, fakeRepo.lastCreated)
        assertEquals(1, fakeRepo.createCallCount)
    }

    @Test
    fun propagates_failure() = runTest {
        val error = RuntimeException("Create failed")
        fakeRepo.createResult = Result.failure(error)

        val result = useCase(draft)

        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }
}
