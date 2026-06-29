package com.inclinic.app.features.patient.medical_history.application

import com.inclinic.app.core.model.ShareRequest
import com.inclinic.app.core.model.ShareScope
import com.inclinic.app.core.model.ShareStatus
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.patient.infrastructure.remote.ShareDataSource
import kotlinx.coroutines.test.runTest
import kotlin.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class FakeShareDataSourceForActiveAccesses : ShareDataSource {
    var shareRequests: List<ShareRequest> = emptyList()
    var getShareRequestsError: Exception? = null

    override suspend fun getShareRequests(): Result<List<ShareRequest>> {
        val err = getShareRequestsError
        return if (err != null) Result.failure(err) else Result.success(shareRequests)
    }
    override suspend fun getShareRequestDetail(requestId: String): Result<ShareRequest> =
        Result.failure(UnsupportedOperationException())
    override suspend fun respondToShareRequest(requestId: String, action: String, duration: Int?): Result<ShareRequest> =
        Result.failure(UnsupportedOperationException())
    override suspend fun revokeAccess(requestId: String): Result<ShareRequest> =
        Result.failure(UnsupportedOperationException())
}

class GetActiveAccessesUseCaseTest {

    private val fakeDataSource = FakeShareDataSourceForActiveAccesses()
    private val dispatchers = TestAppDispatchers()
    private val useCase = GetActiveAccessesUseCase(fakeDataSource, dispatchers)

    private val approved = ShareRequest(
        id = "r1",
        doctorId = "d1",
        doctorName = "Ana Torres",
        scope = ShareScope.FULL_HISTORY,
        status = ShareStatus.APPROVED,
        requestedAt = Instant.fromEpochSeconds(0),
    )
    private val pending = ShareRequest(
        id = "r2",
        doctorId = "d2",
        doctorName = "Miguel Vargas",
        scope = ShareScope.FULL_HISTORY,
        status = ShareStatus.PENDING,
        requestedAt = Instant.fromEpochSeconds(0),
    )
    private val revoked = ShareRequest(
        id = "r3",
        doctorId = "d3",
        doctorName = "Luis Gómez",
        scope = ShareScope.FULL_HISTORY,
        status = ShareStatus.REVOKED,
        requestedAt = Instant.fromEpochSeconds(0),
    )

    @Test
    fun returns_only_approved_accesses() = runTest {
        fakeDataSource.shareRequests = listOf(approved, pending, revoked)

        val result = useCase()

        assertTrue(result.isSuccess)
        val list = result.getOrNull()!!
        assertEquals(1, list.size)
        assertEquals("r1", list.first().id)
        assertEquals(ShareStatus.APPROVED, list.first().status)
    }

    @Test
    fun returns_empty_list_when_no_approved_accesses() = runTest {
        fakeDataSource.shareRequests = listOf(pending, revoked)

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(emptyList(), result.getOrNull())
    }

    @Test
    fun propagates_data_source_failure() = runTest {
        fakeDataSource.getShareRequestsError = RuntimeException("Network error")

        val result = useCase()

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }
}
