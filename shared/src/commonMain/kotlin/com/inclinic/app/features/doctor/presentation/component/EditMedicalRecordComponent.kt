package com.inclinic.app.features.doctor.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.core.model.MedicalRecordDraft

interface EditMedicalRecordComponent {
    val state: Value<EditMedicalRecordState>

    fun onDiagnosisChange(value: String)
    fun onSymptomsChange(value: String)
    fun onTreatmentChange(value: String)
    fun onPrescriptionChange(value: String)
    fun onNotesChange(value: String)
    fun onSubmit()
    fun onBack()

    sealed interface Output {
        data object Success : Output
        data object Back : Output
    }
}

data class EditMedicalRecordState(
    val draft: MedicalRecordDraft = MedicalRecordDraft(appointmentId = ""),
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null,
)
