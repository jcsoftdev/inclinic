@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.patient.medical_history.application

import com.inclinic.app.core.model.ShareRequest
import com.inclinic.app.core.model.ShareScope
import com.inclinic.app.core.model.ShareStatus
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.patient.infrastructure.remote.ShareDataSource
import kotlinx.coroutines.test.runTest
import kotlin.time.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class FakeShareDataSource : ShareDataSource {
    var listResult: Result<List<ShareRequest>> = Result.success(emptyList())
    var callCount = 0

    override suspend fun getShareRequests(): Result<List<ShareRequest>> {
        callCount++
        return listResult
    }

    override suspend fun getShareRequestDetail(requestId: String): Result<ShareRequest> = Result.failure(UnsupportedOperationException())
    override suspend fun respondToShareRequest(requestId: String, action: String, duration: Int?): Result<ShareRequest> = Result.failure(UnsupportedOperationException())
    override suspend fun revokeAccess(requestId: String): Result<ShareRequest> = Result.failure(UnsupportedOperationException())
}

class GetShareRequestsUseCaseTest {

    private val fake = FakeShareDataSource()
    private val useCase = GetShareRequestsUseCase(dataSource = fake, dispatchers = TestAppDispatchers())

    @Test
    fun success_returns_share_requests() = runTest {
        val requests = listOf(
            ShareRequest(id = "sr-1", doctorId = "doc-1", doctorName = "Dr. Torres", scope = ShareScope.FULL_HISTORY, status = ShareStatus.PENDING, requestedAt = Clock.System.now()),
        )
        fake.listResult = Result.success(requests)

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals("sr-1", result.getOrNull()?.first()?.id)
    }

    @Test
    fun failure_propagates_error() = runTest {
        fake.listResult = Result.failure(Exception("Forbidden"))

        val result = useCase()

        assertTrue(result.isFailure)
        assertEquals("Forbidden", result.exceptionOrNull()?.message)
    }
}
