package com.inclinic.app.features.doctor.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.core.model.MedicalRecordDraft
import kotlinx.serialization.Serializable

interface CreateMedicalRecordComponent {
    val state: Value<CreateMedicalRecordState>

    fun onDiagnosisChange(value: String)
    fun onSymptomsChange(value: String)
    fun onTreatmentChange(value: String)
    fun onPrescriptionChange(value: String)
    fun onNotesChange(value: String)
    fun onSubmit()
    fun onBack()
    fun onRestoreDraft()
    fun onDiscardDraft()

    sealed interface Output {
        data class Success(val recordId: String) : Output
        data object Back : Output
    }
}

/**
 * @Serializable so StateKeeper survives Android configuration changes.
 * [isSubmitting] and [success] are @Transient — they reset after recreation.
 *
 * REQ-4-009
 */
@Serializable
data class CreateMedicalRecordState(
    val draft: MedicalRecordDraft = MedicalRecordDraft(),
    @kotlinx.serialization.Transient val isSubmitting: Boolean = false,
    val hasSavedDraft: Boolean = false,
    @kotlinx.serialization.Transient val error: String? = null,
    @kotlinx.serialization.Transient val success: Boolean = false,
)
