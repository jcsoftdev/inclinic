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
import com.inclinic.app.features.patient.medical_history.application.GetMedicalRecordDetailUseCase
import kotlinx.coroutines.test.runTest
import kotlin.time.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private fun medicalRecordDetailTest(id: String = "rec-1"): MedicalRecordDetail {
    val now = Clock.System.now()
    return MedicalRecordDetail(
        id = id, appointmentId = "apt-1", doctorId = "doc-1",
        doctorName = "Dr. Ana Torres", specialtyName = "General Medicine",
        recordDate = now, chiefComplaint = "Annual checkup",
        symptoms = "Fatigue", diagnosis = "Healthy",
    )
}

private class FakeRecordDetailDataSource(
    private val detail: MedicalRecordDetail? = medicalRecordDetailTest(),
    private val loadError: Throwable? = null,
) : MedicalRecordDataSource {
    override suspend fun getPatientRecords(patientId: String): Result<List<MedicalRecord>> =
        Result.success(emptyList())

    override suspend fun getRecordDetail(recordId: String): Result<MedicalRecordDetail> =
        if (loadError != null) Result.failure(loadError) else Result.success(detail!!)

    override suspend fun getAccessLogs(): Result<List<HistoryAccessLog>> =
        Result.success(emptyList())
}

class DefaultMedicalRecordDetailComponentTest {

    private val dispatchers = TestAppDispatchers()
    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val ctx = DefaultComponentContext(lifecycle = lifecycle)

    private fun createComponent(
        dataSource: MedicalRecordDataSource = FakeRecordDetailDataSource(),
        outputs: MutableList<MedicalRecordDetailComponent.Output> = mutableListOf(),
    ): DefaultMedicalRecordDetailComponent {
        return DefaultMedicalRecordDetailComponent(
            componentContext = ctx,
            recordId = "rec-1",
            getRecordDetail = GetMedicalRecordDetailUseCase(dataSource, dispatchers),
            dispatchers = dispatchers,
            onOutput = outputs::add,
        )
    }

    @Test
    fun load_success_sets_record_in_state() = runTest {
        val component = createComponent()

        val state = component.state.value
        assertFalse(state.isLoading)
        assertNotNull(state.record)
        assertEquals("rec-1", state.record?.id)
        assertEquals("Dr. Ana Torres", state.record?.doctorName)
        assertNull(state.error)
    }

    @Test
    fun load_failure_sets_error_in_state() = runTest {
        val ds = FakeRecordDetailDataSource(detail = null, loadError = Exception("Record not found"))
        val component = createComponent(dataSource = ds)

        val state = component.state.value
        assertFalse(state.isLoading)
        assertNull(state.record)
        assertNotNull(state.error)
    }

    @Test
    fun onBack_emits_Back_output() = runTest {
        val outputs = mutableListOf<MedicalRecordDetailComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onBack()

        assertEquals(1, outputs.size)
        assertTrue(outputs.first() is MedicalRecordDetailComponent.Output.Back)
    }

    @Test
    fun onNavigateToMembership_emits_NavigateToMembership_output() = runTest {
        val outputs = mutableListOf<MedicalRecordDetailComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onNavigateToMembership()

        assertEquals(1, outputs.size)
        assertTrue(outputs.first() is MedicalRecordDetailComponent.Output.NavigateToMembership)
    }
}
