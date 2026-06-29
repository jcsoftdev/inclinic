package com.inclinic.app.features.doctor.sharing.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.features.doctor.sharing.core.model.ShareRequest

interface ShareRequestsListComponent {
    val state: Value<ShareRequestsListState>

    fun onRefresh()
    fun onSelectIncoming()
    fun onSelectOutgoing()
    /** Doctor cancels a PENDING request they sent. */
    fun onCancel(requestId: String)
    fun onRequestNew()
    fun onBack()

    sealed interface Output {
        data object NavigateToRequestShare : Output
        data object Back : Output
    }
}

data class ShareRequestsListState(
    val isLoading: Boolean = false,
    /** true = show approved (access granted); false = show pending/rejected/expired */
    val showIncoming: Boolean = true,
    val incomingRequests: List<ShareRequest> = emptyList(),
    val outgoingRequests: List<ShareRequest> = emptyList(),
    val error: String? = null,
)
