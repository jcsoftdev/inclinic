package com.inclinic.app.features.patient.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.patient.medical_history.application.GetMedicalRecordDetailUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultMedicalRecordDetailComponent(
    componentContext: ComponentContext,
    private val recordId: String,
    private val getRecordDetail: GetMedicalRecordDetailUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (MedicalRecordDetailComponent.Output) -> Unit,
) : MedicalRecordDetailComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(MedicalRecordDetailState())
    override val state: Value<MedicalRecordDetailState> = _state

    init { load() }

    override fun onBack() { onOutput(MedicalRecordDetailComponent.Output.Back) }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getRecordDetail(recordId)
                .onSuccess { record -> _state.update { it.copy(isLoading = false, record = record) } }
                .onFailure { err -> _state.update { it.copy(isLoading = false, error = err.toUserMessage("Error")) } }
        }
    }
}
