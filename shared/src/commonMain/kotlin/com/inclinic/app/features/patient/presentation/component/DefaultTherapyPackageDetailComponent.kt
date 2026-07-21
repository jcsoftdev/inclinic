package com.inclinic.app.features.patient.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.SessionStatus
import com.inclinic.app.features.patient.therapy.application.GetTherapyPackageDetailUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultTherapyPackageDetailComponent(
    componentContext: ComponentContext,
    private val packageId: String,
    private val getPackageDetail: GetTherapyPackageDetailUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (TherapyPackageDetailComponent.Output) -> Unit,
) : TherapyPackageDetailComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(TherapyPackageDetailState())
    override val state: Value<TherapyPackageDetailState> = _state

    init { load() }

    override fun onTabChange(tab: SessionsTab) {
        _state.update { it.copy(selectedTab = tab) }
    }

    override fun onScheduleNextSession() {
        val pkg = _state.value.therapyPackage ?: return
        onOutput(TherapyPackageDetailComponent.Output.NavigateToScheduleSession(packageId, pkg.doctorId))
    }

    override fun onViewStatement() {
        onOutput(TherapyPackageDetailComponent.Output.NavigateToStatement(packageId))
    }

    override fun onBack() {
        onOutput(TherapyPackageDetailComponent.Output.Back)
    }

    override fun onErrorDismissed() {
        _state.update { it.copy(error = null) }
    }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getPackageDetail(packageId)
                .onSuccess { (pkg, sessions) ->
                    _state.update { it.copy(isLoading = false, therapyPackage = pkg, sessions = sessions) }
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.toUserMessage("Error al cargar paquete")) }
                }
        }
    }
}
