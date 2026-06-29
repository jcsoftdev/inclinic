package com.inclinic.app.features.doctor.onboarding.application

import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.onboarding.fakes.FakeDoctorOnboardingRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ResubmitOnboardingUseCaseTest {

    private val fakeRepo = FakeDoctorOnboardingRepository()
    private val dispatchers = TestAppDispatchers()
    private val useCase = ResubmitOnboardingUseCase(repository = fakeRepo, dispatchers = dispatchers)

    @Test
    fun happy_path_returns_success_and_delegates_corrections() = runTest {
        val corrections = mapOf("cmpLicense" to "CMP-99999", "phone" to "+51999000999")
        fakeRepo.resubmitResult = Result.success(Unit)

        val result = useCase(corrections)

        assertTrue(result.isSuccess)
        assertEquals(1, fakeRepo.resubmitCallCount)
        assertEquals(corrections, fakeRepo.lastResubmitCorrections)
    }

    @Test
    fun propagates_repository_error() = runTest {
        val error = RuntimeException("Server rejected")
        fakeRepo.resubmitResult = Result.failure(error)

        val result = useCase(mapOf("field" to "value"))

        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }
}
