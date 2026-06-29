package com.inclinic.app.features.doctor.patients.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.patients.application.SearchPatientByEmailUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultSearchPatientComponent(
    componentContext: ComponentContext,
    private val searchPatient: SearchPatientByEmailUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (SearchPatientComponent.Output) -> Unit,
) : SearchPatientComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(SearchPatientState())
    override val state: Value<SearchPatientState> = _state

    override fun onQueryChange(query: String) {
        _state.update { it.copy(query = query, error = null) }
    }

    override fun onSearch() {
        val query = _state.value.query.trim()
        if (query.isEmpty()) return
        _state.update { it.copy(isSearching = true, error = null) }
        scope.launch {
            searchPatient(query)
                .onSuccess { results ->
                    _state.update { it.copy(isSearching = false, results = results, hasSearched = true) }
                }
                .onFailure { err ->
                    _state.update { it.copy(isSearching = false, error = err.toUserMessage("Error searching patients"), hasSearched = true) }
                }
        }
    }

    override fun onPatientClicked(patientId: String) {
        onOutput(SearchPatientComponent.Output.NavigateToPatient(patientId))
    }

    override fun onBack() {
        onOutput(SearchPatientComponent.Output.Back)
    }
}
