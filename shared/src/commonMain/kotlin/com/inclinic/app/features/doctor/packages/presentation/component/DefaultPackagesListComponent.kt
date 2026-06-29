package com.inclinic.app.features.doctor.packages.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.packages.application.GetDoctorPackagesUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultPackagesListComponent(
    componentContext: ComponentContext,
    private val getPackages: GetDoctorPackagesUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (PackagesListComponent.Output) -> Unit,
) : PackagesListComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(PackagesListState())
    override val state: Value<PackagesListState> = _state

    init { load() }

    override fun onRefresh() { load() }

    override fun onCreateClicked() {
        onOutput(PackagesListComponent.Output.NavigateToCreate)
    }

    override fun onPackageClicked(id: String) {
        onOutput(PackagesListComponent.Output.NavigateToDetail(id))
    }

    override fun onTabSelected(tab: PackageListTab) {
        _state.update { it.copy(selectedTab = tab) }
    }

    override fun onBack() {
        onOutput(PackagesListComponent.Output.Back)
    }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getPackages()
                .onSuccess { list -> _state.update { it.copy(isLoading = false, packages = list) } }
                .onFailure { err -> _state.update { it.copy(isLoading = false, error = err.toUserMessage("Error loading packages")) } }
        }
    }
}
