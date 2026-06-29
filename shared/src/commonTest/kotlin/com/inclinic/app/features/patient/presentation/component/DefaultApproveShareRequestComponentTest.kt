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
import com.inclinic.app.features.patient.medical_history.application.GetShareRequestDetailUseCase
import com.inclinic.app.features.patient.medical_history.application.RespondShareRequestUseCase
import kotlinx.coroutines.test.runTest
import kotlin.time.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private fun testShareRequest(status: ShareStatus = ShareStatus.PENDING): ShareRequest = ShareRequest(
    id = "sr-1", doctorId = "doc-1", doctorName = "Dr. Torres",
    scope = ShareScope.FULL_HISTORY, status = status, requestedAt = Clock.System.now(),
)

private class FakeShareDataSource(
    private val detailResult: Result<ShareRequest> = Result.success(testShareRequest()),
    private var respondResult: Result<ShareRequest> = Result.success(testShareRequest(ShareStatus.APPROVED)),
) : ShareDataSource {
    var respondCallCount = 0
    var lastAction: String? = null
    var lastDuration: Int? = null

    override suspend fun getShareRequestDetail(requestId: String): Result<ShareRequest> = detailResult

    override suspend fun respondToShareRequest(requestId: String, action: String, duration: Int?): Result<ShareRequest> {
        respondCallCount++
        lastAction = action
        lastDuration = duration
        return respondResult
    }

    override suspend fun getShareRequests(): Result<List<ShareRequest>> = Result.success(emptyList())
    override suspend fun revokeAccess(requestId: String): Result<ShareRequest> = Result.failure(UnsupportedOperationException())

    fun setRespondResult(result: Result<ShareRequest>) { respondResult = result }
}

class DefaultApproveShareRequestComponentTest {

    private val dispatchers = TestAppDispatchers()
    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val ctx = DefaultComponentContext(lifecycle = lifecycle)

    private fun createComponent(
        dataSource: FakeShareDataSource = FakeShareDataSource(),
        outputs: MutableList<ApproveShareRequestComponent.Output> = mutableListOf(),
    ): DefaultApproveShareRequestComponent {
        return DefaultApproveShareRequestComponent(
            componentContext = ctx,
            requestId = "sr-1",
            getDetail = GetShareRequestDetailUseCase(dataSource, dispatchers),
            respond = RespondShareRequestUseCase(dataSource, dispatchers),
            dispatchers = dispatchers,
            onOutput = outputs::add,
        )
    }

    @Test
    fun load_success_sets_request_in_state() = runTest {
        val component = createComponent()

        val state = component.state.value
        assertFalse(state.isLoading)
        assertNotNull(state.request)
        assertEquals("sr-1", state.request?.id)
        assertEquals("Dr. Torres", state.request?.doctorName)
    }

    @Test
    fun load_failure_sets_error() = runTest {
        val failingDs = FakeShareDataSource(detailResult = Result.failure(Exception("Not found")))
        val component = createComponent(dataSource = failingDs)

        val state = component.state.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
        assertEquals("Not found", state.error)
    }

    @Test
    fun onDurationSelected_updates_selectedDuration() = runTest {
        val component = createComponent()

        component.onDurationSelected(30)

        assertEquals(30, component.state.value.selectedDuration)
    }

    @Test
    fun onApprove_success_emits_Approved_output() = runTest {
        val outputs = mutableListOf<ApproveShareRequestComponent.Output>()
        val component = createComponent(outputs = outputs)
        component.onDurationSelected(7)

        component.onApprove()

        assertEquals(1, outputs.size)
        assertTrue(outputs.first() is ApproveShareRequestComponent.Output.Approved)
    }

    @Test
    fun onApprove_failure_sets_error_and_clears_isSubmitting() = runTest {
        val ds = FakeShareDataSource()
        ds.setRespondResult(Result.failure(Exception("Already responded")))
        val outputs = mutableListOf<ApproveShareRequestComponent.Output>()
        val component = createComponent(dataSource = ds, outputs = outputs)

        component.onApprove()

        assertTrue(outputs.isEmpty())
        assertFalse(component.state.value.isSubmitting)
        assertEquals("Already responded", component.state.value.error)
    }

    @Test
    fun onReject_success_emits_Rejected_output() = runTest {
        val outputs = mutableListOf<ApproveShareRequestComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onReject()

        assertEquals(1, outputs.size)
        assertTrue(outputs.first() is ApproveShareRequestComponent.Output.Rejected)
    }

    @Test
    fun onClose_emits_Closed_output() = runTest {
        val outputs = mutableListOf<ApproveShareRequestComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onClose()

        assertEquals(1, outputs.size)
        assertTrue(outputs.first() is ApproveShareRequestComponent.Output.Closed)
    }
}
