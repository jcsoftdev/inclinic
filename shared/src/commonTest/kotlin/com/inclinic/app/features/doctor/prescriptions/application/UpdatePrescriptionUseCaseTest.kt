package com.inclinic.app.features.doctor.prescriptions.application

import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.prescriptions.core.model.PrescriptionItemDraft
import com.inclinic.app.features.doctor.prescriptions.core.model.UpdatePrescriptionDraft
import com.inclinic.app.features.doctor.prescriptions.fakes.FakeDoctorPrescriptionsRepository
import com.inclinic.app.features.doctor.prescriptions.fakes.prescriptionFixture
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UpdatePrescriptionUseCaseTest {

    private val repo = FakeDoctorPrescriptionsRepository()
    private val useCase = UpdatePrescriptionUseCase(repo, TestAppDispatchers())

    private fun draft(med: String = "Ibuprofeno 400mg") = UpdatePrescriptionDraft(
        diagnosis = null,
        instructions = "Tomar con comida",
        notes = null,
        validUntil = null,
        items = listOf(
            PrescriptionItemDraft(
                medicationName = med,
                dosage = "400mg",
                frequency = "Cada 8h",
                duration = "7 dias",
                notes = null,
                order = 0,
            ),
        ),
    )

    @Test
    fun returns_updated_prescription() = runTest {
        repo.updateResult = Result.success(prescriptionFixture(id = "rx-1", medication = "Ibuprofeno 400mg"))
        val result = useCase("rx-1", draft("Ibuprofeno 400mg"))
        assertTrue(result.isSuccess)
        assertEquals("rx-1", result.getOrThrow().id)
    }

    @Test
    fun passes_id_and_draft_to_repository() = runTest {
        useCase("rx-99", draft("Paracetamol"))
        assertEquals("rx-99", repo.lastUpdatedId)
        assertEquals("Paracetamol", repo.lastUpdatedDraft?.items?.firstOrNull()?.medicationName)
    }

    @Test
    fun propagates_failure() = runTest {
        repo.updateResult = Result.failure(RuntimeException("window expired"))
        val result = useCase("rx-1", draft())
        assertTrue(result.isFailure)
        assertEquals("window expired", result.exceptionOrNull()?.message)
    }
}
