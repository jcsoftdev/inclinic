package com.inclinic.app.features.doctor.profile.application

import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.profile.fakes.FakeDoctorProfileRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetDoctorIncomeUseCaseTest {

    private val fakeRepo = FakeDoctorProfileRepository()
    private val dispatchers = TestAppDispatchers()
    private val useCase = GetDoctorIncomeUseCase(repository = fakeRepo, dispatchers = dispatchers)

    @Test
    fun returns_income_summary_on_success() = runTest {
        val result = useCase()

        assertTrue(result.isSuccess)
        val summary = result.getOrThrow()
        assertEquals(18000L, summary.totalCents)
        assertEquals(2700L, summary.commissionCents)
        assertEquals(15300L, summary.netCents)
        assertEquals(6, summary.sessions)
        assertEquals(1, fakeRepo.getIncomeCallCount)
    }

    @Test
    fun propagates_failure() = runTest {
        fakeRepo.getIncomeResult = Result.failure(RuntimeException("Timeout"))

        val result = useCase()

        assertTrue(result.isFailure)
    }

    @Test
    fun calls_repository_exactly_once() = runTest {
        useCase()

        assertEquals(1, fakeRepo.getIncomeCallCount)
    }
}
