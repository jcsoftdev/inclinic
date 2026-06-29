package com.inclinic.app.features.doctor.profile.application

import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.profile.fakes.FakeDoctorProfileRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EditSpecialtiesUseCaseTest {

    private val fakeRepo = FakeDoctorProfileRepository()
    private val dispatchers = TestAppDispatchers()
    private val useCase = EditSpecialtiesUseCase(repository = fakeRepo, dispatchers = dispatchers)

    @Test
    fun passes_specialty_ids_to_repository() = runTest {
        val ids = listOf("sp-1", "sp-2")

        val result = useCase(ids)

        assertTrue(result.isSuccess)
        assertEquals(1, fakeRepo.editSpecialtiesCallCount)
        assertEquals(ids, fakeRepo.lastEditedSpecialtyIds)
    }

    @Test
    fun accepts_empty_list() = runTest {
        val result = useCase(emptyList())

        assertTrue(result.isSuccess)
        assertEquals(emptyList<String>(), fakeRepo.lastEditedSpecialtyIds)
    }

    @Test
    fun propagates_failure() = runTest {
        fakeRepo.editSpecialtiesResult = Result.failure(RuntimeException("Forbidden"))

        val result = useCase(listOf("sp-1"))

        assertTrue(result.isFailure)
    }
}
