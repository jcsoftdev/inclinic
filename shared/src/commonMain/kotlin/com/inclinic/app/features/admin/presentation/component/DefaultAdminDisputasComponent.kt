package com.inclinic.app.features.admin.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.admin.disputes.application.GetDisputesUseCase
import com.inclinic.app.features.admin.disputes.application.GetNoShowsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultAdminDisputasComponent(
    componentContext: ComponentContext,
    private val getDisputes: GetDisputesUseCase,
    private val getNoShows: GetNoShowsUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (AdminDisputasComponent.Output) -> Unit,
) : AdminDisputasComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(AdminDisputasState())
    override val state: Value<AdminDisputasState> = _state

    init { loadDisputes() }

    override fun onSegmentSelected(segment: DisputasSegment) {
        if (_state.value.segment == segment) return
        _state.update { it.copy(segment = segment) }
        when (segment) {
            DisputasSegment.Disputes -> if (_state.value.disputes.isEmpty()) loadDisputes()
            DisputasSegment.NoShows -> if (_state.value.noShows.isEmpty()) loadNoShows()
        }
    }

    override fun onDisputeStatusFilter(filter: String?) {
        _state.update { it.copy(disputeStatusFilter = filter) }
        loadDisputes(filter)
    }

    override fun onRefresh() {
        when (_state.value.segment) {
            DisputasSegment.Disputes -> loadDisputes(_state.value.disputeStatusFilter)
            DisputasSegment.NoShows -> loadNoShows()
        }
    }

    override fun onDisputeClicked(id: String) {
        onOutput(AdminDisputasComponent.Output.NavigateToResolveDispute(id))
    }

    override fun onNoShowClicked(id: String) {
        onOutput(AdminDisputasComponent.Output.NavigateToResolveNoShow(id))
    }

    private fun loadDisputes(status: String? = _state.value.disputeStatusFilter) {
        _state.update { it.copy(disputesLoading = true, disputesError = null) }
        scope.launch {
            getDisputes(status)
                .onSuccess { items ->
                    _state.update { it.copy(disputesLoading = false, disputes = items) }
                }
                .onFailure { err ->
                    _state.update { it.copy(disputesLoading = false, disputesError = err.toUserMessage("Error al cargar disputas")) }
                }
        }
    }

    private fun loadNoShows() {
        _state.update { it.copy(noShowsLoading = true, noShowsError = null) }
        scope.launch {
            getNoShows()
                .onSuccess { items ->
                    _state.update { it.copy(noShowsLoading = false, noShows = items) }
                }
                .onFailure { err ->
                    _state.update { it.copy(noShowsLoading = false, noShowsError = err.toUserMessage("Error al cargar no-shows")) }
                }
        }
    }
}
