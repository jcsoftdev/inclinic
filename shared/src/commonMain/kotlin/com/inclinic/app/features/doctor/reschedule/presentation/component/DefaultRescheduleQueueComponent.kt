package com.inclinic.app.features.doctor.reschedule.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.reschedule.application.GetRescheduleRequestsUseCase
import com.inclinic.app.features.doctor.reschedule.application.RespondRescheduleRequestUseCase
import com.inclinic.app.features.doctor.reschedule.core.model.RescheduleRequestStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultRescheduleQueueComponent(
    componentContext: ComponentContext,
    private val getRequests: GetRescheduleRequestsUseCase,
    private val respondReschedule: RespondRescheduleRequestUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (RescheduleQueueComponent.Output) -> Unit,
) : RescheduleQueueComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(RescheduleQueueState())
    override val state: Value<RescheduleQueueState> = _state

    init { load() }

    override fun onRetry() { load() }

    override fun onApprove(requestId: String) {
        respond(requestId, RescheduleRequestStatus.APPROVED)
    }

    override fun onReject(requestId: String) {
        respond(requestId, RescheduleRequestStatus.REJECTED)
    }

    override fun onBack() {
        onOutput(RescheduleQueueComponent.Output.Back)
    }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            val result = getRequests()
            _state.update { state ->
                state.copy(
                    isLoading = false,
                    requests = result.getOrElse { emptyList() },
                    error = result.exceptionOrNull()?.toUserMessage(),
                )
            }
        }
    }

    private fun respond(requestId: String, decision: RescheduleRequestStatus) {
        _state.update { it.copy(respondingId = requestId) }
        scope.launch {
            respondReschedule(requestId, decision)
                .onSuccess { updated ->
                    _state.update { state ->
                        state.copy(
                            respondingId = null,
                            requests = state.requests.map { req ->
                                if (req.id == requestId) updated else req
                            },
                        )
                    }
                }
                .onFailure { err ->
                    _state.update {
                        it.copy(
                            respondingId = null,
                            error = err.toUserMessage("Error responding to request"),
                        )
                    }
                }
        }
    }
}
