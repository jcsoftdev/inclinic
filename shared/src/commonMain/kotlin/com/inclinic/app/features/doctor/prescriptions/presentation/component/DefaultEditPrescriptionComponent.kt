package com.inclinic.app.features.doctor.prescriptions.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.prescriptions.application.GetPrescriptionUseCase
import com.inclinic.app.features.doctor.prescriptions.application.UpdatePrescriptionUseCase
import com.inclinic.app.features.doctor.prescriptions.core.model.PrescriptionItemDraft
import com.inclinic.app.features.doctor.prescriptions.core.model.UpdatePrescriptionDraft
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultEditPrescriptionComponent(
    componentContext: ComponentContext,
    private val prescriptionId: String,
    private val getPrescription: GetPrescriptionUseCase,
    private val updatePrescription: UpdatePrescriptionUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (EditPrescriptionComponent.Output) -> Unit,
) : EditPrescriptionComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    private val _state = MutableValue(EditPrescriptionState(prescriptionId = prescriptionId))
    override val state: Value<EditPrescriptionState> = _state

    init {
        lifecycle.doOnCreate { load() }
        lifecycle.doOnDestroy { scope.cancel() }
    }

    // ── Legacy single-item shims (delegate to index 0 for backward compat) ────

    override fun onMedicationChange(v: String) = onUpdateItemName(0, v)
    override fun onDosageChange(v: String) = onUpdateItemDose(0, v)
    override fun onFrequencyChange(v: String) = onUpdateItemFrequency(0, v)
    override fun onDurationChange(v: String) = onUpdateItemDuration(0, v)
    override fun onInstructionsChange(v: String) {
        _state.value = _state.value.copy(instructions = v)
    }

    // ── Multi-item API ────────────────────────────────────────────────────────

    override fun onUpdateItemName(index: Int, v: String) {
        updateItem(index) { copy(name = v, nameError = null) }
        // Mirror index-0 into legacy field for backward compat
        if (index == 0) _state.value = _state.value.copy(medication = v, medicationError = null)
    }

    override fun onUpdateItemDose(index: Int, v: String) {
        updateItem(index) { copy(dose = v) }
        if (index == 0) _state.value = _state.value.copy(dosage = v)
    }

    override fun onUpdateItemFrequency(index: Int, v: String) {
        updateItem(index) { copy(frequency = v) }
        if (index == 0) _state.value = _state.value.copy(frequency = v)
    }

    override fun onUpdateItemDuration(index: Int, v: String) {
        updateItem(index) { copy(duration = v) }
        if (index == 0) _state.value = _state.value.copy(duration = v)
    }

    override fun onUpdateItemNotes(index: Int, v: String) {
        updateItem(index) { copy(notes = v) }
    }

    override fun onAddItem() {
        val items = _state.value.medicationItems + MedicationItemDraft()
        _state.value = _state.value.copy(medicationItems = items)
    }

    override fun onRemoveItem(index: Int) {
        val items = _state.value.medicationItems
        if (items.size <= 1) return
        _state.value = _state.value.copy(medicationItems = items.toMutableList().also { it.removeAt(index) })
    }

    // ── Submit ────────────────────────────────────────────────────────────────

    override fun onSubmit() {
        val s = _state.value
        val items = s.medicationItems

        // Validate: every item must have a non-blank name
        val validated = items.mapIndexed { idx, item ->
            if (item.name.isBlank()) item.copy(nameError = "El medicamento es obligatorio") else item
        }
        if (validated.any { it.nameError != null }) {
            _state.value = s.copy(
                medicationItems = validated,
                medicationError = if (validated.firstOrNull()?.nameError != null) validated[0].nameError else s.medicationError,
            )
            return
        }

        _state.value = s.copy(isSubmitting = true, error = null)
        scope.launch {
            val draft = UpdatePrescriptionDraft(
                diagnosis = s.prescription?.diagnosis,
                instructions = s.instructions.trim().ifBlank { null },
                notes = null,
                validUntil = s.prescription?.validUntil,
                items = items.mapIndexed { idx, item ->
                    PrescriptionItemDraft(
                        medicationName = item.name.trim(),
                        dosage = item.dose.trim().ifBlank { null },
                        frequency = item.frequency.trim().ifBlank { null },
                        duration = item.duration.trim().ifBlank { null },
                        notes = item.notes.trim().ifBlank { null },
                        order = idx,
                    )
                },
            )
            updatePrescription(prescriptionId, draft).fold(
                onSuccess = { onOutput(EditPrescriptionComponent.Output.Saved) },
                onFailure = { err ->
                    _state.value = _state.value.copy(
                        isSubmitting = false,
                        error = err.toUserMessage("Error al guardar receta"),
                    )
                },
            )
        }
    }

    override fun onBack() = onOutput(EditPrescriptionComponent.Output.Back)

    // ── Private helpers ───────────────────────────────────────────────────────

    private fun updateItem(index: Int, transform: MedicationItemDraft.() -> MedicationItemDraft) {
        val items = _state.value.medicationItems.toMutableList()
        if (index < 0 || index >= items.size) return
        items[index] = items[index].transform()
        _state.value = _state.value.copy(medicationItems = items)
    }

    private fun load() {
        scope.launch {
            getPrescription(prescriptionId).fold(
                onSuccess = { presc ->
                    val items = if (presc.items.isNotEmpty()) {
                        presc.items.map { item ->
                            MedicationItemDraft(
                                name = item.medicationName,
                                dose = item.dosage ?: "",
                                frequency = item.frequency ?: "",
                                duration = item.duration ?: "",
                                notes = item.notes ?: "",
                            )
                        }
                    } else {
                        listOf(MedicationItemDraft())
                    }
                    val firstItem = presc.items.firstOrNull()
                    _state.value = EditPrescriptionState(
                        prescriptionId = prescriptionId,
                        prescription = presc,
                        medicationItems = items,
                        // Legacy single-item fields mirrored from index 0
                        medication = firstItem?.medicationName ?: "",
                        dosage = firstItem?.dosage ?: "",
                        frequency = firstItem?.frequency ?: "",
                        duration = firstItem?.duration ?: "",
                        instructions = presc.instructions ?: "",
                        isLoading = false,
                    )
                },
                onFailure = { err ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = err.toUserMessage("Error al cargar receta"),
                    )
                },
            )
        }
    }
}
