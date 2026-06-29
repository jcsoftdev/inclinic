package com.inclinic.app.features.doctor.patients.application

import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.patients.core.model.PatientListItem
import com.inclinic.app.features.doctor.patients.fakes.FakeDoctorPatientsRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SearchPatientByEmailUseCaseTest {

    private val fakeRepo = FakeDoctorPatientsRepository()
    private val dispatchers = TestAppDispatchers()
    private val useCase = SearchPatientByEmailUseCase(repository = fakeRepo, dispatchers = dispatchers)

    @Test
    fun returns_matching_patients_on_success() = runTest {
        val patient = PatientListItem("p1", "Ana Garcia", null, null, 0)
        fakeRepo.searchResult = Result.success(listOf(patient))

        val result = useCase("ana@test.com")

        assertTrue(result.isSuccess)
        assertEquals(listOf(patient), result.getOrThrow())
        assertEquals("ana@test.com", fakeRepo.lastSearchQuery)
    }

    @Test
    fun returns_empty_list_when_no_match() = runTest {
        fakeRepo.searchResult = Result.success(emptyList())

        val result = useCase("unknown@test.com")

        assertTrue(result.isSuccess)
        assertEquals(emptyList(), result.getOrThrow())
        assertEquals(1, fakeRepo.searchCallCount)
    }

    @Test
    fun propagates_repository_failure() = runTest {
        val error = RuntimeException("Search failed")
        fakeRepo.searchResult = Result.failure(error)

        val result = useCase("bad@test.com")

        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }
}
