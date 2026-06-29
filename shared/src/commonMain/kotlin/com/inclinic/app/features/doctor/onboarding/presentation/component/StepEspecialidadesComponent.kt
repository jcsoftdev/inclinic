package com.inclinic.app.features.doctor.onboarding.presentation.component

import com.arkivanov.decompose.value.Value

data class StepEspecialidadesState(
    val availableSpecialties: List<String> = emptyList(),
    val selectedSpecialtyIds: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val error: String? = null,
) {
    val canContinue: Boolean get() = selectedSpecialtyIds.isNotEmpty()
}

interface StepEspecialidadesComponent {
    val state: Value<StepEspecialidadesState>

    fun onToggleSpecialty(specialtyId: String)
    fun onContinueClicked()
    fun onErrorDismissed()
}
