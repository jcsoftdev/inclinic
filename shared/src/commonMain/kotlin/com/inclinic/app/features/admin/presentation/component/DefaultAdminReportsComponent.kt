package com.inclinic.app.features.admin.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.admin.infrastructure.remote.AdminReportItem
import com.inclinic.app.features.admin.reports.application.GetReportsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultAdminReportsComponent(
    componentContext: ComponentContext,
    private val getReports: GetReportsUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (AdminReportsComponent.Output) -> Unit,
) : AdminReportsComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(AdminReportsState())
    override val state: Value<AdminReportsState> = _state

    init { load() }

    override fun onRefresh() { load() }

    override fun onFilterChange(filter: AdminReportsFilter) {
        _state.update { it.copy(activeFilter = filter) }
        load(filter)
    }

    override fun onReportClicked(report: AdminReportItem) {
        onOutput(AdminReportsComponent.Output.NavigateToResolve(report))
    }

    override fun onBack() {
        onOutput(AdminReportsComponent.Output.Back)
    }

    private fun load(filter: AdminReportsFilter = _state.value.activeFilter) {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getReports(status = filter.apiStatus)
                .onSuccess { items ->
                    _state.update { it.copy(isLoading = false, allItems = items) }
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.toUserMessage("Error cargando reportes")) }
                }
        }
    }
}
