package com.inclinic.app.features.doctor.prescriptions.presentation.component

import com.arkivanov.decompose.value.Value

interface CreatePrescriptionComponent {
    val state: Value<CreatePrescriptionState>

    fun onUpdateItemName(index: Int, v: String)
    fun onUpdateItemDose(index: Int, v: String)
    fun onUpdateItemFrequency(index: Int, v: String)
    fun onUpdateItemDuration(index: Int, v: String)
    fun onUpdateItemNotes(index: Int, v: String)
    fun onAddItem()
    fun onRemoveItem(index: Int)
    fun onDiagnosisChange(v: String)
    fun onInstructionsChange(v: String)

    fun onSubmit()
    fun onBack()

    sealed interface Output {
        data object Created : Output
        data object Back : Output
    }
}

data class CreatePrescriptionState(
    val appointmentId: String = "",
    val diagnosis: String = "",
    val instructions: String = "",
    val medicationItems: List<MedicationItemDraft> = listOf(MedicationItemDraft()),
    val isSubmitting: Boolean = false,
    val error: String? = null,
)
