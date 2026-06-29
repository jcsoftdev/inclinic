package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.patient.doctor_profile.application.GetDoctorDetailUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultConsultTypeComponent(
    componentContext: ComponentContext,
    private val doctorId: String,
    private val getDoctorDetail: GetDoctorDetailUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (ConsultTypeComponent.Output) -> Unit,
) : ConsultTypeComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(ConsultTypeState())
    override val state: Value<ConsultTypeState> = _state

    init { loadDoctor() }

    override fun onTypeSelected(type: ConsultType) {
        _state.update { it.copy(selectedType = type) }
    }

    override fun onContinue() {
        val type = _state.value.selectedType
        val typeStr = when (type) {
            ConsultType.PRESENCIAL -> "office"
            ConsultType.TELEMEDICINE -> "telemedicine"
            ConsultType.HOME_VISIT -> "home"
        }
        onOutput(ConsultTypeComponent.Output.NavigateToAvailability(doctorId, typeStr))
    }

    override fun onBack() { onOutput(ConsultTypeComponent.Output.Back) }

    private fun loadDoctor() {
        _state.update { it.copy(isLoading = true) }
        scope.launch {
            getDoctorDetail(doctorId)
                .onSuccess { doctor -> _state.update { it.copy(isLoading = false, doctor = doctor) } }
                .onFailure { _state.update { it.copy(isLoading = false) } }
        }
    }
}
