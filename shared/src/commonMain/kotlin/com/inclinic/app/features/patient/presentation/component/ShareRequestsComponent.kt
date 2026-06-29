package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.core.model.ShareRequest

interface ShareRequestsComponent {
    val state: Value<ShareRequestsState>
    fun onTabSelected(tab: ShareRequestTab)
    fun onRefresh()
    fun onBack()
    fun onRequestSelected(requestId: String)
    fun onInlineApprove(requestId: String)
    fun onInlineReject(requestId: String)
    sealed interface Output {
        data object Back : Output
        data class NavigateToDetail(val requestId: String) : Output
    }
}

enum class ShareRequestTab { PENDING, ACTIVE, HISTORY }

data class ShareRequestsState(
    val requests: List<ShareRequest> = emptyList(),
    val selectedTab: ShareRequestTab = ShareRequestTab.PENDING,
    val isLoading: Boolean = false,
    val submittingId: String? = null,
    val error: String? = null,
)
