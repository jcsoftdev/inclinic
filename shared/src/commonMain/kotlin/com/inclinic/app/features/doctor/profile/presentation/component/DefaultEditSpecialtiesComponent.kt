package com.inclinic.app.features.doctor.profile.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.profile.application.EditSpecialtiesUseCase
import com.inclinic.app.features.doctor.profile.application.GetDoctorProfileUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultEditSpecialtiesComponent(
    componentContext: ComponentContext,
    private val getProfile: GetDoctorProfileUseCase,
    private val editSpecialties: EditSpecialtiesUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (EditSpecialtiesComponent.Output) -> Unit,
) : EditSpecialtiesComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())

    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(EditSpecialtiesState())
    override val state: Value<EditSpecialtiesState> = _state

    init { load() }

    override fun onToggleSpecialty(specialtyId: String) {
        _state.update { s ->
            val next = if (specialtyId in s.selectedIds) s.selectedIds - specialtyId
                       else s.selectedIds + specialtyId
            s.copy(selectedIds = next)
        }
    }

    override fun onSave() {
        if (_state.value.isSaving) return
        _state.update { it.copy(isSaving = true, error = null) }
        scope.launch {
            editSpecialties(_state.value.selectedIds.toList())
                .onSuccess {
                    _state.update { it.copy(isSaving = false, saveSuccess = true) }
                    onOutput(EditSpecialtiesComponent.Output.Saved)
                }
                .onFailure { err ->
                    _state.update { it.copy(isSaving = false, error = err.toUserMessage("Save failed")) }
                }
        }
    }

    override fun onBack() = onOutput(EditSpecialtiesComponent.Output.Back)
    override fun onNavigateToRequestSpecialty() = onOutput(EditSpecialtiesComponent.Output.NavigateToRequestSpecialty)

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getProfile()
                .onSuccess { profile ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            availableSpecialties = profile.specialties,
                            selectedIds = profile.specialties.map { sp -> sp.id }.toSet(),
                        )
                    }
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.toUserMessage("Error loading")) }
                }
        }
    }
}
