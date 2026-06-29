package com.inclinic.app.features.doctor.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.MedicalRecordDraft
import com.inclinic.app.features.doctor.medical_records.application.GetMedicalRecordDetailUseCase
import com.inclinic.app.features.doctor.medical_records.application.UpdateMedicalRecordUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultEditMedicalRecordComponent(
    componentContext: ComponentContext,
    private val recordId: String,
    private val getRecord: GetMedicalRecordDetailUseCase,
    private val updateRecord: UpdateMedicalRecordUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (EditMedicalRecordComponent.Output) -> Unit,
) : EditMedicalRecordComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(EditMedicalRecordState())
    override val state: Value<EditMedicalRecordState> = _state

    init { load() }

    override fun onDiagnosisChange(value: String) {
        _state.update { it.copy(draft = it.draft.copy(diagnosis = value)) }
    }

    override fun onSymptomsChange(value: String) {
        _state.update { it.copy(draft = it.draft.copy(symptoms = value)) }
    }

    override fun onTreatmentChange(value: String) {
        _state.update { it.copy(draft = it.draft.copy(treatment = value)) }
    }

    override fun onPrescriptionChange(value: String) {
        _state.update { it.copy(draft = it.draft.copy(prescription = value)) }
    }

    override fun onNotesChange(value: String) {
        _state.update { it.copy(draft = it.draft.copy(notes = value)) }
    }

    override fun onSubmit() {
        if (_state.value.isSubmitting) return
        _state.update { it.copy(isSubmitting = true, error = null) }
        val draft = _state.value.draft
        scope.launch {
            updateRecord(recordId, draft)
                .onSuccess { _state.update { it.copy(isSubmitting = false) }; onOutput(EditMedicalRecordComponent.Output.Success) }
                .onFailure { err -> _state.update { it.copy(isSubmitting = false, error = err.toUserMessage("Update failed")) } }
        }
    }

    override fun onBack() { onOutput(EditMedicalRecordComponent.Output.Back) }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getRecord(recordId)
                .onSuccess { record ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            draft = MedicalRecordDraft(
                                appointmentId = record.appointmentId,
                                diagnosis = record.diagnosis,
                                symptoms = record.symptoms,
                                treatment = record.treatment,
                                prescription = record.prescription ?: "",
                                notes = record.notes ?: "",
                            )
                        )
                    }
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.toUserMessage("Error loading record")) }
                }
        }
    }
}
