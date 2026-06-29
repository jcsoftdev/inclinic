package com.inclinic.app.features.doctor.profile.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.core.model.Specialty

interface EditSpecialtiesComponent {
    val state: Value<EditSpecialtiesState>

    fun onToggleSpecialty(specialtyId: String)
    fun onSave()
    fun onBack()

    sealed interface Output {
        data object Back : Output
        data object Saved : Output
    }
}

data class EditSpecialtiesState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val availableSpecialties: List<Specialty> = emptyList(),
    val selectedIds: Set<String> = emptySet(),
    val error: String? = null,
    val saveSuccess: Boolean = false,
)
