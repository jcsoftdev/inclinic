package com.inclinic.app.features.doctor.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.MedicalRecordDraft
import com.inclinic.app.features.doctor.medical_records.application.CreateMedicalRecordUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Draft is kept in-memory only (per session).
 * Draft key for SecureStorage (Phase 4): "draft_medical_record_{patientId}"
 */
class DefaultCreateMedicalRecordComponent(
    componentContext: ComponentContext,
    private val patientId: String,
    private val appointmentId: String?,
    private val createRecord: CreateMedicalRecordUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (CreateMedicalRecordComponent.Output) -> Unit,
) : CreateMedicalRecordComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private var savedDraft: MedicalRecordDraft? = null

    /**
     * Restore persisted draft content (diagnoses, prescriptions, etc.) across
     * Android configuration changes.
     *
     * REQ-4-009
     */
    private val restoredState: CreateMedicalRecordState? =
        stateKeeper.consume("create_record_state", CreateMedicalRecordState.serializer())

    private val _state = MutableValue(
        restoredState ?: CreateMedicalRecordState(
            draft = MedicalRecordDraft(appointmentId = appointmentId)
        )
    )
    override val state: Value<CreateMedicalRecordState> = _state

    init {
        stateKeeper.register("create_record_state", CreateMedicalRecordState.serializer()) {
            _state.value
        }
    }

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
            createRecord(patientId, draft)
                .onSuccess { record ->
                    savedDraft = null
                    _state.update { it.copy(isSubmitting = false, success = true) }
                    onOutput(CreateMedicalRecordComponent.Output.Success(record.id))
                }
                .onFailure { err ->
                    _state.update { it.copy(isSubmitting = false, error = err.toUserMessage("Save failed")) }
                }
        }
    }

    override fun onBack() {
        // Save draft in-memory before leaving
        val current = _state.value.draft
        if (current.diagnosis.isNotBlank() || current.symptoms.isNotBlank()) {
            savedDraft = current
        }
        onOutput(CreateMedicalRecordComponent.Output.Back)
    }

    override fun onRestoreDraft() {
        val draft = savedDraft ?: return
        _state.update { it.copy(draft = draft, hasSavedDraft = false) }
    }

    override fun onDiscardDraft() {
        savedDraft = null
        _state.update { it.copy(hasSavedDraft = false) }
    }
}
