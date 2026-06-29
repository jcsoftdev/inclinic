package com.inclinic.app.features.patient.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.patient.appointments.application.GetRescheduleProposalUseCase
import com.inclinic.app.features.patient.appointments.application.RespondRescheduleUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultRescheduleResponseComponent(
    componentContext: ComponentContext,
    private val appointmentId: String,
    private val getProposal: GetRescheduleProposalUseCase,
    private val respondReschedule: RespondRescheduleUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (RescheduleResponseComponent.Output) -> Unit,
) : RescheduleResponseComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(RescheduleResponseState())
    override val state: Value<RescheduleResponseState> = _state

    init { load() }

    override fun onAccept() { respond(accept = true) }

    override fun onReject() { respond(accept = false) }

    override fun onBack() { onOutput(RescheduleResponseComponent.Output.Back) }

    private fun respond(accept: Boolean) {
        val proposal = _state.value.proposal ?: return
        _state.update { it.copy(isResponding = true, error = null) }
        scope.launch {
            respondReschedule(proposal.id, accept)
                .onSuccess { onOutput(RescheduleResponseComponent.Output.Responded) }
                .onFailure { err ->
                    _state.update { it.copy(isResponding = false, error = err.toUserMessage()) }
                }
        }
    }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getProposal(appointmentId)
                .onSuccess { proposal ->
                    _state.update { it.copy(isLoading = false, proposal = proposal) }
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.toUserMessage()) }
                }
        }
    }
}
