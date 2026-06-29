package com.inclinic.app.features.patient.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.PackageStatus
import com.inclinic.app.features.patient.therapy.application.GetTherapyPackagesUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultTherapyPackagesListComponent(
    componentContext: ComponentContext,
    private val patientId: String,
    private val getTherapyPackages: GetTherapyPackagesUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (TherapyPackagesListComponent.Output) -> Unit,
) : TherapyPackagesListComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(TherapyPackagesListState())
    override val state: Value<TherapyPackagesListState> = _state

    init { load() }

    override fun onTabChange(tab: PackagesTab) {
        _state.update { it.copy(selectedTab = tab) }
        load()
    }

    override fun onPackageTapped(packageId: String) {
        onOutput(TherapyPackagesListComponent.Output.NavigateToPackageDetail(packageId))
    }

    override fun onBuyPackage() {
        onOutput(TherapyPackagesListComponent.Output.NavigateToOffers)
    }

    override fun onBack() {
        onOutput(TherapyPackagesListComponent.Output.Back)
    }

    override fun onErrorDismissed() {
        _state.update { it.copy(error = null) }
    }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            val statusFilter = when (_state.value.selectedTab) {
                PackagesTab.ACTIVE -> PackageStatus.ACTIVE.name
                PackagesTab.PENDING_PAYMENT -> PackageStatus.PENDING_PAYMENT.name
                PackagesTab.HISTORY -> null
            }
            getTherapyPackages(patientId, statusFilter)
                .onSuccess { packages ->
                    val filtered = when (_state.value.selectedTab) {
                        PackagesTab.HISTORY -> packages.filter {
                            it.status == PackageStatus.COMPLETED || it.status == PackageStatus.CANCELLED || it.status == PackageStatus.EXPIRED
                        }
                        else -> packages
                    }
                    _state.update { it.copy(isLoading = false, packages = filtered) }
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.toUserMessage("Error al cargar paquetes")) }
                }
        }
    }
}
