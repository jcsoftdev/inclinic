package com.inclinic.app.features.doctor.sharing.application

import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.sharing.core.model.ShareRequestStatus
import com.inclinic.app.features.doctor.sharing.fakes.FakeDoctorSharingRepository
import com.inclinic.app.features.doctor.sharing.fakes.stubRequest
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetIncomingShareRequestsUseCaseTest {

    private val repo = FakeDoctorSharingRepository()
    private val dispatchers = TestAppDispatchers()
    private val useCase = GetIncomingShareRequestsUseCase(repo, dispatchers)

    @Test
    fun returns_empty_list_when_no_approved_requests() = runTest {
        repo.listResult = Result.success(emptyList())
        val result = useCase()
        assertTrue(result.isSuccess)
        assertEquals(emptyList(), result.getOrThrow())
    }

    @Test
    fun filters_to_approved_requests_only() = runTest {
        val requests = listOf(
            stubRequest("r1", ShareRequestStatus.PENDING),
            stubRequest("r2", ShareRequestStatus.APPROVED),
            stubRequest("r3", ShareRequestStatus.REJECTED),
        )
        repo.listResult = Result.success(requests)
        val result = useCase()
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrThrow().size)
        assertEquals("r2", result.getOrThrow()[0].id)
    }

    @Test
    fun propagates_repository_failure() = runTest {
        val error = RuntimeException("Network error")
        repo.listResult = Result.failure(error)
        val result = useCase()
        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }

    @Test
    fun calls_list_requests_on_repository() = runTest {
        useCase()
        assertEquals(1, repo.listCallCount)
    }
}
