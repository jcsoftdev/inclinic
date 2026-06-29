package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.value.Value

/**
 * Displays and allows editing of the patient's clinical profile:
 * blood type, biometrics, allergies, chronic conditions, emergency contact.
 *
 * Backend: GET /patients/:id/medical-profile (load) + PUT /patients/:id/medical-profile (save).
 */
interface ClinicalProfileComponent {
    val state: Value<ClinicalProfileState>

    fun onBack()
    fun onNavigateToDeleteAccount()
    fun onToggleEdit()
    fun onBloodTypeChange(value: String)
    fun onHeightCmChange(value: String)
    fun onWeightKgChange(value: String)
    fun onAllergiesChange(value: String)        // comma-separated raw input
    fun onConditionsChange(value: String)       // comma-separated raw input
    fun onEmergencyNameChange(value: String)
    fun onEmergencyPhoneChange(value: String)
    fun onEmergencyRelationChange(value: String)
    fun onSave()
    fun onDismissError()

    sealed interface Output {
        data object Back : Output
        data object NavigateToDeleteAccount : Output
    }
}

data class ClinicalProfileState(
    // ── Loaded data ──────────────────────────────────────────────────────────
    val bloodType: String? = null,
    val heightCm: Float? = null,
    val weightKg: Float? = null,
    val allergies: List<String> = emptyList(),
    val conditions: List<String> = emptyList(),
    val emergencyContactName: String? = null,
    val emergencyContactPhone: String? = null,
    val emergencyContactRelation: String? = null,

    // ── Edit-mode drafts (raw string input) ──────────────────────────────────
    val isEditing: Boolean = false,
    val draftBloodType: String = "",
    val draftHeightCm: String = "",
    val draftWeightKg: String = "",
    val draftAllergies: String = "",    // comma-separated
    val draftConditions: String = "",   // comma-separated
    val draftEmergencyName: String = "",
    val draftEmergencyPhone: String = "",
    val draftEmergencyRelation: String = "",

    // ── Async state ───────────────────────────────────────────────────────────
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
)
