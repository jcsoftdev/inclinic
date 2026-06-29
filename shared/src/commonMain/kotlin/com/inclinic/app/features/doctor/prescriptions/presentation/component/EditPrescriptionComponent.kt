package com.inclinic.app.features.doctor.prescriptions.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.features.doctor.prescriptions.core.model.Prescription

interface EditPrescriptionComponent {
    val state: Value<EditPrescriptionState>

    // Legacy single-item API kept for backward compat (delegates to index 0)
    fun onMedicationChange(v: String)
    fun onDosageChange(v: String)
    fun onFrequencyChange(v: String)
    fun onDurationChange(v: String)
    fun onInstructionsChange(v: String)

    // Multi-item API
    fun onUpdateItemName(index: Int, v: String)
    fun onUpdateItemDose(index: Int, v: String)
    fun onUpdateItemFrequency(index: Int, v: String)
    fun onUpdateItemDuration(index: Int, v: String)
    fun onUpdateItemNotes(index: Int, v: String)
    fun onAddItem()
    fun onRemoveItem(index: Int)

    fun onSubmit()
    fun onBack()

    sealed interface Output {
        data object Saved : Output
        data object Back : Output
    }
}

/** A single editable medication row in the prescription. */
data class MedicationItemDraft(
    val name: String = "",
    val dose: String = "",
    val frequency: String = "",
    val duration: String = "",
    val notes: String = "",
    val nameError: String? = null,
)

data class EditPrescriptionState(
    val prescriptionId: String = "",
    // Legacy single-item fields (kept for backward compat, mirror index 0)
    val medication: String = "",
    val dosage: String = "",
    val frequency: String = "",
    val duration: String = "",
    val instructions: String = "",
    val medicationError: String? = null,
    // Multi-item list
    val medicationItems: List<MedicationItemDraft> = emptyList(),
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    /** When non-null, the prescription was loaded and editable within the 24h window. */
    val prescription: Prescription? = null,
)
