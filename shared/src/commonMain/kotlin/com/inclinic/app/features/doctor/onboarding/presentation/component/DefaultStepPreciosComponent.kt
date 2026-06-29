package com.inclinic.app.features.doctor.onboarding.presentation.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.onboarding.core.model.PriceConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

class DefaultStepPreciosComponent(
    componentContext: ComponentContext,
    private val dispatchers: AppDispatchers,
    private val onContinue: (PriceConfig) -> Unit,
) : StepPreciosComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())

    init {
        lifecycle.doOnDestroy { scope.cancel() }
    }

    private val _state = MutableValue(StepPreciosState())
    override val state: Value<StepPreciosState> = _state

    override fun onConsultationFeeChanged(value: String) {
        _state.update { it.copy(consultationFeeText = value, consultationFeeError = null, error = null) }
    }

    override fun onTogglePresential(enabled: Boolean) {
        _state.update { it.copy(supportsPresential = enabled, error = null) }
    }

    override fun onToggleVirtual(enabled: Boolean) {
        _state.update { it.copy(supportsVirtual = enabled, error = null) }
    }

    override fun onContinueClicked() {
        val s = _state.value
        val fee = s.consultationFeeText.toDoubleOrNull()

        when {
            fee == null || fee <= 0.0 -> {
                _state.update { it.copy(consultationFeeError = "Ingresa un precio válido mayor a 0") }
                return
            }
            !s.supportsPresential && !s.supportsVirtual -> {
                _state.update { it.copy(error = "Selecciona al menos una modalidad") }
                return
            }
        }

        onContinue(
            PriceConfig(
                consultationFee = fee,
                supportsPresential = s.supportsPresential,
                supportsVirtual = s.supportsVirtual,
            )
        )
    }

    override fun onErrorDismissed() {
        _state.update { it.copy(error = null) }
    }
}
