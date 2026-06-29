package com.inclinic.app.features.doctor.patients.application

import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.patients.core.model.PatientList
import com.inclinic.app.features.doctor.patients.core.model.PatientListItem
import com.inclinic.app.features.doctor.patients.core.model.PatientListStats
import com.inclinic.app.features.doctor.patients.fakes.FakeDoctorPatientsRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetDoctorPatientsUseCaseTest {

    private val fakeRepo = FakeDoctorPatientsRepository()
    private val dispatchers = TestAppDispatchers()
    private val useCase = GetDoctorPatientsUseCase(repository = fakeRepo, dispatchers = dispatchers)

    @Test
    fun returns_empty_list_when_no_patients() = runTest {
        fakeRepo.getPatientsResult = Result.success(PatientList())

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(emptyList(), result.getOrThrow().items)
    }

    @Test
    fun returns_patient_list_with_stats_on_success() = runTest {
        val patients = listOf(
            PatientListItem("p1", "Ana Garcia", "2025-01-10", null, 3),
            PatientListItem("p2", "Carlos Lopez", null, null, 1),
        )
        val list = PatientList(items = patients, stats = PatientListStats(total = 2, active = 1, premium = 0))
        fakeRepo.getPatientsResult = Result.success(list)

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(list, result.getOrThrow())
        assertEquals(2, result.getOrThrow().stats.total)
        assertEquals(1, fakeRepo.getPatientsCallCount)
    }

    @Test
    fun propagates_repository_failure() = runTest {
        val error = RuntimeException("Network error")
        fakeRepo.getPatientsResult = Result.failure(error)

        val result = useCase()

        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }
}
