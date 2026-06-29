package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.core.model.ShareRequest

interface ActiveAccessesComponent {
    val state: Value<ActiveAccessesState>

    fun onRefresh()
    fun onBack()
    fun onRevoke(requestId: String)

    sealed interface Output {
        data object Back : Output
    }
}

data class ActiveAccessesState(
    val accesses: List<ShareRequest> = emptyList(),
    val isLoading: Boolean = false,
    val revokingId: String? = null,
    val error: String? = null,
)
