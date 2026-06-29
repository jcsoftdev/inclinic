@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.core.model.Prescription
import com.inclinic.app.core.model.PrescriptionStatus
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.patient.infrastructure.remote.PrescriptionDataSource
import com.inclinic.app.features.patient.medical_history.application.DownloadPrescriptionPdfUseCase
import com.inclinic.app.features.patient.medical_history.application.GetPrescriptionDetailUseCase
import com.inclinic.app.features.patient.medical_history.application.GetPrescriptionPdfUrlUseCase
import kotlinx.coroutines.test.runTest
import kotlin.time.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private fun testPrescription(id: String = "rx-1"): Prescription {
    val now = Clock.System.now()
    return Prescription(
        id = id, code = "RX-2026-001", doctorId = "doc-1",
        doctorName = "Dr. Ana Torres", doctorLicense = "CMP-123",
        specialtyName = "General Medicine", issuedAt = now,
        medications = emptyList(), status = PrescriptionStatus.ACTIVE,
    )
}

private class FakePrescriptionDataSource(
    private val prescription: Prescription? = testPrescription(),
    private val loadError: Throwable? = null,
) : PrescriptionDataSource {
    override suspend fun getPatientPrescriptions(): Result<List<Prescription>> =
        Result.success(emptyList())

    override suspend fun getPrescriptionDetail(prescriptionId: String): Result<Prescription> =
        if (loadError != null) Result.failure(loadError) else Result.success(prescription!!)

    override fun prescriptionPdfUrl(prescriptionId: String): String =
        "https://api.test/api/prescriptions/$prescriptionId/pdf"

    override suspend fun downloadPrescriptionPdf(prescriptionId: String): Result<ByteArray> =
        Result.success(ByteArray(0))
}

class DefaultPrescriptionDetailComponentTest {

    private val dispatchers = TestAppDispatchers()
    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val ctx = DefaultComponentContext(lifecycle = lifecycle)

    private fun createComponent(
        dataSource: PrescriptionDataSource = FakePrescriptionDataSource(),
        outputs: MutableList<PrescriptionDetailComponent.Output> = mutableListOf(),
    ): DefaultPrescriptionDetailComponent {
        return DefaultPrescriptionDetailComponent(
            componentContext = ctx,
            prescriptionId = "rx-1",
            getPrescriptionDetail = GetPrescriptionDetailUseCase(dataSource, dispatchers),
            getPrescriptionPdfUrl = GetPrescriptionPdfUrlUseCase(dataSource),
            downloadPrescriptionPdf = DownloadPrescriptionPdfUseCase(dataSource, dispatchers),
            dispatchers = dispatchers,
            onOutput = outputs::add,
        )
    }

    @Test
    fun load_success_sets_prescription_in_state() = runTest {
        val component = createComponent()

        val state = component.state.value
        assertFalse(state.isLoading)
        assertNotNull(state.prescription)
        assertEquals("rx-1", state.prescription?.id)
        assertEquals("RX-2026-001", state.prescription?.code)
        assertEquals("Dr. Ana Torres", state.prescription?.doctorName)
        assertNull(state.error)
    }

    @Test
    fun load_failure_sets_error_in_state() = runTest {
        val ds = FakePrescriptionDataSource(prescription = null, loadError = Exception("Not found"))
        val component = createComponent(dataSource = ds)

        val state = component.state.value
        assertFalse(state.isLoading)
        assertNull(state.prescription)
        assertEquals("Not found", state.error)
    }

    @Test
    fun onBack_emits_Back_output() = runTest {
        val outputs = mutableListOf<PrescriptionDetailComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onBack()

        assertEquals(1, outputs.size)
        assertTrue(outputs.first() is PrescriptionDetailComponent.Output.Back)
    }

    @Test
    fun pdfUrl_returns_absolute_backend_url() = runTest {
        val component = createComponent()

        assertEquals("https://api.test/api/prescriptions/rx-1/pdf", component.pdfUrl())
    }

    @Test
    fun onShare_does_not_crash() = runTest {
        val component = createComponent()

        // onShare is a no-op in the current implementation; verify it doesn't throw
        component.onShare()

        assertFalse(component.state.value.isLoading)
    }
}
