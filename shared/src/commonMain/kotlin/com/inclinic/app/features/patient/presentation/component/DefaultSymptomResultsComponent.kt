package com.inclinic.app.features.patient.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.patient.search.application.AnalyzeSymptomsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultSymptomResultsComponent(
    componentContext: ComponentContext,
    private val symptoms: String,
    private val analyzeSymptoms: AnalyzeSymptomsUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (SymptomResultsComponent.Output) -> Unit,
) : SymptomResultsComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(SymptomResultsState(symptoms = symptoms))
    override val state: Value<SymptomResultsState> = _state

    init { loadResults() }

    override fun onEditSymptoms() { onOutput(SymptomResultsComponent.Output.EditSymptoms) }
    override fun onViewDoctorProfile(doctorId: String) { onOutput(SymptomResultsComponent.Output.NavigateToDoctorProfile(doctorId)) }
    override fun onBack() { onOutput(SymptomResultsComponent.Output.Back) }
    override fun onRetry() { loadResults() }

    private fun loadResults() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            analyzeSymptoms(symptoms)
                .onSuccess { result -> _state.update { it.copy(isLoading = false, analysis = result.analysis, doctors = result.doctors) } }
                .onFailure { err -> _state.update { it.copy(isLoading = false, error = err.toUserMessage("Error al analizar")) } }
        }
    }
}
