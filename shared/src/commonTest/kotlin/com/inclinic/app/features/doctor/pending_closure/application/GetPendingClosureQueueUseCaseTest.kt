package com.inclinic.app.features.doctor.pending_closure.application

import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.pending_closure.core.model.PendingClosureItem
import com.inclinic.app.features.doctor.pending_closure.fakes.FakePendingClosureAppointmentDataSource
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private fun stubPendingClosureItem(id: String) = PendingClosureItem(
    id = id,
    patientName = "María García",
    startTime = "2026-06-01T09:00:00.000Z",
    price = 70.0,
    specialtyName = "Cardiología",
    visitType = "CLINIC",
)

class GetPendingClosureQueueUseCaseTest {

    private val dispatchers = TestAppDispatchers()
    private val dataSource = FakePendingClosureAppointmentDataSource()

    @Test
    fun returns_items_from_data_source() = runTest {
        dataSource.pendingClosureResult = Result.success(listOf(stubPendingClosureItem("a1")))
        val useCase = GetPendingClosureQueueUseCase(dataSource, dispatchers)

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals("a1", result.getOrNull()?.first()?.id)
    }

    @Test
    fun forwards_from_and_to_params() = runTest {
        val useCase = GetPendingClosureQueueUseCase(dataSource, dispatchers)
        useCase(from = "2026-06-01", to = "2026-06-30")
        assertEquals("2026-06-01", dataSource.lastFrom)
        assertEquals("2026-06-30", dataSource.lastTo)
    }

    @Test
    fun propagates_failure() = runTest {
        dataSource.pendingClosureResult = Result.failure(RuntimeException("boom"))
        val useCase = GetPendingClosureQueueUseCase(dataSource, dispatchers)
        assertTrue(useCase().isFailure)
    }
}
