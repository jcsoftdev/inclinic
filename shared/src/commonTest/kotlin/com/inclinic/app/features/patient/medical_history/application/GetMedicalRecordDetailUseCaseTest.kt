@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.patient.medical_history.application

import com.inclinic.app.core.model.HistoryAccessLog
import com.inclinic.app.core.model.MedicalRecord
import com.inclinic.app.core.model.MedicalRecordDetail
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.patient.infrastructure.remote.MedicalRecordDataSource
import kotlinx.coroutines.test.runTest
import kotlin.time.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class FakeMedicalRecordDataSource : MedicalRecordDataSource {
    var detailResult: Result<MedicalRecordDetail> = Result.success(
        MedicalRecordDetail(
            id = "mr-1", appointmentId = "apt-1", doctorId = "doc-1",
            doctorName = "Dr. Torres", recordDate = Clock.System.now(),
            diagnosis = "Hipertensión arterial",
        )
    )
    var lastId: String? = null
    var callCount = 0

    override suspend fun getRecordDetail(recordId: String): Result<MedicalRecordDetail> {
        callCount++
        lastId = recordId
        return detailResult
    }

    override suspend fun getPatientRecords(patientId: String): Result<List<MedicalRecord>> = Result.success(emptyList())
    override suspend fun getAccessLogs(): Result<List<HistoryAccessLog>> = Result.success(emptyList())
}

class GetMedicalRecordDetailUseCaseTest {

    private val fake = FakeMedicalRecordDataSource()
    private val useCase = GetMedicalRecordDetailUseCase(dataSource = fake, dispatchers = TestAppDispatchers())

    @Test
    fun success_returns_record_detail() = runTest {
        val result = useCase("mr-1")

        assertTrue(result.isSuccess)
        assertEquals("mr-1", result.getOrNull()?.id)
        assertEquals("Hipertensión arterial", result.getOrNull()?.diagnosis)
        assertEquals("mr-1", fake.lastId)
    }

    @Test
    fun failure_propagates_not_found() = runTest {
        fake.detailResult = Result.failure(Exception("Not found"))

        val result = useCase("mr-999")

        assertTrue(result.isFailure)
        assertEquals("Not found", result.exceptionOrNull()?.message)
    }
}
