@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.patient.medical_history.application

import com.inclinic.app.core.model.AccessType
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

private class FakeAccessLogsDataSource : MedicalRecordDataSource {
    var logsResult: Result<List<HistoryAccessLog>> = Result.success(emptyList())
    var callCount = 0

    override suspend fun getAccessLogs(): Result<List<HistoryAccessLog>> {
        callCount++
        return logsResult
    }

    override suspend fun getPatientRecords(patientId: String): Result<List<MedicalRecord>> = Result.success(emptyList())
    override suspend fun getRecordDetail(recordId: String): Result<MedicalRecordDetail> = Result.failure(UnsupportedOperationException())
}

class GetHistoryAccessLogsUseCaseTest {

    private val fake = FakeAccessLogsDataSource()
    private val useCase = GetHistoryAccessLogsUseCase(dataSource = fake, dispatchers = TestAppDispatchers())

    @Test
    fun success_returns_logs() = runTest {
        val logs = listOf(
            HistoryAccessLog(id = "log-1", doctorId = "doc-1", doctorName = "Dr. Torres", accessType = AccessType.READ, accessedAt = Clock.System.now()),
        )
        fake.logsResult = Result.success(logs)

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals("log-1", result.getOrNull()?.first()?.id)
    }

    @Test
    fun failure_propagates_error() = runTest {
        fake.logsResult = Result.failure(Exception("Unauthorized"))

        val result = useCase()

        assertTrue(result.isFailure)
        assertEquals("Unauthorized", result.exceptionOrNull()?.message)
    }
}
