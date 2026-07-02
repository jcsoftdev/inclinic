package com.inclinic.app.features.doctor.pending_closure.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.features.doctor.pending_closure.core.model.PendingClosureItem

interface PendingClosureQueueComponent {
    val state: Value<PendingClosureQueueState>

    fun onAppointmentTapped(appointmentId: String)
    fun onRetry()
    fun onBack()

    sealed interface Output {
        data class NavigateToDetail(val appointmentId: String) : Output
        data object Back : Output
    }
}

data class PendingClosureQueueState(
    val isLoading: Boolean = false,
    val items: List<PendingClosureItem> = emptyList(),
    val error: String? = null,
)
