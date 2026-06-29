package com.inclinic.app.features.doctor.sharing.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.sharing.application.RequestShareUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultRequestShareComponent(
    componentContext: ComponentContext,
    private val requestShare: RequestShareUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (RequestShareComponent.Output) -> Unit,
) : RequestShareComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(RequestShareState())
    override val state: Value<RequestShareState> = _state

    override fun onPatientIdChange(value: String) {
        _state.update { it.copy(patientId = value, error = null) }
    }

    override fun onReasonChange(value: String) {
        _state.update { it.copy(reason = value, error = null) }
    }

    override fun onSubmit() {
        val s = _state.value
        if (s.patientId.isBlank() || s.reason.length < 20 || s.isSubmitting) return
        _state.update { it.copy(isSubmitting = true, error = null) }
        scope.launch {
            requestShare(
                patientId = s.patientId.trim(),
                reason = s.reason.trim(),
            )
                .onSuccess {
                    _state.update { it.copy(isSubmitting = false, success = true) }
                    onOutput(RequestShareComponent.Output.Success)
                }
                .onFailure { err ->
                    _state.update { it.copy(isSubmitting = false, error = err.toUserMessage("Error al enviar solicitud")) }
                }
        }
    }

    override fun onBack() {
        onOutput(RequestShareComponent.Output.Back)
    }
}
