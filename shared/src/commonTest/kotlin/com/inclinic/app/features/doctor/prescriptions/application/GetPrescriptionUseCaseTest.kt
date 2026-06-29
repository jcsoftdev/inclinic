package com.inclinic.app.features.doctor.prescriptions.application

import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.prescriptions.fakes.FakeDoctorPrescriptionsRepository
import com.inclinic.app.features.doctor.prescriptions.fakes.prescriptionFixture
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetPrescriptionUseCaseTest {

    private val repo = FakeDoctorPrescriptionsRepository()
    private val useCase = GetPrescriptionUseCase(repo, TestAppDispatchers())

    @Test
    fun returns_prescription_from_repository() = runTest {
        repo.getResult = Result.success(prescriptionFixture(id = "rx-42"))
        val result = useCase("rx-42")
        assertTrue(result.isSuccess)
        assertEquals("rx-42", result.getOrThrow().id)
        assertEquals(1, result.getOrThrow().items.size)
    }

    @Test
    fun propagates_failure() = runTest {
        repo.getResult = Result.failure(RuntimeException("not found"))
        val result = useCase("rx-missing")
        assertTrue(result.isFailure)
    }
}
