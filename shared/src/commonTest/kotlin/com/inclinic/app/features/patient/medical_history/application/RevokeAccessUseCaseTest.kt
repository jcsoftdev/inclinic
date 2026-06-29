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

private class FakeShareDataSourceForRevoke : ShareDataSource {
    var revokeResult: Result<ShareRequest> = Result.failure(UnsupportedOperationException())
    var lastRevokedId: String? = null

    override suspend fun getShareRequests(): Result<List<ShareRequest>> =
        Result.failure(UnsupportedOperationException())
    override suspend fun getShareRequestDetail(requestId: String): Result<ShareRequest> =
        Result.failure(UnsupportedOperationException())
    override suspend fun respondToShareRequest(requestId: String, action: String, duration: Int?): Result<ShareRequest> =
        Result.failure(UnsupportedOperationException())
    override suspend fun revokeAccess(requestId: String): Result<ShareRequest> {
        lastRevokedId = requestId
        return revokeResult
    }
}

class RevokeAccessUseCaseTest {

    private val fakeDataSource = FakeShareDataSourceForRevoke()
    private val dispatchers = TestAppDispatchers()
    private val useCase = RevokeAccessUseCase(fakeDataSource, dispatchers)

    private val revokedResult = ShareRequest(
        id = "r1",
        doctorId = "d1",
        doctorName = "Ana Torres",
        scope = ShareScope.FULL_HISTORY,
        status = ShareStatus.REVOKED,
        requestedAt = Instant.fromEpochSeconds(0),
    )

    @Test
    fun delegates_delete_to_data_source_and_returns_result() = runTest {
        fakeDataSource.revokeResult = Result.success(revokedResult)

        val result = useCase("r1")

        assertTrue(result.isSuccess)
        assertEquals(ShareStatus.REVOKED, result.getOrNull()?.status)
        assertEquals("r1", fakeDataSource.lastRevokedId)
    }

    @Test
    fun propagates_data_source_failure() = runTest {
        fakeDataSource.revokeResult = Result.failure(RuntimeException("Forbidden"))

        val result = useCase("r1")

        assertTrue(result.isFailure)
        assertEquals("Forbidden", result.exceptionOrNull()?.message)
    }
}
