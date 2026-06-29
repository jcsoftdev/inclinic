package com.inclinic.app.features.doctor.sharing.application

import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.sharing.fakes.FakeDoctorSharingRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RespondShareRequestUseCaseTest {

    private val repo = FakeDoctorSharingRepository()
    private val dispatchers = TestAppDispatchers()
    private val useCase = RespondShareRequestUseCase(repo, dispatchers)

    @Test
    fun returns_success_when_cancel_succeeds() = runTest {
        repo.cancelResult = Result.success(Unit)
        val result = useCase("req-1")
        assertTrue(result.isSuccess)
    }

    @Test
    fun passes_correct_id_to_repository() = runTest {
        useCase("req-99")
        assertEquals("req-99", repo.lastCancelledId)
    }

    @Test
    fun propagates_failure() = runTest {
        repo.cancelResult = Result.failure(RuntimeException("404"))
        val result = useCase("req-1")
        assertTrue(result.isFailure)
    }

    @Test
    fun calls_cancel_on_repository() = runTest {
        useCase("some-id")
        assertEquals(1, repo.cancelCallCount)
    }
}
