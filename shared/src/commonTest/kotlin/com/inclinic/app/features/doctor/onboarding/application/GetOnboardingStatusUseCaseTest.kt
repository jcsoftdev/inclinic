package com.inclinic.app.features.doctor.onboarding.application

import com.inclinic.app.core.model.OnboardingStatus
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.onboarding.fakes.FakeDoctorOnboardingRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetOnboardingStatusUseCaseTest {

    private val fakeRepo = FakeDoctorOnboardingRepository()
    private val dispatchers = TestAppDispatchers()
    private val useCase = GetOnboardingStatusUseCase(repository = fakeRepo, dispatchers = dispatchers)

    @Test
    fun returns_PENDING_when_repo_holds_PENDING() = runTest {
        fakeRepo.statusResult = Result.success(OnboardingStatus.PENDING)

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(OnboardingStatus.PENDING, result.getOrNull())
    }

    @Test
    fun returns_APPROVED_when_repo_holds_APPROVED() = runTest {
        fakeRepo.statusResult = Result.success(OnboardingStatus.APPROVED)

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(OnboardingStatus.APPROVED, result.getOrNull())
    }

    @Test
    fun returns_REJECTED_when_repo_holds_REJECTED() = runTest {
        fakeRepo.statusResult = Result.success(OnboardingStatus.REJECTED)

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(OnboardingStatus.REJECTED, result.getOrNull())
    }

    @Test
    fun returns_NONE_when_repo_holds_NONE() = runTest {
        fakeRepo.statusResult = Result.success(OnboardingStatus.NONE)

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(OnboardingStatus.NONE, result.getOrNull())
    }

    @Test
    fun propagates_repository_failure() = runTest {
        val error = RuntimeException("Network unavailable")
        fakeRepo.statusResult = Result.failure(error)

        val result = useCase()

        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }
}
