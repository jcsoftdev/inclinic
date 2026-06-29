package com.inclinic.app.features.doctor.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.patient_detail.application.GetPatientDetailUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultPatientDetailComponent(
    componentContext: ComponentContext,
    private val patientId: String,
    private val getPatientDetail: GetPatientDetailUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (PatientDetailComponent.Output) -> Unit,
) : PatientDetailComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(PatientDetailState())
    override val state: Value<PatientDetailState> = _state

    init { load() }

    override fun onViewMedicalRecords() {
        onOutput(PatientDetailComponent.Output.NavigateToMedicalRecords(patientId))
    }

    override fun onBack() { onOutput(PatientDetailComponent.Output.Back) }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getPatientDetail(patientId)
                .onSuccess { data -> _state.update { it.copy(isLoading = false, data = data) } }
                .onFailure { err -> _state.update { it.copy(isLoading = false, error = err.toUserMessage("Error loading patient")) } }
        }
    }
}
