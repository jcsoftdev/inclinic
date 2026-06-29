package com.inclinic.app.features.patient.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.patient.medical_history.application.GetMedicalHistoryUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultMedicalHistoryComponent(
    componentContext: ComponentContext,
    private val patientId: String,
    private val getMedicalHistory: GetMedicalHistoryUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (MedicalHistoryComponent.Output) -> Unit,
) : MedicalHistoryComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(MedicalHistoryState())
    override val state: Value<MedicalHistoryState> = _state

    init { load() }

    override fun onRefresh() { load() }
    override fun onBack() { onOutput(MedicalHistoryComponent.Output.Back) }
    override fun onNavigateToClinicalProfile() { onOutput(MedicalHistoryComponent.Output.NavigateToClinicalProfile) }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getMedicalHistory(patientId)
                .onSuccess { records -> _state.update { it.copy(isLoading = false, records = records) } }
                .onFailure { err -> _state.update { it.copy(isLoading = false, error = err.toUserMessage("Failed to load medical history")) } }
        }
    }
}
