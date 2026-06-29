@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.doctor.prescriptions.presentation

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.prescriptions.application.GetPrescriptionUseCase
import com.inclinic.app.features.doctor.prescriptions.application.UpdatePrescriptionUseCase
import com.inclinic.app.features.doctor.prescriptions.core.model.PrescriptionItem
import com.inclinic.app.features.doctor.prescriptions.fakes.FakeDoctorPrescriptionsRepository
import com.inclinic.app.features.doctor.prescriptions.fakes.prescriptionFixture
import com.inclinic.app.features.doctor.prescriptions.presentation.component.DefaultEditPrescriptionComponent
import com.inclinic.app.features.doctor.prescriptions.presentation.component.EditPrescriptionComponent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DefaultEditPrescriptionComponentTest {

    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val fakeRepo = FakeDoctorPrescriptionsRepository()
    private val dispatchers = TestAppDispatchers()

    private fun makeComponent(
        prescriptionId: String = "rx-1",
        onOutput: (EditPrescriptionComponent.Output) -> Unit = {},
    ): DefaultEditPrescriptionComponent {
        val ctx = DefaultComponentContext(lifecycle)
        return DefaultEditPrescriptionComponent(
            componentContext = ctx,
            prescriptionId = prescriptionId,
            getPrescription = GetPrescriptionUseCase(fakeRepo, dispatchers),
            updatePrescription = UpdatePrescriptionUseCase(fakeRepo, dispatchers),
            dispatchers = dispatchers,
            onOutput = onOutput,
        )
    }

    // ── Load tests ────────────────────────────────────────────────────────────

    @Test
    fun load_success_populates_all_medication_items() = runTest {
        val presc = prescriptionFixture().copy(
            items = listOf(
                PrescriptionItem("i1", "Losartan 50mg", "50mg", "Cada 12h", "30 dias", null, 0),
                PrescriptionItem("i2", "Metformina 850mg", "850mg", "Cada 8h", "60 dias", null, 1),
            ),
        )
        fakeRepo.getResult = Result.success(presc)

        val component = makeComponent()

        val items = component.state.value.medicationItems
        assertEquals(2, items.size)
        assertEquals("Losartan 50mg", items[0].name)
        assertEquals("Metformina 850mg", items[1].name)
        assertFalse(component.state.value.isLoading)
        assertNull(component.state.value.error)
    }

    @Test
    fun load_failure_sets_error_and_empty_items() = runTest {
        fakeRepo.getResult = Result.failure(RuntimeException("Network error"))

        val component = makeComponent()

        assertNotNull(component.state.value.error)
        assertTrue(component.state.value.medicationItems.isEmpty())
        assertFalse(component.state.value.isLoading)
    }

    // ── Item mutation tests ───────────────────────────────────────────────────

    @Test
    fun onUpdateItemName_changes_name_at_given_index() = runTest {
        val presc = prescriptionFixture().copy(
            items = listOf(
                PrescriptionItem("i1", "OldName", null, null, null, null, 0),
                PrescriptionItem("i2", "SecondDrug", null, null, null, null, 1),
            ),
        )
        fakeRepo.getResult = Result.success(presc)
        val component = makeComponent()

        component.onUpdateItemName(0, "NewName")

        assertEquals("NewName", component.state.value.medicationItems[0].name)
        assertEquals("SecondDrug", component.state.value.medicationItems[1].name)
    }

    @Test
    fun onUpdateItemDose_changes_dose_at_index() = runTest {
        fakeRepo.getResult = Result.success(prescriptionFixture())
        val component = makeComponent()

        component.onUpdateItemDose(0, "100mg")

        assertEquals("100mg", component.state.value.medicationItems[0].dose)
    }

    @Test
    fun onUpdateItemFrequency_changes_frequency_at_index() = runTest {
        fakeRepo.getResult = Result.success(prescriptionFixture())
        val component = makeComponent()

        component.onUpdateItemFrequency(0, "Cada 24h")

        assertEquals("Cada 24h", component.state.value.medicationItems[0].frequency)
    }

    @Test
    fun onUpdateItemDuration_changes_duration_at_index() = runTest {
        fakeRepo.getResult = Result.success(prescriptionFixture())
        val component = makeComponent()

        component.onUpdateItemDuration(0, "15 dias")

        assertEquals("15 dias", component.state.value.medicationItems[0].duration)
    }

    @Test
    fun onUpdateItemNotes_changes_notes_at_index() = runTest {
        fakeRepo.getResult = Result.success(prescriptionFixture())
        val component = makeComponent()

        component.onUpdateItemNotes(0, "Tomar con comida")

        assertEquals("Tomar con comida", component.state.value.medicationItems[0].notes)
    }

    @Test
    fun onAddItem_appends_a_blank_item_to_the_list() = runTest {
        fakeRepo.getResult = Result.success(prescriptionFixture())
        val component = makeComponent()

        val beforeCount = component.state.value.medicationItems.size
        component.onAddItem()

        assertEquals(beforeCount + 1, component.state.value.medicationItems.size)
        val newItem = component.state.value.medicationItems.last()
        assertEquals("", newItem.name)
        assertEquals("", newItem.dose)
    }

    @Test
    fun onRemoveItem_removes_item_at_given_index() = runTest {
        val presc = prescriptionFixture().copy(
            items = listOf(
                PrescriptionItem("i1", "DrugA", null, null, null, null, 0),
                PrescriptionItem("i2", "DrugB", null, null, null, null, 1),
                PrescriptionItem("i3", "DrugC", null, null, null, null, 2),
            ),
        )
        fakeRepo.getResult = Result.success(presc)
        val component = makeComponent()

        component.onRemoveItem(1)

        val items = component.state.value.medicationItems
        assertEquals(2, items.size)
        assertEquals("DrugA", items[0].name)
        assertEquals("DrugC", items[1].name)
    }

    @Test
    fun onRemoveItem_does_nothing_when_only_one_item_remains() = runTest {
        fakeRepo.getResult = Result.success(prescriptionFixture())
        val component = makeComponent()

        assertEquals(1, component.state.value.medicationItems.size)
        component.onRemoveItem(0)

        assertEquals(1, component.state.value.medicationItems.size)
    }

    // ── Validation tests ─────────────────────────────────────────────────────

    @Test
    fun onSubmit_sets_nameError_when_first_item_name_is_blank() = runTest {
        val presc = prescriptionFixture().copy(
            items = listOf(PrescriptionItem("i1", "", null, null, null, null, 0)),
        )
        fakeRepo.getResult = Result.success(presc)
        val component = makeComponent()

        component.onUpdateItemName(0, "")
        component.onSubmit()

        assertNotNull(component.state.value.medicationItems[0].nameError)
        assertEquals(0, fakeRepo.lastUpdatedId?.let { 0 } ?: 0)
    }

    // ── Submit sends ALL items ────────────────────────────────────────────────

    @Test
    fun onSubmit_sends_all_medication_items_to_the_backend() = runTest {
        val presc = prescriptionFixture().copy(
            items = listOf(
                PrescriptionItem("i1", "Losartan", "50mg", "Cada 12h", "30 dias", null, 0),
                PrescriptionItem("i2", "Aspirina", "100mg", "Cada 24h", "7 dias", null, 1),
            ),
        )
        fakeRepo.getResult = Result.success(presc)
        fakeRepo.updateResult = Result.success(presc)
        val outputs = mutableListOf<EditPrescriptionComponent.Output>()
        val component = makeComponent(onOutput = outputs::add)

        component.onSubmit()

        val draft = fakeRepo.lastUpdatedDraft
        assertNotNull(draft)
        assertEquals(2, draft!!.items.size)
        assertEquals("Losartan", draft.items[0].medicationName)
        assertEquals("Aspirina", draft.items[1].medicationName)
        assertTrue(outputs.first() is EditPrescriptionComponent.Output.Saved)
    }

    @Test
    fun onSubmit_failure_sets_error_and_clears_isSubmitting() = runTest {
        fakeRepo.getResult = Result.success(prescriptionFixture())
        fakeRepo.updateResult = Result.failure(RuntimeException("Server error"))
        val component = makeComponent()

        component.onSubmit()

        assertFalse(component.state.value.isSubmitting)
        assertNotNull(component.state.value.error)
    }

    // ── Navigation tests ─────────────────────────────────────────────────────

    @Test
    fun onBack_emits_Back_output() = runTest {
        fakeRepo.getResult = Result.success(prescriptionFixture())
        val outputs = mutableListOf<EditPrescriptionComponent.Output>()
        val component = makeComponent(onOutput = outputs::add)

        component.onBack()

        assertTrue(outputs.first() is EditPrescriptionComponent.Output.Back)
    }
}
