@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.core.model.AccessType
import com.inclinic.app.core.model.HistoryAccessLog
import com.inclinic.app.core.model.MedicalRecord
import com.inclinic.app.core.model.MedicalRecordDetail
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.patient.infrastructure.remote.MedicalRecordDataSource
import com.inclinic.app.features.patient.medical_history.application.GetHistoryAccessLogsUseCase
import kotlinx.coroutines.test.runTest
import kotlin.time.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private fun historyAccessLogTest(id: String = "log-1"): HistoryAccessLog {
    val now = Clock.System.now()
    return HistoryAccessLog(
        id = id, doctorId = "doc-1", doctorName = "Dr. Ana Torres",
        accessType = AccessType.READ, accessedAt = now,
    )
}

private class FakeAccessLogsDataSource(
    private val logs: List<HistoryAccessLog> = listOf(historyAccessLogTest()),
    private val loadError: Throwable? = null,
) : MedicalRecordDataSource {
    var getAccessLogsCallCount = 0

    override suspend fun getPatientRecords(patientId: String): Result<List<MedicalRecord>> =
        Result.success(emptyList())

    override suspend fun getRecordDetail(recordId: String): Result<MedicalRecordDetail> =
        Result.failure(UnsupportedOperationException())

    override suspend fun getAccessLogs(): Result<List<HistoryAccessLog>> {
        getAccessLogsCallCount++
        return if (loadError != null) Result.failure(loadError) else Result.success(logs)
    }
}

class DefaultHistoryAccessLogsComponentTest {

    private val dispatchers = TestAppDispatchers()
    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val ctx = DefaultComponentContext(lifecycle = lifecycle)

    private fun createComponent(
        dataSource: FakeAccessLogsDataSource = FakeAccessLogsDataSource(),
        outputs: MutableList<HistoryAccessLogsComponent.Output> = mutableListOf(),
    ): DefaultHistoryAccessLogsComponent {
        return DefaultHistoryAccessLogsComponent(
            componentContext = ctx,
            getAccessLogs = GetHistoryAccessLogsUseCase(dataSource, dispatchers),
            dispatchers = dispatchers,
            onOutput = outputs::add,
        )
    }

    @Test
    fun load_success_sets_logs_in_state() = runTest {
        val component = createComponent()

        val state = component.state.value
        assertFalse(state.isLoading)
        assertEquals(1, state.logs.size)
        assertEquals("log-1", state.logs.first().id)
        assertEquals("Dr. Ana Torres", state.logs.first().doctorName)
        assertNull(state.error)
    }

    @Test
    fun load_failure_sets_error_in_state() = runTest {
        val ds = FakeAccessLogsDataSource(logs = emptyList(), loadError = Exception("Unauthorized"))
        val component = createComponent(dataSource = ds)

        val state = component.state.value
        assertFalse(state.isLoading)
        assertTrue(state.logs.isEmpty())
        assertNotNull(state.error)
    }

    @Test
    fun load_empty_list_sets_empty_logs() = runTest {
        val ds = FakeAccessLogsDataSource(logs = emptyList())
        val component = createComponent(dataSource = ds)

        val state = component.state.value
        assertFalse(state.isLoading)
        assertTrue(state.logs.isEmpty())
        assertNull(state.error)
    }

    @Test
    fun onRefresh_triggers_reload() = runTest {
        val ds = FakeAccessLogsDataSource()
        val component = createComponent(dataSource = ds)
        assertEquals(1, ds.getAccessLogsCallCount)

        component.onRefresh()

        assertEquals(2, ds.getAccessLogsCallCount)
    }

    @Test
    fun onBack_emits_Back_output() = runTest {
        val outputs = mutableListOf<HistoryAccessLogsComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onBack()

        assertEquals(1, outputs.size)
        assertTrue(outputs.first() is HistoryAccessLogsComponent.Output.Back)
    }

    @Test
    fun onManageAccess_emits_NavigateToManageAccess_output() = runTest {
        val outputs = mutableListOf<HistoryAccessLogsComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onManageAccess()

        assertEquals(1, outputs.size)
        assertTrue(outputs.first() is HistoryAccessLogsComponent.Output.NavigateToManageAccess)
    }
}
