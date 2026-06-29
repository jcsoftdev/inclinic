package com.inclinic.app.features.doctor.onboarding.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.onboarding.application.ResubmitOnboardingUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultCorregirSolicitudComponent(
    componentContext: ComponentContext,
    private val dispatchers: AppDispatchers,
    private val resubmitOnboardingUseCase: ResubmitOnboardingUseCase,
    initialCorrections: Map<String, String> = emptyMap(),
    private val onOutput: (CorregirSolicitudComponent.Output) -> Unit = {},
) : CorregirSolicitudComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())

    init {
        lifecycle.doOnDestroy { scope.cancel() }
    }

    private val _state = MutableValue(CorregirSolicitudState(corrections = initialCorrections))
    override val state: Value<CorregirSolicitudState> = _state

    override fun onFieldChanged(field: String, value: String) {
        _state.update { s ->
            s.copy(corrections = s.corrections + (field to value), error = null)
        }
    }

    override fun onSubmitClicked() {
        val corrections = _state.value.corrections
        if (corrections.isEmpty()) {
            _state.update { it.copy(error = "No hay correcciones que enviar") }
            return
        }

        scope.launch {
            _state.update { it.copy(isSubmitting = true, error = null) }
            resubmitOnboardingUseCase(corrections)
                .onSuccess {
                    _state.update { it.copy(isSubmitting = false, submitSuccess = true) }
                }
                .onFailure { err ->
                    _state.update { it.copy(isSubmitting = false, error = err.toUserMessage("Error al reenviar")) }
                }
        }
    }

    override fun onErrorDismissed() {
        _state.update { it.copy(error = null) }
    }

    fun onLogOutClicked() {
        onOutput(CorregirSolicitudComponent.Output.LogOut)
    }
}
