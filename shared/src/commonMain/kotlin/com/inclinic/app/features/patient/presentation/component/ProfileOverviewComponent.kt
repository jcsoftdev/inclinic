package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.core.model.MedicalProfile
import com.inclinic.app.core.model.PatientProfile

interface ProfileOverviewComponent {
    val state: Value<ProfileOverviewState>

    fun onEditProfile()
    fun onSettings()
    fun onLogout()
    fun onErrorDismissed()

    sealed interface Output {
        data object NavigateToEditProfile : Output
        data object NavigateToSettings : Output
    }
}

data class ProfileOverviewState(
    val profile: PatientProfile? = null,
    val medicalProfile: MedicalProfile? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)
