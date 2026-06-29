package com.inclinic.app.features.doctor.packages.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.packages.application.CancelPackageUseCase
import com.inclinic.app.features.doctor.packages.application.GetDoctorPackagesUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultPackageDetailComponent(
    componentContext: ComponentContext,
    private val packageId: String,
    private val getPackages: GetDoctorPackagesUseCase,
    private val cancelPackage: CancelPackageUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (PackageDetailComponent.Output) -> Unit,
) : PackageDetailComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(PackageDetailState())
    override val state: Value<PackageDetailState> = _state

    init { load() }

    override fun onRetry() { load() }

    override fun onCancel() {
        if (_state.value.pkg == null) return
        _state.update { it.copy(isCancelling = true, error = null) }
        scope.launch {
            cancelPackage(packageId)
                .onSuccess {
                    _state.update { it.copy(isCancelling = false) }
                    onOutput(PackageDetailComponent.Output.Cancelled)
                }
                .onFailure { err ->
                    _state.update { it.copy(isCancelling = false, error = err.toUserMessage("Error cancelling package")) }
                }
        }
    }

    override fun onBack() {
        onOutput(PackageDetailComponent.Output.Back)
    }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getPackages()
                .onSuccess { list ->
                    val match = list.firstOrNull { it.id == packageId }
                    if (match == null) {
                        _state.update { it.copy(isLoading = false, pkg = null, error = "Paquete no encontrado") }
                    } else {
                        _state.update { it.copy(isLoading = false, pkg = match, error = null) }
                    }
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.toUserMessage("Error loading package")) }
                }
        }
    }
}
