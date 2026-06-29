package com.inclinic.app.features.doctor.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.config.application.GetDoctorPriceConfigUseCase
import com.inclinic.app.core.util.formatDecimal
import com.inclinic.app.features.doctor.config.application.UpdateDoctorPriceConfigUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultPriceConfigComponent(
    componentContext: ComponentContext,
    private val doctorId: String,
    private val getPriceConfig: GetDoctorPriceConfigUseCase,
    private val updatePriceConfig: UpdateDoctorPriceConfigUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (PriceConfigComponent.Output) -> Unit,
) : PriceConfigComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(PriceConfigState())
    override val state: Value<PriceConfigState> = _state

    init { load() }

    override fun onPriceChange(value: String) {
        _state.update { it.copy(price = value, error = null) }
    }

    override fun onPresentialToggle() {
        _state.update { it.copy(supportsPresential = !it.supportsPresential) }
    }

    override fun onVirtualToggle() {
        _state.update { it.copy(supportsVirtual = !it.supportsVirtual) }
    }

    override fun onSave() {
        if (_state.value.isSaving) return
        val priceDouble = _state.value.price.toDoubleOrNull()
        if (priceDouble == null || priceDouble <= 0 || priceDouble > 9999.99) {
            _state.update { it.copy(error = "Price must be between S/.0.01 and S/.9999.99") }
            return
        }
        _state.update { it.copy(isSaving = true, error = null, saveSuccess = false) }
        scope.launch {
            updatePriceConfig(doctorId, priceDouble, _state.value.supportsPresential, _state.value.supportsVirtual)
                .onSuccess { config ->
                    _state.update {
                        it.copy(
                            isSaving = false,
                            saveSuccess = true,
                            price = config.consultationFee.formatDecimal(2),
                            supportsPresential = config.supportsPresential,
                            supportsVirtual = config.supportsVirtual,
                        )
                    }
                }
                .onFailure { err -> _state.update { it.copy(isSaving = false, error = err.toUserMessage("Save failed")) } }
        }
    }

    override fun onBack() { onOutput(PriceConfigComponent.Output.Back) }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getPriceConfig(doctorId)
                .onSuccess { config ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            price = config.consultationFee.formatDecimal(2),
                            supportsPresential = config.supportsPresential,
                            supportsVirtual = config.supportsVirtual,
                        )
                    }
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.toUserMessage("Error loading config")) }
                }
        }
    }
}
