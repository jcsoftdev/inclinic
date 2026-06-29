@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.patient.medical_history.application

import com.inclinic.app.core.model.Prescription
import com.inclinic.app.core.model.PrescriptionStatus
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.patient.infrastructure.remote.PrescriptionDataSource
import kotlinx.coroutines.test.runTest
import kotlin.time.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class FakePrescriptionDataSource : PrescriptionDataSource {
    var result: Result<Prescription> = Result.success(
        Prescription(id = "rx-1", code = "RX-001", doctorId = "doc-1", issuedAt = Clock.System.now())
    )
    var lastId: String? = null
    var callCount = 0

    override suspend fun getPatientPrescriptions(): Result<List<Prescription>> = Result.success(emptyList())

    override suspend fun getPrescriptionDetail(prescriptionId: String): Result<Prescription> {
        callCount++
        lastId = prescriptionId
        return result
    }

    override fun prescriptionPdfUrl(prescriptionId: String): String =
        "https://api.test/api/prescriptions/$prescriptionId/pdf"

    override suspend fun downloadPrescriptionPdf(prescriptionId: String): Result<ByteArray> =
        Result.success(ByteArray(0))
}

class GetPrescriptionDetailUseCaseTest {

    private val fake = FakePrescriptionDataSource()
    private val useCase = GetPrescriptionDetailUseCase(dataSource = fake, dispatchers = TestAppDispatchers())

    @Test
    fun success_returns_prescription() = runTest {
        val result = useCase("rx-1")

        assertTrue(result.isSuccess)
        assertEquals("rx-1", result.getOrNull()?.id)
        assertEquals("rx-1", fake.lastId)
        assertEquals(1, fake.callCount)
    }

    @Test
    fun failure_propagates_error() = runTest {
        fake.result = Result.failure(Exception("Not found"))

        val result = useCase("rx-999")

        assertTrue(result.isFailure)
        assertEquals("Not found", result.exceptionOrNull()?.message)
    }
}
