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

private class FakeRespondShareDataSource : ShareDataSource {
    var respondResult: Result<ShareRequest> = Result.success(
        ShareRequest(id = "sr-1", doctorId = "doc-1", scope = ShareScope.FULL_HISTORY, status = ShareStatus.APPROVED, requestedAt = Clock.System.now())
    )
    var lastRequestId: String? = null
    var lastAction: String? = null
    var lastDuration: Int? = null
    var callCount = 0

    override suspend fun respondToShareRequest(requestId: String, action: String, duration: Int?): Result<ShareRequest> {
        callCount++
        lastRequestId = requestId
        lastAction = action
        lastDuration = duration
        return respondResult
    }

    override suspend fun getShareRequests(): Result<List<ShareRequest>> = Result.success(emptyList())
    override suspend fun getShareRequestDetail(requestId: String): Result<ShareRequest> = Result.failure(UnsupportedOperationException())
    override suspend fun revokeAccess(requestId: String): Result<ShareRequest> = Result.failure(UnsupportedOperationException())
}

class RespondShareRequestUseCaseTest {

    private val fake = FakeRespondShareDataSource()
    private val useCase = RespondShareRequestUseCase(dataSource = fake, dispatchers = TestAppDispatchers())

    @Test
    fun approve_maps_to_approve_action_with_duration() = runTest {
        val result = useCase("sr-1", approved = true, durationDays = 30)

        assertTrue(result.isSuccess)
        assertEquals("sr-1", fake.lastRequestId)
        assertEquals("approve", fake.lastAction)
        assertEquals(30, fake.lastDuration)
    }

    @Test
    fun reject_maps_to_reject_action_with_null_duration() = runTest {
        val result = useCase("sr-2", approved = false, durationDays = null)

        assertTrue(result.isSuccess)
        assertEquals("reject", fake.lastAction)
        assertEquals(null, fake.lastDuration)
    }

    @Test
    fun failure_propagates_error() = runTest {
        fake.respondResult = Result.failure(Exception("Already responded"))

        val result = useCase("sr-1", approved = true, durationDays = 7)

        assertTrue(result.isFailure)
        assertEquals("Already responded", result.exceptionOrNull()?.message)
    }
}
