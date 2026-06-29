package com.inclinic.app.features.doctor.reschedule.application

import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.reschedule.core.model.RescheduleRequestStatus
import com.inclinic.app.features.doctor.reschedule.fakes.FakeRescheduleQueueRepository
import com.inclinic.app.features.doctor.reschedule.fakes.stubRequest
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RespondRescheduleRequestUseCaseTest {

    private val repo = FakeRescheduleQueueRepository()
    private val dispatchers = TestAppDispatchers()
    private val useCase = RespondRescheduleRequestUseCase(repo, dispatchers)

    @Test
    fun returns_updated_request_on_approve() = runTest {
        repo.respondResult = Result.success(stubRequest("req-1", RescheduleRequestStatus.APPROVED))
        val result = useCase("req-1", RescheduleRequestStatus.APPROVED)
        assertTrue(result.isSuccess)
        assertEquals(RescheduleRequestStatus.APPROVED, result.getOrThrow().status)
    }

    @Test
    fun returns_updated_request_on_reject() = runTest {
        repo.respondResult = Result.success(stubRequest("req-1", RescheduleRequestStatus.REJECTED))
        val result = useCase("req-1", RescheduleRequestStatus.REJECTED)
        assertTrue(result.isSuccess)
        assertEquals(RescheduleRequestStatus.REJECTED, result.getOrThrow().status)
    }

    @Test
    fun passes_correct_id_and_decision_to_repository() = runTest {
        useCase("req-99", RescheduleRequestStatus.APPROVED)
        assertEquals("req-99", repo.lastRespondId)
        assertEquals(RescheduleRequestStatus.APPROVED, repo.lastRespondDecision)
    }

    @Test
    fun propagates_failure() = runTest {
        repo.respondResult = Result.failure(RuntimeException("404"))
        val result = useCase("req-1", RescheduleRequestStatus.APPROVED)
        assertTrue(result.isFailure)
    }
}
