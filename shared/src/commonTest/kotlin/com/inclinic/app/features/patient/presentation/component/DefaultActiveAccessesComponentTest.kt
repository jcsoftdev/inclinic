@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.core.model.ShareRequest
import com.inclinic.app.core.model.ShareScope
import com.inclinic.app.core.model.ShareStatus
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.patient.infrastructure.remote.ShareDataSource
import com.inclinic.app.features.patient.medical_history.application.GetActiveAccessesUseCase
import com.inclinic.app.features.patient.medical_history.application.RevokeAccessUseCase
import kotlinx.coroutines.test.runTest
import kotlin.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

private fun approvedRequest(id: String = "r1") = ShareRequest(
    id = id,
    doctorId = "d1",
    doctorName = "Ana Torres",
    scope = ShareScope.FULL_HISTORY,
    status = ShareStatus.APPROVED,
    requestedAt = Instant.fromEpochSeconds(0),
)

private class FakeShareDataSourceForComponent(
    var listResult: Result<List<ShareRequest>> = Result.success(listOf(approvedRequest())),
    var revokeResult: Result<ShareRequest> = Result.success(approvedRequest().copy(status = ShareStatus.REVOKED)),
    var lastRevokedId: String? = null,
) : ShareDataSource {
    override suspend fun getShareRequests() = listResult
    override suspend fun getShareRequestDetail(requestId: String) = Result.failure<ShareRequest>(UnsupportedOperationException())
    override suspend fun respondToShareRequest(requestId: String, action: String, duration: Int?) = Result.failure<ShareRequest>(UnsupportedOperationException())
    override suspend fun revokeAccess(requestId: String): Result<ShareRequest> {
        lastRevokedId = requestId
        return revokeResult
    }
}

class DefaultActiveAccessesComponentTest {

    private val dispatchers = TestAppDispatchers()
    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val ctx = DefaultComponentContext(lifecycle = lifecycle)

    private fun createComponent(
        dataSource: FakeShareDataSourceForComponent = FakeShareDataSourceForComponent(),
        outputs: MutableList<ActiveAccessesComponent.Output> = mutableListOf(),
    ) = DefaultActiveAccessesComponent(
        componentContext = ctx,
        getActiveAccesses = GetActiveAccessesUseCase(dataSource, dispatchers),
        revokeAccess = RevokeAccessUseCase(dataSource, dispatchers),
        dispatchers = dispatchers,
        onOutput = outputs::add,
    )

    @Test
    fun load_success_populates_accesses_in_state() = runTest {
        val accesses = listOf(approvedRequest("r1"), approvedRequest("r2"))
        val ds = FakeShareDataSourceForComponent(listResult = Result.success(accesses))
        val component = createComponent(ds)

        assertFalse(component.state.value.isLoading)
        // Only APPROVED items returned (both approved in this case)
        assertEquals(2, component.state.value.accesses.size)
    }

    @Test
    fun load_failure_sets_error_in_state() = runTest {
        val ds = FakeShareDataSourceForComponent(listResult = Result.failure(Exception("Network error")))
        val component = createComponent(ds)

        assertFalse(component.state.value.isLoading)
        assertTrue(component.state.value.accesses.isEmpty())
        assertEquals("Network error", component.state.value.error)
    }

    @Test
    fun onRevoke_removes_item_from_state_on_success() = runTest {
        val ds = FakeShareDataSourceForComponent(
            listResult = Result.success(listOf(approvedRequest("r1"), approvedRequest("r2"))),
            revokeResult = Result.success(approvedRequest("r1").copy(status = ShareStatus.REVOKED)),
        )
        val component = createComponent(ds)

        component.onRevoke("r1")

        assertNull(component.state.value.revokingId)
        assertEquals(1, component.state.value.accesses.size)
        assertEquals("r2", component.state.value.accesses.first().id)
        assertEquals("r1", ds.lastRevokedId)
    }

    @Test
    fun onRevoke_sets_error_on_failure() = runTest {
        val ds = FakeShareDataSourceForComponent(
            revokeResult = Result.failure(Exception("Forbidden")),
        )
        val component = createComponent(ds)

        component.onRevoke("r1")

        assertNull(component.state.value.revokingId)
        assertEquals("Forbidden", component.state.value.error)
    }

    @Test
    fun onBack_emits_Back_output() = runTest {
        val outputs = mutableListOf<ActiveAccessesComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onBack()

        assertEquals(1, outputs.size)
        assertTrue(outputs.first() is ActiveAccessesComponent.Output.Back)
    }
}
