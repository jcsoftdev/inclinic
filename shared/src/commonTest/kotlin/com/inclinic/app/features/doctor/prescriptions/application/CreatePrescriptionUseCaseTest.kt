package com.inclinic.app.features.doctor.prescriptions.application

import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.prescriptions.core.model.CreatePrescriptionDraft
import com.inclinic.app.features.doctor.prescriptions.core.model.PrescriptionItemDraft
import com.inclinic.app.features.doctor.prescriptions.fakes.FakeDoctorPrescriptionsRepository
import com.inclinic.app.features.doctor.prescriptions.fakes.prescriptionFixture
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CreatePrescriptionUseCaseTest {

    private val repo = FakeDoctorPrescriptionsRepository()
    private val useCase = CreatePrescriptionUseCase(repo, TestAppDispatchers())

    private fun draft(appointmentId: String = "apt-1") = CreatePrescriptionDraft(
        appointmentId = appointmentId,
        diagnosis = null,
        instructions = "Tomar con agua",
        notes = null,
        validUntil = null,
        items = listOf(
            PrescriptionItemDraft(
                medicationName = "Amoxicilina 500mg",
                dosage = "500mg",
                frequency = "Cada 8h",
                duration = "7 dias",
                notes = null,
                order = 0,
            ),
        ),
    )

    @Test
    fun returns_created_prescription() = runTest {
        repo.createResult = Result.success(prescriptionFixture(id = "rx-new", appointmentId = "apt-1"))
        val result = useCase(draft("apt-1"))
        assertTrue(result.isSuccess)
        assertEquals("rx-new", result.getOrThrow().id)
    }

    @Test
    fun passes_draft_to_repository() = runTest {
        useCase(draft("apt-42"))
        assertEquals("apt-42", repo.lastCreatedDraft?.appointmentId)
    }

    @Test
    fun propagates_failure() = runTest {
        repo.createResult = Result.failure(RuntimeException("ya existe una receta"))
        val result = useCase(draft())
        assertTrue(result.isFailure)
        assertEquals("ya existe una receta", result.exceptionOrNull()?.message)
    }
}
