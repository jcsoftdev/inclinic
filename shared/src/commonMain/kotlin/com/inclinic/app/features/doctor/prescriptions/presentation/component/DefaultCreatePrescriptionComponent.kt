package com.inclinic.app.features.doctor.prescriptions.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.prescriptions.application.CreatePrescriptionUseCase
import com.inclinic.app.features.doctor.prescriptions.core.model.CreatePrescriptionDraft
import com.inclinic.app.features.doctor.prescriptions.core.model.PrescriptionItemDraft
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultCreatePrescriptionComponent(
    componentContext: ComponentContext,
    private val appointmentId: String,
    private val createPrescription: CreatePrescriptionUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (CreatePrescriptionComponent.Output) -> Unit,
) : CreatePrescriptionComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    private val _state = MutableValue(CreatePrescriptionState(appointmentId = appointmentId))
    override val state: Value<CreatePrescriptionState> = _state

    init {
        lifecycle.doOnDestroy { scope.cancel() }
    }

    override fun onUpdateItemName(index: Int, v: String) = updateItem(index) { copy(name = v, nameError = null) }
    override fun onUpdateItemDose(index: Int, v: String) = updateItem(index) { copy(dose = v) }
    override fun onUpdateItemFrequency(index: Int, v: String) = updateItem(index) { copy(frequency = v) }
    override fun onUpdateItemDuration(index: Int, v: String) = updateItem(index) { copy(duration = v) }
    override fun onUpdateItemNotes(index: Int, v: String) = updateItem(index) { copy(notes = v) }

    override fun onAddItem() {
        _state.value = _state.value.copy(medicationItems = _state.value.medicationItems + MedicationItemDraft())
    }

    override fun onRemoveItem(index: Int) {
        val items = _state.value.medicationItems
        if (items.size <= 1) return
        _state.value = _state.value.copy(medicationItems = items.toMutableList().also { it.removeAt(index) })
    }

    override fun onDiagnosisChange(v: String) {
        _state.value = _state.value.copy(diagnosis = v)
    }

    override fun onInstructionsChange(v: String) {
        _state.value = _state.value.copy(instructions = v)
    }

    override fun onSubmit() {
        val current = _state.value
        val validated = current.medicationItems.map { item ->
            if (item.name.isBlank()) item.copy(nameError = "Nombre del medicamento requerido") else item
        }
        if (validated.any { it.nameError != null }) {
            _state.value = current.copy(medicationItems = validated, error = "Completa el nombre de cada medicamento")
            return
        }

        _state.value = current.copy(isSubmitting = true, error = null)
        scope.launch {
            val draft = CreatePrescriptionDraft(
                appointmentId = appointmentId,
                diagnosis = current.diagnosis.trim().ifBlank { null },
                instructions = current.instructions.trim().ifBlank { null },
                notes = null,
                validUntil = null,
                items = current.medicationItems.mapIndexed { i, item ->
                    PrescriptionItemDraft(
                        medicationName = item.name.trim(),
                        dosage = item.dose.trim().ifBlank { null },
                        frequency = item.frequency.trim().ifBlank { null },
                        duration = item.duration.trim().ifBlank { null },
                        notes = item.notes.trim().ifBlank { null },
                        order = i,
                    )
                },
            )
            createPrescription(draft).fold(
                onSuccess = {
                    _state.value = _state.value.copy(isSubmitting = false)
                    onOutput(CreatePrescriptionComponent.Output.Created)
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        isSubmitting = false,
                        error = e.toUserMessage("No se pudo crear la receta"),
                    )
                },
            )
        }
    }

    override fun onBack() = onOutput(CreatePrescriptionComponent.Output.Back)

    private fun updateItem(index: Int, transform: MedicationItemDraft.() -> MedicationItemDraft) {
        val items = _state.value.medicationItems.toMutableList()
        if (index < 0 || index >= items.size) return
        items[index] = items[index].transform()
        _state.value = _state.value.copy(medicationItems = items)
    }
}
