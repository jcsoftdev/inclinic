package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.core.model.MedicalProfile
import com.inclinic.app.core.model.PatientProfile

interface PatientProfileComponent {
    val state: Value<PatientProfileState>

    fun onNameChange(name: String)
    fun onPhoneChange(phone: String)
    fun onDateOfBirthChange(dob: String)
    fun onToggleEdit()
    fun onSave()
    fun onBack()
    fun onErrorDismissed()

    sealed interface Output {
        data object Back : Output
        data object Saved : Output
    }
}

data class PatientProfileState(
    val profile: PatientProfile? = null,
    val medicalProfile: MedicalProfile? = null,
    val name: String = "",
    val phone: String = "",
    val dateOfBirth: String = "",
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
)
