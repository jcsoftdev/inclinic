package com.inclinic.app.features.doctor.reschedule.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.features.doctor.reschedule.core.model.RescheduleRequest

interface RescheduleQueueComponent {
    val state: Value<RescheduleQueueState>

    fun onRetry()
    fun onApprove(requestId: String)
    fun onReject(requestId: String)
    fun onBack()

    sealed interface Output {
        data object Back : Output
    }
}

data class RescheduleQueueState(
    val isLoading: Boolean = false,
    val requests: List<RescheduleRequest> = emptyList(),
    val error: String? = null,
    val respondingId: String? = null,
)
