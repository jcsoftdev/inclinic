package com.inclinic.app.features.doctor.reschedule.application

import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.reschedule.fakes.FakeRescheduleQueueRepository
import com.inclinic.app.features.doctor.reschedule.fakes.stubRequest
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetRescheduleRequestsUseCaseTest {

    private val repo = FakeRescheduleQueueRepository()
    private val dispatchers = TestAppDispatchers()
    private val useCase = GetRescheduleRequestsUseCase(repo, dispatchers)

    @Test
    fun returns_requests_from_repository() = runTest {
        repo.listResult = Result.success(listOf(stubRequest("r1"), stubRequest("r2")))
        val result = useCase()
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrThrow().size)
    }

    @Test
    fun delegates_to_repository() = runTest {
        useCase()
        assertEquals(1, repo.listCallCount)
    }

    @Test
    fun propagates_failure() = runTest {
        repo.listResult = Result.failure(RuntimeException("500"))
        val result = useCase()
        assertTrue(result.isFailure)
    }
}
