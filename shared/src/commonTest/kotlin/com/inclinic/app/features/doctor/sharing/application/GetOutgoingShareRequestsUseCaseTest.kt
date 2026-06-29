package com.inclinic.app.features.doctor.sharing.application

import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.sharing.core.model.ShareRequestStatus
import com.inclinic.app.features.doctor.sharing.fakes.FakeDoctorSharingRepository
import com.inclinic.app.features.doctor.sharing.fakes.stubRequest
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetOutgoingShareRequestsUseCaseTest {

    private val repo = FakeDoctorSharingRepository()
    private val dispatchers = TestAppDispatchers()
    private val useCase = GetOutgoingShareRequestsUseCase(repo, dispatchers)

    @Test
    fun returns_empty_list_when_no_non_approved_requests() = runTest {
        repo.listResult = Result.success(emptyList())
        val result = useCase()
        assertTrue(result.isSuccess)
        assertEquals(emptyList(), result.getOrThrow())
    }

    @Test
    fun filters_out_approved_requests() = runTest {
        val requests = listOf(
            stubRequest("o1", ShareRequestStatus.PENDING),
            stubRequest("o2", ShareRequestStatus.APPROVED),
            stubRequest("o3", ShareRequestStatus.EXPIRED),
        )
        repo.listResult = Result.success(requests)
        val result = useCase()
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrThrow().size)
        assertTrue(result.getOrThrow().none { it.status == ShareRequestStatus.APPROVED })
    }

    @Test
    fun propagates_failure() = runTest {
        repo.listResult = Result.failure(RuntimeException("fail"))
        val result = useCase()
        assertTrue(result.isFailure)
    }

    @Test
    fun calls_list_requests_on_repository() = runTest {
        useCase()
        assertEquals(1, repo.listCallCount)
    }
}
