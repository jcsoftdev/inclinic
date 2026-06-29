@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.core.model.HistoryAccessLog
import com.inclinic.app.core.model.MedicalRecord
import com.inclinic.app.core.model.MedicalRecordDetail
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.patient.infrastructure.remote.MedicalRecordDataSource
import com.inclinic.app.features.patient.medical_history.application.GetMedicalHistoryUseCase
import kotlinx.coroutines.test.runTest
import kotlin.time.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private fun medicalHistoryTestRecord(id: String = "rec-1"): MedicalRecord {
    val now = Clock.System.now()
    return MedicalRecord(
        id = id, appointmentId = "apt-1", patientId = "pat-1", doctorId = "doc-1",
        doctorName = "Dr. Ana Torres", specialtyName = "General Medicine",
        diagnosis = "Common cold", symptoms = "Runny nose, cough",
        treatment = "Rest and fluids", prescription = null, notes = null,
        createdAt = now,
    )
}

private class FakeMedicalHistoryDataSource(
    private val records: List<MedicalRecord> = listOf(medicalHistoryTestRecord()),
    private val loadError: Throwable? = null,
) : MedicalRecordDataSource {
    var getPatientRecordsCallCount = 0

    override suspend fun getPatientRecords(patientId: String): Result<List<MedicalRecord>> {
        getPatientRecordsCallCount++
        return if (loadError != null) Result.failure(loadError) else Result.success(records)
    }

    override suspend fun getRecordDetail(recordId: String): Result<MedicalRecordDetail> =
        Result.failure(UnsupportedOperationException())

    override suspend fun getAccessLogs(): Result<List<HistoryAccessLog>> =
        Result.success(emptyList())
}

class DefaultMedicalHistoryComponentTest {

    private val dispatchers = TestAppDispatchers()
    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val ctx = DefaultComponentContext(lifecycle = lifecycle)

    private fun createComponent(
        dataSource: FakeMedicalHistoryDataSource = FakeMedicalHistoryDataSource(),
        outputs: MutableList<MedicalHistoryComponent.Output> = mutableListOf(),
    ): DefaultMedicalHistoryComponent {
        return DefaultMedicalHistoryComponent(
            componentContext = ctx,
            patientId = "pat-1",
            getMedicalHistory = GetMedicalHistoryUseCase(dataSource, dispatchers),
            dispatchers = dispatchers,
            onOutput = outputs::add,
        )
    }

    @Test
    fun load_success_sets_records_in_state() = runTest {
        val component = createComponent()

        val state = component.state.value
        assertFalse(state.isLoading)
        assertEquals(1, state.records.size)
        assertEquals("rec-1", state.records.first().id)
        assertNull(state.error)
    }

    @Test
    fun load_failure_sets_error_in_state() = runTest {
        val ds = FakeMedicalHistoryDataSource(records = emptyList(), loadError = Exception("Forbidden"))
        val component = createComponent(dataSource = ds)

        val state = component.state.value
        assertFalse(state.isLoading)
        assertTrue(state.records.isEmpty())
        assertNotNull(state.error)
    }

    @Test
    fun load_empty_list_sets_empty_records() = runTest {
        val ds = FakeMedicalHistoryDataSource(records = emptyList())
        val component = createComponent(dataSource = ds)

        val state = component.state.value
        assertFalse(state.isLoading)
        assertTrue(state.records.isEmpty())
        assertNull(state.error)
    }

    @Test
    fun onRefresh_triggers_reload() = runTest {
        val ds = FakeMedicalHistoryDataSource()
        val component = createComponent(dataSource = ds)
        assertEquals(1, ds.getPatientRecordsCallCount)

        component.onRefresh()

        assertEquals(2, ds.getPatientRecordsCallCount)
    }

    @Test
    fun record_exposes_doctorName_and_specialtyName() = runTest {
        val component = createComponent()

        val record = component.state.value.records.first()
        assertEquals("Dr. Ana Torres", record.doctorName)
        assertEquals("General Medicine", record.specialtyName)
    }

    @Test
    fun onBack_emits_Back_output() = runTest {
        val outputs = mutableListOf<MedicalHistoryComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onBack()

        assertEquals(1, outputs.size)
        assertTrue(outputs.first() is MedicalHistoryComponent.Output.Back)
    }
}
