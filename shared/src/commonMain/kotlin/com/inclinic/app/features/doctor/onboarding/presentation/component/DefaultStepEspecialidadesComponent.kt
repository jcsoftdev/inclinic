package com.inclinic.app.features.doctor.onboarding.presentation.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

class DefaultStepEspecialidadesComponent(
    componentContext: ComponentContext,
    private val dispatchers: AppDispatchers,
    availableSpecialties: List<String>,
    private val onContinue: (List<String>) -> Unit,
) : StepEspecialidadesComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())

    init {
        lifecycle.doOnDestroy { scope.cancel() }
    }

    private val _state = MutableValue(
        StepEspecialidadesState(availableSpecialties = availableSpecialties)
    )
    override val state: Value<StepEspecialidadesState> = _state

    override fun onToggleSpecialty(specialtyId: String) {
        _state.update { s ->
            val current = s.selectedSpecialtyIds
            val updated = if (specialtyId in current) current - specialtyId else current + specialtyId
            s.copy(selectedSpecialtyIds = updated, error = null)
        }
    }

    override fun onContinueClicked() {
        if (!_state.value.canContinue) {
            _state.update { it.copy(error = "Selecciona al menos una especialidad") }
            return
        }
        onContinue(_state.value.selectedSpecialtyIds.toList())
    }

    override fun onErrorDismissed() {
        _state.update { it.copy(error = null) }
    }
}
