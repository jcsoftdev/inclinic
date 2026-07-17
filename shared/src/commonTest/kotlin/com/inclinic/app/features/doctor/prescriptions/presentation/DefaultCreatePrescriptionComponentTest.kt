package com.inclinic.app.features.doctor.prescriptions.presentation

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.prescriptions.application.CreatePrescriptionUseCase
import com.inclinic.app.features.doctor.prescriptions.fakes.FakeDoctorPrescriptionsRepository
import com.inclinic.app.features.doctor.prescriptions.fakes.prescriptionFixture
import com.inclinic.app.features.doctor.prescriptions.presentation.component.CreatePrescriptionComponent
import com.inclinic.app.features.doctor.prescriptions.presentation.component.DefaultCreatePrescriptionComponent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DefaultCreatePrescriptionComponentTest {

    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val repo = FakeDoctorPrescriptionsRepository()
    private var output: CreatePrescriptionComponent.Output? = null

    private fun component() = DefaultCreatePrescriptionComponent(
        componentContext = DefaultComponentContext(lifecycle),
        appointmentId = "apt-1",
        createPrescription = CreatePrescriptionUseCase(repo, TestAppDispatchers()),
        dispatchers = TestAppDispatchers(),
        onOutput = { output = it },
    )

    @Test
    fun submit_with_empty_medication_name_sets_error_and_does_not_call_repository() = runTest {
        val c = component()
        c.onSubmit()
        assertNotNull(c.state.value.error)
        assertEquals(null, repo.lastCreatedDraft)
    }

    @Test
    fun submit_with_valid_item_calls_repository_and_emits_Created() = runTest {
        repo.createResult = Result.success(prescriptionFixture(id = "rx-new", appointmentId = "apt-1"))
        val c = component()
        c.onUpdateItemName(0, "Amoxicilina 500mg")
        c.onSubmit()

        assertEquals("apt-1", repo.lastCreatedDraft?.appointmentId)
        assertEquals("Amoxicilina 500mg", repo.lastCreatedDraft?.items?.first()?.medicationName)
        assertEquals(CreatePrescriptionComponent.Output.Created, output)
    }

    @Test
    fun submit_failure_sets_error_and_clears_isSubmitting() = runTest {
        repo.createResult = Result.failure(RuntimeException("Esta cita ya tiene una receta"))
        val c = component()
        c.onUpdateItemName(0, "Amoxicilina 500mg")
        c.onSubmit()

        assertEquals("Esta cita ya tiene una receta", c.state.value.error)
        assertTrue(!c.state.value.isSubmitting)
    }

    @Test
    fun onAddItem_appends_blank_item_onRemoveItem_removes_by_index() = runTest {
        val c = component()
        c.onAddItem()
        assertEquals(2, c.state.value.medicationItems.size)
        c.onRemoveItem(0)
        assertEquals(1, c.state.value.medicationItems.size)
    }
}
